package com.intel.sample.contentprotection.process;

import com.intel.sample.contentprotection.models.ContentProtectionTransactionRequest;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;

public interface ContentProtectionUnpackedTransactionHandler {

  void processTransaction(
      String requester, ContentProtectionTransactionRequest request, State state)
      throws InternalError, InvalidTransactionException;
}
