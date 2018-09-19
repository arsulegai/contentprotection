package com.intel.sample.contentprotection.models;

/**
 * Transaction type is either user or content. If it's user addition user can be registered as of
 * now.
 */
public enum ContentProtectionTransactionType {

  // Transaction Type can be user related
  USER("USER"),
  // Transaction Type can be content related
  CONTENT("CONTENT");

  private String entity;

  private ContentProtectionTransactionType(String entity) {
    this.entity = entity;
  }

  public String getEntity() {
    return entity;
  }
}
