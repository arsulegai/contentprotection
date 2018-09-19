package com.intel.sample.contentprotection.process;

import com.intel.sample.contentprotection.constants.TransactionRequestIndices;
import com.intel.sample.contentprotection.exceptions.ErrorScenario;
import com.intel.sample.contentprotection.exceptions.InvalidRequestException;
import com.intel.sample.contentprotection.models.ContentProtectionAction;
import com.intel.sample.contentprotection.models.ContentProtectionRole;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionRequest;
import com.intel.sample.contentprotection.models.ContentProtectionTransactionType;
import sawtooth.sdk.protobuf.TpProcessRequest;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.List;

/**
 * This is a utility class to process the request and pack or unpack the request to readable format
 */
public class ContentProtectionTransactionPackerUnpacker {

  public ContentProtectionTransactionRequest unpackTransactionRequest(
      TpProcessRequest transactionRequest) {
    List<String> receivedRequestList = parseTransactionRequest(transactionRequest);
    return convertTransactionToContentTransactionRequest(receivedRequestList);
  }

  private ContentProtectionTransactionRequest convertTransactionToContentTransactionRequest(
      List<String> receivedRequestList) {

    ContentProtectionTransactionType type =
        ContentProtectionTransactionType.valueOf(
            receivedRequestList.get(TransactionRequestIndices.TransactionTypeIndex).toUpperCase());
    ContentProtectionAction action =
        ContentProtectionAction.valueOf(
            receivedRequestList
                .get(TransactionRequestIndices.TransactionActionIndex)
                .toUpperCase());
    return receivedRequestList.size() == 3
        ? action == ContentProtectionAction.READ
            ? new ContentProtectionTransactionRequest(
                type,
                action,
                receivedRequestList.get(TransactionRequestIndices.TransactionIdReadIndex))
            : new ContentProtectionTransactionRequest(
                type,
                action,
                ContentProtectionRole.valueOf(
                    receivedRequestList
                        .get(TransactionRequestIndices.TransactionRoleIndex)
                        .toUpperCase()))
        : new ContentProtectionTransactionRequest(
            type,
            action,
            ContentProtectionRole.valueOf(
                receivedRequestList
                    .get(TransactionRequestIndices.TransactionRoleIndex)
                    .toUpperCase()),
            receivedRequestList.get(TransactionRequestIndices.TransactionIdIndex),
            receivedRequestList.get(TransactionRequestIndices.TransactionContentIndex));
  }

  private List<String> parseTransactionRequest(TpProcessRequest transactionRequest) {
    /*
     * Raw input would be as follows: Transaction - Type, Action, Role, Id, Content - if it's
     * content related Or Transaction - Type, Action, Role - if it's user related
     */
    List<String> receivedRequestList =
        Arrays.asList(
            new String(
                    DatatypeConverter.parseBase64Binary(
                        transactionRequest.getPayload().toStringUtf8()))
                .split(","));
    // Validate the input transaction request parameters
    if (receivedRequestList.size() < 3
        || receivedRequestList.size() > 5
        || receivedRequestList.size() == 4) {
      throw new InvalidRequestException(
          ErrorScenario.INVALID_TRANSACTION, transactionRequest.getPayload().toStringUtf8());
    }
    try {
      // If a read request then only Id is input, role and content is not input
      if (ContentProtectionAction.valueOf(
              receivedRequestList.get(TransactionRequestIndices.TransactionActionIndex))
          == ContentProtectionAction.READ) {
        if (receivedRequestList.size() > 3) {
          throw new InvalidRequestException(
              ErrorScenario.INVALID_TRANSACTION, transactionRequest.getPayload().toStringUtf8());
        }
      }
    } catch (Exception e) {
      // TODO: log the value passed which made it invalid to parse
      if (e.getClass().getSimpleName().equals(InvalidRequestException.class.getSimpleName())) {
        throw e;
      }
    }
    // Validate first three parameters of the request
    if (Arrays.stream(ContentProtectionTransactionType.values())
            .noneMatch(
                knownType ->
                    knownType
                        .toString()
                        .equals(
                            receivedRequestList
                                .get(TransactionRequestIndices.TransactionTypeIndex)
                                .toUpperCase()))
        || Arrays.stream(ContentProtectionAction.values())
            .noneMatch(
                knownAction ->
                    knownAction
                        .toString()
                        .equals(
                            receivedRequestList
                                .get(TransactionRequestIndices.TransactionActionIndex)
                                .toUpperCase()))
        || ((ContentProtectionAction.valueOf(
                    receivedRequestList
                        .get(TransactionRequestIndices.TransactionActionIndex)
                        .toUpperCase())
                != ContentProtectionAction.READ)
            && Arrays.stream(ContentProtectionRole.values())
                .noneMatch(
                    knownRole ->
                        knownRole
                            .toString()
                            .equals(
                                receivedRequestList
                                    .get(TransactionRequestIndices.TransactionRoleIndex)
                                    .toUpperCase())))) {
      throw new InvalidRequestException(
          ErrorScenario.UNSUPPORTED_TRANSACTION, receivedRequestList.toString());
    }
    try {
      // If transaction type is user related, then there should not be content information
      // Moreover only register and update operations are allowed
      // If type is content related, register action cannot be done
      if ((ContentProtectionTransactionType.valueOf(
                      receivedRequestList
                          .get(TransactionRequestIndices.TransactionTypeIndex)
                          .toUpperCase())
                  == ContentProtectionTransactionType.USER
              && !Arrays.asList(ContentProtectionAction.REGISTER, ContentProtectionAction.UPDATE)
                  .contains(
                      ContentProtectionAction.valueOf(
                          receivedRequestList
                              .get(TransactionRequestIndices.TransactionActionIndex)
                              .toUpperCase())))
          || (ContentProtectionTransactionType.valueOf(
                      receivedRequestList
                          .get(TransactionRequestIndices.TransactionTypeIndex)
                          .toUpperCase())
                  == ContentProtectionTransactionType.CONTENT
              && ContentProtectionAction.valueOf(
                      receivedRequestList
                          .get(TransactionRequestIndices.TransactionActionIndex)
                          .toUpperCase())
                  == ContentProtectionAction.REGISTER)) {
        throw new InvalidRequestException(
            ErrorScenario.UNSUPPORTED_TRANSACTION, receivedRequestList.toString());
      }
    } catch (Exception e) {
      // TODO: log the value passed which made it invalid to parse
      if (e.getClass().getSimpleName().equals(InvalidRequestException.class.getSimpleName())) {
        throw e;
      }
    }
    return receivedRequestList;
  }
}
