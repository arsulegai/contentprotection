package com.intel.sample.contentprotection.exceptions;

public class InternalServerException extends RuntimeException {

  public InternalServerException(ErrorScenario scenario, String customDebug) {
    super(scenario.toString() + customDebug);
  }
}
