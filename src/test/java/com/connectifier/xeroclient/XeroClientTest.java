// Copyright 2014 Connectifier, Inc. All Rights Reserved.

package com.connectifier.xeroclient;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class XeroClientTest {

  @Test
  public void testDateFormatting() {
    Date date = new Date(1396719320939l);
    Assert.assertEquals("2014-04-05T17:35:20", XeroClient.utcFormatter.format(date));
  }

}
