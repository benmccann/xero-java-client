// Copyright 2014 Connectifier, Inc. All Rights Reserved.

package com.connectifier.xeroclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.connectifier.xeroclient.models.ResponseType;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class XeroClientTest {

  @Test
  public void testDateFormatting() {
    Date date = new Date(1396719320939l);
    Assert.assertEquals("2014-04-05T17:35:20", XeroClient.utcFormatter.format(date));
  }

  @Test
  public void testUnmarshalling_createInvoiceResponse() {
    String xml = getResourceAsString("create-invoice-response.xml");
    ResponseType response = XeroClient.unmarshallResponse(xml, ResponseType.class);
    Assert.assertNotNull(response);
  }

  private String getResourceAsString(String resource) {
    try (InputStream is = XeroClientTest.class.getClassLoader().getResourceAsStream(resource)) {
      return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalArgumentException("Cound not read resource " + resource, e);
    }
  }

}
