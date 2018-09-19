package com.intel.sample.contentprotection.models;

/**
 * This is simple POJO
 *
 * <p>This represents the format in which the transaction requests are received from the clients.
 * Typical use cases are
 */
public class ContentProtectionTransactionRequest {
  // Transaction Type - User related or Content related
  private ContentProtectionTransactionType contentProtectionTransactionType;

  // Action to be performed
  private ContentProtectionAction contentProtectionAction;

  // Role of the usern
  private ContentProtectionRole contentProtectionRole;

  // Content ID is unique and used as identifier in pool
  private String contentProtectionContentId = "";

  // Content to be protected
  private String contentProtectionContentToBeProtected = "";

  public ContentProtectionTransactionRequest(
      ContentProtectionTransactionType contentProtectionTransactionType,
      ContentProtectionAction contentProtectionAction,
      ContentProtectionRole contentProtectionRole) {
    this.contentProtectionTransactionType = contentProtectionTransactionType;
    this.contentProtectionAction = contentProtectionAction;
    this.contentProtectionRole = contentProtectionRole;
  }

  public ContentProtectionTransactionRequest(
      ContentProtectionTransactionType contentProtectionTransactionType,
      ContentProtectionAction contentProtectionAction,
      ContentProtectionRole contentProtectionRole,
      String contentProtectionContentId,
      String contentProtectionContentToBeProtected) {
    this.contentProtectionTransactionType = contentProtectionTransactionType;
    this.contentProtectionAction = contentProtectionAction;
    this.contentProtectionRole = contentProtectionRole;
    this.contentProtectionContentId = contentProtectionContentId;
    this.contentProtectionContentToBeProtected = contentProtectionContentToBeProtected;
  }

  public ContentProtectionTransactionRequest(
      ContentProtectionTransactionType contentProtectionTransactionType,
      ContentProtectionAction contentProtectionAction,
      String contentProtectionContentId) {
    this.contentProtectionTransactionType = contentProtectionTransactionType;
    this.contentProtectionAction = contentProtectionAction;
    this.contentProtectionContentId = contentProtectionContentId;
    this.contentProtectionRole = ContentProtectionRole.NONE;
  }

  public ContentProtectionAction getContentProtectionAction() {
    return contentProtectionAction;
  }

  public String getContentProtectionContentId() {
    return contentProtectionContentId;
  }

  public String getContentProtectionContentToBeProtected() {
    return contentProtectionContentToBeProtected;
  }

  public ContentProtectionRole getContentProtectionRole() {
    return contentProtectionRole;
  }

  public ContentProtectionTransactionType getContentProtectionTransactionType() {
    return contentProtectionTransactionType;
  }
}
