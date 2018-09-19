package com.intel.sample.contentprotection.models;

/** Different actions performed when using the Transaction Processor */
public enum ContentProtectionAction {

  // Register the user with the Transaction Processor / system
  REGISTER,
  // Create the content
  CREATE,
  // Update the content
  UPDATE,
  // Read the content
  READ
}
