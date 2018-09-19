package com.intel.sample.contentprotection.process;

import com.google.protobuf.ByteString;
import com.intel.sample.contentprotection.constants.AddressLength;
import com.intel.sample.contentprotection.constants.ContentProtectionUserTable;
import com.intel.sample.contentprotection.exceptions.ErrorScenario;
import com.intel.sample.contentprotection.exceptions.InternalServerException;
import com.intel.sample.contentprotection.exceptions.InvalidOperationException;
import com.intel.sample.contentprotection.models.ContentProtectionAction;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionRequest;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionType;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;

import java.util.*;

public class UserTransactionHandler implements ContentProtectionUnpackedTransactionHandler {

  private final String userSpaceAddress;
  private final String nameSpaceAddress;

  public UserTransactionHandler(String nameSpaceAddress) {
    this.nameSpaceAddress = nameSpaceAddress;
    this.userSpaceAddress =
        NameSpaceUtils.calculateNameSpace(
            ContentProtectionTransactionType.USER.getEntity(),
            AddressLength.transactionTypeAddressLength);
  }

  /** @return the nameSpaceAddress */
  private String getNameSpaceAddress() {
    return nameSpaceAddress;
  }

  /** @return the userSpaceAddress */
  private String getUserSpaceAddress() {
    return userSpaceAddress;
  }

  @Override
  public void processTransaction(
      String requester, ContentProtectionTransactionRequest request, State state)
      throws InternalError, InvalidTransactionException {

    // Final address for the particular user
    String address =
        getNameSpaceAddress()
            + getUserSpaceAddress()
            + NameSpaceUtils.calculateNameSpace(requester, AddressLength.transactionAddressLength);
    Map<String, ByteString> users = state.getState(Collections.singletonList(address));
    // If it's register then store user with the role information
    // requester - role
    if (request.getContentProtectionAction() == ContentProtectionAction.REGISTER) {
      if (users.containsKey(address) && !Objects.equals(users.get(address).toStringUtf8(), "")) {
        throw new InvalidOperationException(
            ErrorScenario.ILLEGAL_OPERATION, users.get(address).toStringUtf8());
      }
    }
    // If it's update then store user information with the updated role
    // Note: User entry must be present before proceeding here
    // requester - updated role
    if (request.getContentProtectionAction() == ContentProtectionAction.UPDATE) {
      if ((!users.containsKey(address))
          || (users.containsKey(address)
              && Objects.equals(users.get(address).toStringUtf8(), ""))) {
        throw new InvalidOperationException(ErrorScenario.ILLEGAL_OPERATION, request.toString());
      }
      String contentsAtAddress = users.get(address).toStringUtf8();
      String storedUser =
          contentsAtAddress.split("-")[ContentProtectionUserTable.userIdentifierIndex];
      if (!storedUser.equals(requester)) {
        throw new InvalidOperationException(
            ErrorScenario.ILLEGAL_OPERATION, request.getContentProtectionRole().name());
      }
    }
    // state to be stored into Global State Store
    String stateToStore = requester + "-" + request.getContentProtectionRole();
    ByteString byteStringStateToBeStored = ByteString.copyFromUtf8(stateToStore);
    Map.Entry<String, ByteString> entry =
        new AbstractMap.SimpleEntry<>(address, byteStringStateToBeStored);
    Collection<Map.Entry<String, ByteString>> addressValues = Collections.singletonList(entry);
    Collection<String> addresses = state.setState(addressValues);
    if (addresses.size() < 1) {
      throw new InternalServerException(ErrorScenario.INTERNAL_WRITE_ERROR, request.toString());
    }
  }
}
