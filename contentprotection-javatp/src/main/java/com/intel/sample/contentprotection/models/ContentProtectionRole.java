package com.intel.sample.contentprotection.models;

/**
 * Different roles involved in Content Protection Transaction Processor. For sample use case we have
 * admin, level1, level2, none
 */
public enum ContentProtectionRole {

  // none - have no access to the contents
  NONE(0),
  // level1 - have access to first level of information
  LEVEL1(1),
  // level2 - have access to second level of information
  LEVEL2(2),
  // admin - has access to all information
  ADMIN(3);

  private int value;

  private ContentProtectionRole(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public boolean isLesserThan(ContentProtectionRole secondRole) {
    return this.getValue() < secondRole.getValue();
  }
}
