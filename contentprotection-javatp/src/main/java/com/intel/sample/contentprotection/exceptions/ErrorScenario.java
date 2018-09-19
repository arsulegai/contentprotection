package com.intel.sample.contentprotection.exceptions;

public enum ErrorScenario {

  // transaction submitted is invalid
  INVALID_TRANSACTION("Less, More parameters supplied"),
  // Submitted the transaction that is not supported
  UNSUPPORTED_TRANSACTION("Unsupported transaction requested"),
  // Trying to perform operation that is not allowed
  ILLEGAL_OPERATION("Operation not allowed"),
  // Error happened internally while writing
  INTERNAL_WRITE_ERROR("Error occurred internally! Sorry");

  private String scenario;

  ErrorScenario(String scenario) {
    this.scenario = scenario;
  }

  @Override
  public String toString() {
    return scenario;
  }
}
