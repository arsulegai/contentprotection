package com.intel.sample.contentprotection;

import com.intel.sample.contentprotection.constants.AddressLength;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionRequest;
import com.intel.sample.contentprotection.process.*;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.TransactionHandler;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.TpProcessRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentProtectionTransactionHandler implements TransactionHandler {

  private static final String transactionFamilyName = "Content Protection";
  private static final String transactionFamilyVersion = "1.0";
  private final String transactionFamilyNameSpaceAddress;

  // Objects that will help process the transactions
  private final ContentProtectionTransactionPackerUnpacker
      contentProtectionTransactionPackerUnpacker;
  private final ContentTransactionHandler contentTransactionHandler;
  private final UserTransactionHandler userTransactionHandler;

  ContentProtectionTransactionHandler() {
    contentProtectionTransactionPackerUnpacker = new ContentProtectionTransactionPackerUnpacker();
    /*
     * Find the hash of transactionFamilyName, it is used in this application to find namespace
     * Consider first 3 bytes / 6 hexadecimal characters
     */
    transactionFamilyNameSpaceAddress =
        NameSpaceUtils.calculateNameSpace(
            transactionFamilyName(), AddressLength.nameSpaceAddressLength);
    contentTransactionHandler =
        new ContentTransactionHandler(getTransactionFamilyNameSpaceAddress());
    userTransactionHandler = new UserTransactionHandler(getTransactionFamilyNameSpaceAddress());
  }

  /**
   * Any transaction in Sawtooth is sent to apply method. Based on passed parameters the handling
   * can be done
   */
  @Override
  public void apply(TpProcessRequest transactionRequest, State state)
      throws InvalidTransactionException, InternalError {
    // Note: Request verification too is done here
    ContentProtectionTransactionRequest unpackedTransactionRequest =
        contentProtectionTransactionPackerUnpacker.unpackTransactionRequest(transactionRequest);

    // Get to know transaction requester, identified by public key
    String requester = transactionRequest.getHeader().getSignerPublicKey();
    ContentProtectionUnpackedTransactionHandler transactionHandler = null;
    switch (unpackedTransactionRequest.getContentProtectionTransactionType()) {
      case USER:
        // Process if it's user related activity
        transactionHandler = getUserTransactionHandler();
        break;
      case CONTENT:
        // Process if it's content related activity
        transactionHandler = getContentTransactionHandler();
        break;
      default:
        // Unexpected
    }
    transactionHandler.processTransaction(requester, unpackedTransactionRequest, state);
  }

  /**
   * Sawtooth expects namespaces be given as input to Peer. This is used to structure the Merkel
   * Tree and effectively index the storage.
   */
  @Override
  public Collection<String> getNameSpaces() {
    List<String> nameSpaces = new ArrayList<>();
    nameSpaces.add(getTransactionFamilyNameSpaceAddress());
    return nameSpaces;
  }

  /**
   * Version field acts as important parameter for Sawtooth to identify if a new smart contract
   * needs to be installed on the network. Sawtooth taks care of pending or ongoing requests when a
   * newer version of smart contract is asked to be registered in network.
   */
  @Override
  public String getVersion() {
    return transactionFamilyVersion;
  }

  /**
   * Sawtooth expects each TransactionFamily have its name, this can be also used to generate the
   * unique hash which could act as namespace when storing in Merkel tree.
   */
  @Override
  public String transactionFamilyName() {
    return transactionFamilyName;
  }

  private ContentProtectionUnpackedTransactionHandler getContentTransactionHandler() {
    return contentTransactionHandler;
  }

  /** @return the transactionFamilyNameSpace */
  private String getTransactionFamilyNameSpaceAddress() {
    return transactionFamilyNameSpaceAddress;
  }

  private ContentProtectionUnpackedTransactionHandler getUserTransactionHandler() {
    return userTransactionHandler;
  }
}
