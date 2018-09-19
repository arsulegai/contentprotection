package com.intel.sample.contentprotection.exceptions;

public class InvalidRequestException extends RuntimeException {

  public InvalidRequestException(ErrorScenario scenario, String customDebugger) {
    super(scenario.toString() + customDebugger);
  }
}
