package com.intel.sample.contentprotection.process;

import sawtooth.sdk.processor.Utils;

import java.io.UnsupportedEncodingException;

public class NameSpaceUtils {

  public static String calculateNameSpace(String entity, int length) {
    String calculatedNameSpaceAddress = "";
    try {
      calculatedNameSpaceAddress = Utils.hash512(entity.getBytes("UTF-8")).substring(0, length);
    } catch (UnsupportedEncodingException e) {
      // TODO: Log the error, namespace would be empty
    }
    return calculatedNameSpaceAddress;
  }
}
