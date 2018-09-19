package cli

import (
	"constants"
	"errors"
	"flag"
	"fmt"
	"globals"
	"os"
	"path"
)

// The flag package provides a default help printer via -h switch
var (
	versionFlag      = flag.Bool("v", false, "Print the version number.")
	OperationUser    = flag.String("user", "", "Enter the username registered with the application")
	OperationType    = flag.String("type", "", "Enter either 'USER' or 'CONTENT'")
	OperationAction  = flag.String("action", "", "'REGISTER' or 'UPDATE' for -type=USER, 'READ', 'CREATE' and 'UPDATE' for -type=CONTENT")
	OperationRole    = flag.String("role", "", "Role of the User for which content is crated, also indicates role of User when registered")
	OperationId      = flag.String("id", "", "Identifier for the content")
	OperationContent = flag.String("content", "", "Content to be protected")
)

func ParseFlags() error {
	flag.Parse() // Scan the arguments list

	if *versionFlag {
		fmt.Println("Version:", constants.TrnsactionFamilyVersion)
	}

	// Validate user input field
	if err := validateUser(); err != nil {
		return err
	}
	// Validate type input field
	if err := validateType(); err != nil {
		return err
	}
	// Validate action input field
	if err := validateAction(); err != nil {
		return err
	}
	// Validate role input field
	if err := validateRole(); err != nil {
		return err
	}
	if err := validateId(); err != nil {
		return err
	}
	if err := validateContent(); err != nil {
		return err
	}
	return nil
}

func validateUser() error {
	// Validate input from command line, if no user then nothing to perform
	if len(*OperationUser) == 0 {
		return errors.New("username (-user) must be specified")
	}
	// Check if public key corresponding to the user is present in the location
	globals.PublicKeyFilePath = path.Join(os.Getenv("HOME"), ".sawtooth", "keys", *OperationUser+".pub")
	if _, err := os.Stat(globals.PublicKeyFilePath); err != nil {
		return errors.New("user doesn't have a valid public key file present")
	}
	globals.PrivateKeyFilePath = path.Join(os.Getenv("HOME"), ".sawtooth", "keys", *OperationUser+".priv")
	if _, err := os.Stat(globals.PrivateKeyFilePath); err != nil {
		return errors.New("user doesn't have a valid private key file present")
	}
	return nil
}

func validateType() error {
	// Validate type field from command line, it should be one of 'USER' and 'CONTENT'
	if len(*OperationType) == 0 {
		return errors.New("transaction type (-type) cannot be empty")
	}
	if !(*OperationType == constants.UserType || *OperationType == constants.ContentType) {
		return errors.New("-type can be one of USER or CONTENT")
	}
	return nil
}

func validateAction() error {
	// Validate action field from command line, it can be one of REGISTER, CREATE, UPDATE, READ
	if len(*OperationAction) == 0 {
		return errors.New("action (-action) cannot be empty")
	}
	if !(*OperationAction == constants.RegisterAction || *OperationAction == constants.CreateAction || *OperationAction == constants.UpdateAction || *OperationAction == constants.ReadAction) {
		return errors.New("-action can be one of REGISTER, CREATE, UPDATE or READ")
	}
	// If type is user then action can be one of REGISTER or CREATE
	if *OperationType == constants.UserType && !(*OperationAction == constants.RegisterAction || *OperationAction == constants.UpdateAction) {
		return errors.New("-type=USER can only perform -action=REGISTER or -action=UPDATE")
	}
	// If type is content then action cannot be REGISTER
	if *OperationType == constants.ContentType && *OperationAction == constants.RegisterAction {
		return errors.New("-type=USER cannot have -action=REGISTER")
	}
	return nil
}

func validateRole() error {
	// Validate role field from command line, it can be one of NONE, LEVEL1, LEVEL2, ADMIN
	if len(*OperationRole) == 0 && *OperationAction != constants.ReadAction {
		return errors.New("role (-role) cannot be empty unless it's -action=READ")
	}
	if !(*OperationRole == constants.NoneRole || *OperationRole == constants.Level1Role || *OperationRole == constants.Level2Role || *OperationRole == constants.AdminRole) && *OperationAction != constants.ReadAction {
		return errors.New("-role can be one of NONE, LEVEL1, LEVEL2, ADMIN")
	}
	return nil
}

func validateId() error {
	// Validate Id field from command line, it cannot be emoty unless it's USER type transaction
	if len(*OperationId) == 0 && *OperationType == constants.ContentType {
		return errors.New("ID (-id) cannot be empty unless it's -type=USER")
	}
	return nil
}

func validateContent() error {
	// Validate content field from command line, it cannot be empty for CONTENT type unless it's READ action
	if len(*OperationContent) == 0 && *OperationType == constants.ContentType && *OperationAction != constants.ReadAction {
		return errors.New("need some content to be protected when -type=CONTENT and -action is not READ")
	}
	return nil
}
