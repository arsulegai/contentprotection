package client

import (
	bytes2 "bytes"
	"cli"
	"constants"
	"crypto/sha512"
	"encoding/base64"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	proto2 "github.com/golang/protobuf/proto"
	"github.com/hyperledger/sawtooth-sdk-go/protobuf/batch_pb2"
	"github.com/hyperledger/sawtooth-sdk-go/protobuf/transaction_pb2"
	"github.com/hyperledger/sawtooth-sdk-go/signing"
	"globals"
	"io/ioutil"
	"math/rand"
	"net/http"
	"os"
	"strconv"
	"strings"
)

func ComposeAndSendTransaction() error {
	// In byte[] read
	privateKey, err := ioutil.ReadFile(globals.PrivateKeyFilePath)
	if err != nil {
		return errors.New("unable to read the private key file")
	}
	// Get Sec256k1PrivateKey
	sec256k1PrivateKey := signing.NewSecp256k1PrivateKey(privateKey)
	signer := signing.NewCryptoFactory(signing.NewSecp256k1Context()).NewSigner(sec256k1PrivateKey)
	publicKey := signer.GetPublicKey().AsHex()
	// Need to calculate address of input and output parameters before sending transaction
	// Check what is the requested transaction
	switch *cli.OperationType {
	case constants.UserType:
		switch *cli.OperationAction {
		case constants.RegisterAction:
			fallthrough
		case constants.UpdateAction:
			return writeUserData(signer, &publicKey)
		default:
			// Not expected
			return errors.New("unknown action requested")
		}
	case constants.ContentType:
		switch *cli.OperationAction {
		case constants.ReadAction:
			return readContentData(signer, &publicKey)
		case constants.CreateAction:
			fallthrough
		case constants.UpdateAction:
			return writeContentData(signer, &publicKey)
		}
	default:
		// Not expected
		return errors.New("unknown transaction type")
	}
	return nil
}

func writeUserData(signer *signing.Signer, publicKey *string) error {
	// User data is stored as person - role
	// NameSpace [:6] + UserSpaceAddress [:6] + Public Key [:58]
	// Request is sent as USER,<action>,<role> -> payload to be sent
	payload := constants.UserType + "," + *cli.OperationAction + "," + *cli.OperationRole
	// Compose what to be sent
	err := sendBatchToBeSent(publicKey, signer, payload, constants.BatchesRestPath)
	if err != nil {
		return errors.New("unable to send the request")
	}
	return nil
}

func readContentData(signer *signing.Signer, publicKey *string) error {
	// TODO: Send both GET and POST methods, POST for logging at TP and GET for display
	// Content data is stored as contentID - role - contentInfo
	// NameSpace [:6] + ContentSpaceAddress [:6] + Content ID [:58]
	// Request is sent as CONTENT,<action>,<id>
	payload := constants.ContentType + "," + *cli.OperationAction + "," + *cli.OperationId
	// Compose what to be sent
	err := sendBatchToBeSent(publicKey, signer, payload, constants.StateRestPath)
	if err != nil {
		return errors.New("unable to send the request")
	}
	return nil
}

func writeContentData(signer *signing.Signer, publicKey *string) error {
	// Content data is stored as contentID - role - contentInfo
	// NameSpace [:6] + ContentSpaceAddress [:6] + Content ID [:58]
	// Request is sent as CONTENT,<action>,<role>,<id>,<content>
	payload := constants.ContentType + "," + *cli.OperationAction + "," + *cli.OperationRole + "," + *cli.OperationId + "," + *cli.OperationContent
	// Compose what to be sent
	err := sendBatchToBeSent(publicKey, signer, payload, constants.BatchesRestPath)
	if err != nil {
		return errors.New("unable to send the request")
	}
	return nil
}

