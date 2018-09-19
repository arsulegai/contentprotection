package com.intel.sample.contentprotection.process;

import com.google.protobuf.ByteString;
import com.intel.sample.contentprotection.constants.AddressLength;
import com.intel.sample.contentprotection.constants.ContentProtectionContentTable;
import com.intel.sample.contentprotection.constants.ContentProtectionUserTable;
import com.intel.sample.contentprotection.exceptions.ErrorScenario;
import com.intel.sample.contentprotection.exceptions.InternalServerException;
import com.intel.sample.contentprotection.exceptions.InvalidOperationException;
import com.intel.sample.contentprotection.exceptions.InvalidRequestException;
import com.intel.sample.contentprotection.models.ContentProtectionAction;
import com.intel.sample.contentprotection.models.ContentProtectionRole;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionRequest;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionType;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;

import java.util.*;

public class ContentTransactionHandler implements ContentProtectionUnpackedTransactionHandler {

  private final String nameSpaceAddress;
  private final String contentSpaceAddress;
  private final String userSpaceAddress;

  public ContentTransactionHandler(String nameSpaceAddress) {
    this.nameSpaceAddress = nameSpaceAddress;
    this.contentSpaceAddress =
        NameSpaceUtils.calculateNameSpace(
            ContentProtectionTransactionType.CONTENT.getEntity(),
            AddressLength.transactionTypeAddressLength);
    this.userSpaceAddress =
        NameSpaceUtils.calculateNameSpace(
            ContentProtectionTransactionType.USER.getEntity(),
            AddressLength.transactionTypeAddressLength);
  }

  /** @return the contentSpaceAddress */
  private String getContentSpaceAddress() {
    return contentSpaceAddress;
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
    /*
     * Content is stored in following format: Content ID - Content Role - Content information
     */
    // Get user role information from state store, before proceeding
    String userAddress =
        getNameSpaceAddress()
            + getUserSpaceAddress()
            + NameSpaceUtils.calculateNameSpace(requester, AddressLength.transactionAddressLength);
    Map<String, ByteString> users = state.getState(Collections.singletonList(userAddress));

    if (Objects.equals(users.get(userAddress).toStringUtf8(), "")) {
      // Unknown user trying to perform transaction
      throw new InvalidOperationException(ErrorScenario.ILLEGAL_OPERATION, requester);
    }
    String userRoleString =
        users.get(userAddress).toStringUtf8().split("-")[ContentProtectionUserTable.userRoleIndex];
    ContentProtectionRole userRole = ContentProtectionRole.valueOf(userRoleString.toUpperCase());

    // If it's read operation then if role of user allows to then return the content
    if (request.getContentProtectionAction() == ContentProtectionAction.READ) {
      if (userRole == ContentProtectionRole.NONE) {
        // User is not allowed for this operation
        throw new InvalidOperationException(
            ErrorScenario.ILLEGAL_OPERATION, request.getContentProtectionAction().name());
      }
    }

    // Check if user requested data is seen in the state store
    String contentAddress =
        getNameSpaceAddress()
            + getContentSpaceAddress()
            + NameSpaceUtils.calculateNameSpace(
                request.getContentProtectionContentId(), AddressLength.transactionAddressLength);
    Map<String, ByteString> contentAddressContents =
        state.getState(Collections.singletonList(contentAddress));
    ContentProtectionRole contentRole = ContentProtectionRole.NONE;
    String contentString = "";
    if (contentAddressContents.containsKey(contentAddress)
        && !Objects.equals(contentAddressContents.get(contentAddress).toStringUtf8(), "")) {
      contentRole =
          ContentProtectionRole.valueOf(
              contentAddressContents
                  .get(contentAddress)
                  .toStringUtf8()
                  .split("-")[ContentProtectionContentTable.contentRoleIndex]
                  .toUpperCase());
      contentString =
          contentAddressContents
              .get(contentAddress)
              .toStringUtf8()
              .split("-")[ContentProtectionContentTable.contentInformationIndex];
    }
    if (userRole.isLesserThan(contentRole)) {
      // User doesn't have sufficient permission in this case
      throw new InvalidOperationException(
          ErrorScenario.ILLEGAL_OPERATION, request.getContentProtectionRole().name());
    }
    switch (request.getContentProtectionAction()) {
      case READ:
        printContents(request.getContentProtectionContentId(), contentRole, contentString);
        return;
      case UPDATE:
        if (Objects.equals(contentString, "")) {
          // Cannot be updated
          throw new InvalidOperationException(
              ErrorScenario.ILLEGAL_OPERATION, request.getContentProtectionContentToBeProtected());
        }
        break;
      case CREATE:
        if (!Objects.equals(contentString, "")) {
          // Cannot be created, already present
          throw new InvalidOperationException(
              ErrorScenario.ILLEGAL_OPERATION, request.getContentProtectionContentToBeProtected());
        }
        break;
      default:
        // Unexpected operation, should not come here
        throw new InvalidRequestException(ErrorScenario.INVALID_TRANSACTION, request.toString());
    }
    // state to be stored into Global State Store
    String stateToStore =
        request.getContentProtectionContentId()
            + "-"
            + request.getContentProtectionRole()
            + "-"
            + request.getContentProtectionContentToBeProtected();
    ByteString byteStringStateToBeStored = ByteString.copyFromUtf8(stateToStore);
    Map.Entry<String, ByteString> entry =
        new AbstractMap.SimpleEntry<>(contentAddress, byteStringStateToBeStored);
    Collection<Map.Entry<String, ByteString>> addressValues = Collections.singletonList(entry);
    Collection<String> addresses = state.setState(addressValues);
    if (addresses.size() < 1) {
      throw new InternalServerException(ErrorScenario.INTERNAL_WRITE_ERROR, request.toString());
    }
  }

  private void printContents(
      String contentId, ContentProtectionRole contentRole, String contentString) {
    System.out.println(
        "Content ID: "
            + contentId
            + "; Role: "
            + contentRole.name()
            + "; Content Protected: "
            + contentString);
  }
}
