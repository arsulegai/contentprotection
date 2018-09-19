package com.intel.sample.contentprotection.exceptions;

public class InvalidOperationException extends RuntimeException {

  public InvalidOperationException(ErrorScenario scenario, String customDebug) {
    super(scenario.toString() + customDebug);
  }
}