func sendBatchToBeSent(publicKey *string, signer *signing.Signer, payload string, path string) error {
	var partialAddress string
	var addressList []string
	// If it's read then we need complete address
	if path == constants.StateRestPath {
		partialAddress = hashValue(constants.TransactionFamilyName)[:constants.NameSpaceAddressLength] + hashValue(constants.ContentType)[:constants.OperationTypeAddressLength] + hashValue(*cli.OperationId)[:constants.ContentAddressLength]
		userAddress := hashValue(constants.TransactionFamilyName)[:constants.NameSpaceAddressLength] + hashValue(constants.UserType)[:constants.OperationTypeAddressLength]
		addressList = []string{partialAddress, userAddress}
	} else {
		// Address to be read from the client
		partialAddress = hashValue(constants.TransactionFamilyName)[:constants.NamespaceAddressLength]
		addressList = []string{partialAddress}
	}

	// Encode the payload into base64 format
	payloadEncoded := base64.StdEncoding.EncodeToString([]byte(payload))

	// Compose TransactionHeader and get it in []byte
	// Note: Please use proto.Marshal() to get the serialized representation of protobuf message, do not use json.Marshal()
	// Sawtooth expects protobuf be serialized, not json be serialized
	transactionHeader, err := proto2.Marshal(&transaction_pb2.TransactionHeader{
		SignerPublicKey: *publicKey,
		FamilyName:      constants.TransactionFamilyName,
		FamilyVersion:   constants.TrnsactionFamilyVersion,
		Dependencies:    []string{},
		// fill out random number as nonce
		Nonce:            strconv.Itoa(rand.Int()),
		BatcherPublicKey: *publicKey,
		Inputs:           addressList,
		Outputs:          addressList,
		PayloadSha512:    hashValue(payloadEncoded),
	})
	// Following traces for debugging purpose
	//fmt.Println("Transaction Header: ", transactionHeader, string(transactionHeader))
	if err != nil {
		return errors.New("unable to construct transaction header")
	}
	//fmt.Println("Payload: ", payloadEncoded)

	// Sign the transaction header
	headerSigned := hex.EncodeToString(signer.Sign(transactionHeader))
	// Get the Transaction to be sent
	transaction := transaction_pb2.Transaction{
		Header:          transactionHeader,
		HeaderSignature: headerSigned,
		Payload:         []byte(payloadEncoded),
	}

	// Get the batch header, get []byte of this protobuf message
	batchHeader, err := proto2.Marshal(&batch_pb2.BatchHeader{
		SignerPublicKey: *publicKey,
		TransactionIds:  []string{transaction.HeaderSignature},
	})
	// Following traces for debugging purpose
	//fmt.Println("Batch Header: ", batchHeader, string(batchHeader))
	if err != nil {
		return errors.New("unable to construst transaction")
	}

	// Get the batch header signed
	batchHeaderSigned := hex.EncodeToString(signer.Sign(batchHeader))

	// Compose list of transactions
	transactionList := []*transaction_pb2.Transaction{&transaction}

	// Get the batch from list of transactions and batch header, signature of it
	batch := batch_pb2.Batch{
		Header:          batchHeader,
		Transactions:    transactionList,
		HeaderSignature: batchHeaderSigned,
	}

	// Compose batch list, get []byte from proto.Marshal()
	batchList, err := proto2.Marshal(&batch_pb2.BatchList{
		Batches: []*batch_pb2.Batch{&batch},
	})
	if err != nil {
		return errors.New("unable to construct batchlist")
	}
	// Debug trace
	//fmt.Println("Batch List: ", batchList, string(batchList))

	if path == constants.StateRestPath {
		err := readData(path, partialAddress)
		if err != nil {
			return errors.New("unable to read")
		}
		path = constants.BatchesRestPath
	}
	return sendDataToTP(batchList, path)
}

func readData(path string, address string) error {
	// Get URL from environment variable
	url := os.Getenv(constants.ValidatorUrl) + "/" + path
	result, err := http.Get(url + "/" + address)
	if err != nil {
		fmt.Println("Read Error: ", err)
		return errors.New("unable to perform read operation")
	}
	defer result.Body.Close()
	resultString, err := ioutil.ReadAll(result.Body)
	if err != nil {
		fmt.Println("you are out of luck!")
		return errors.New("unable to read response")
	}
	fmt.Println("Result: ", string(resultString))
	var toPrintResponse globals.HttpResponse
	json.Unmarshal(resultString, &toPrintResponse)
	toPrint, err := base64.StdEncoding.DecodeString(toPrintResponse.Data)
	if err != nil {
		return errors.New("unable to read data from response")
	}
	fmt.Println("Read contents: ", string(toPrint))
	return nil
}

func sendDataToTP(bytes []byte, path string) error {
	// Get URL from environment variable
	url := os.Getenv(constants.ValidatorUrl) + "/" + path
	response, err := http.Post(url, "application/octet-stream", bytes2.NewBuffer(bytes))
	if err != nil {
		fmt.Println("Post Error: ", err)
		return errors.New("unable to post the operation")
	}
	defer response.Body.Close()
	resultString, err := ioutil.ReadAll(response.Body)
	if err != nil {
		fmt.Println("you are out of luck!")
	}
	fmt.Println("Result: ", string(resultString))
	return nil
}

func hashValue(value string) string {
	hashHandler := sha512.New()
	hashHandler.Write([]byte(value))
	return strings.ToLower(hex.EncodeToString(hashHandler.Sum(nil)))
}
