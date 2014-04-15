// Copyright 2014 Connectifier, Inc. All Rights Reserved.

package com.connectifier.xeroclient;

public class XeroApiException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final int responseCode;

  public XeroApiException(int responseCode) {
    super(responseCode + " response.");
    this.responseCode = responseCode;
  }

  public XeroApiException(int responseCode, String message) {
    super(responseCode + " response: " + message);
    this.responseCode = responseCode;
  }
  
  public int getResponseCode() {
    return responseCode;
  }
  
}
