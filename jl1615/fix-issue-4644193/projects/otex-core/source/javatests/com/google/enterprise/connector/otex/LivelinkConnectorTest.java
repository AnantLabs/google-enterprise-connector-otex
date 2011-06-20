// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.otex;

import com.google.enterprise.connector.otex.ConfigurationException;
import com.google.enterprise.connector.otex.client.mock.MockClientFactory;
import com.google.enterprise.connector.spi.RepositoryException;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Date;

public class LivelinkConnectorTest extends TestCase {
  private LivelinkConnector connector;

  protected void setUp() {
    connector = new LivelinkConnector(
        "com.google.enterprise.connector.otex.client.mock.MockClientFactory");
    connector.setServer(System.getProperty("connector.server"));
    connector.setPort(System.getProperty("connector.port"));
    connector.setUsername(System.getProperty("connector.username"));
    connector.setPassword(System.getProperty("connector.password"));

    connector.setShowHiddenItems("true");
  }

  public void testSanitizingListsOfIntegers() {
    String[] values = {
      "",         "",
      "  ",       "",
      " \t ",     "",
      "\t",       "",
      "168",      "168",
      "1,2,3",            "1,2,3",
      " 1 , 2 , 3 ",      "1,2,3",
      "1\t,2,,,3",        null,
      "1,2,3) union",     null,
      "1,2,3)",           null,
      "1,2,3,\r\n4,5,6",  "1,2,3,4,5,6",
      "1,2,3\r\n4,5,6",   null,
      "1,a,b",            null,
      "1,,2,3",           null,
      "123, 456, 789",    "123,456,789",
      "123, 456, 789,",   null,
      ",123,456,789",     null,
      "{1,2,3}",          "1,2,3",
      "\t{ 1, 2,3}",     "1,2,3",
      "\t{ 1, 2,3",      "1,2,3",
      "1,2,3}",           "1,2,3",
      "{ 1 }",            "1",
      "} 1 }",            null,
      "} 1 {",            null,
      "{ 1 {",            null,
    };

    for (int i = 0; i < values.length; i += 2) {
      try {
        String output = LivelinkConnector.sanitizeListOfIntegers(values[i]);
        assertEquals(values[i], values[i + 1], output);
      } catch (IllegalArgumentException e) {
        assertNull(e.toString(), values[i + 1]);
      }
    }
  }

  public void testSanitizingListsOfStrings() {
    String[] values = {
      "",         "",
      "  ",       "",
      " \t ",     "",
      "\t",      "",
      "168",     "168",
      "a,b,c",            "a,b,c",
      " a , b , c ",      "a,b,c",
      "a\t,2,,,-",        "a,2,,,-",
      "1,2,3) union",     "1,2,3) union",
      "1,2,3)",           "1,2,3)",
      "1,2,3,\r\n4,5,6",  "1,2,3,4,5,6",
      "1,2,3\r\n4,5,6",   "1,2,3\r\n4,5,6",
      "1,a,b",            "1,a,b",
      "1,,2,3",           "1,,2,3",
      "123, 456, 789",    "123,456,789",
      "123, 456, 789,",   "123,456,789,",
      ",123,456,789",     ",123,456,789",
      "{1,2,3}",          "1,2,3",
      "\t{ 1, 2,3}",      "1,2,3",
      "\t{ 1, 2,3",       "1,2,3",
      "1,2,3}",           "1,2,3",
      "{ 1 }",            "1",
      "} 1 }",            null,
      "} 1 {",            null,
      "{ 1 {",            null,
    };

    for (int i = 0; i < values.length; i += 2) {
      try {
        String output = LivelinkConnector.sanitizeListOfStrings(values[i]);
        assertEquals(values[i], values[i + 1], output);
      } catch (IllegalArgumentException e) {
        assertNull(e.toString(), values[i + 1]);
      }
    }
  }

  public void testStartDate1() throws Exception {
    connector.setStartDate("2007-09-27 01:12:13");
    connector.login();
    Date startDate = connector.getStartDate();
    assertNotNull(startDate);
    assertEquals("Thu Sep 27 01:12:13 PDT 2007", startDate.toString());
  }

  public void testStartDate2() throws Exception {
    connector.setStartDate("2007-09-27");
    connector.login();
    Date startDate = connector.getStartDate();
    assertNotNull(startDate);
    assertEquals("Thu Sep 27 00:00:00 PDT 2007", startDate.toString());
  }

  public void testStartDate3() throws Exception {
    connector.setStartDate("");
    connector.login();
    assertEquals(null, connector.getStartDate());
  }

  public void testStartDate4() throws Exception {
    connector.setStartDate("   ");
    connector.login();
    assertEquals(null, connector.getStartDate());
  }

  public void testStartDate5() throws Exception {
    connector.setStartDate("Sep 27, 2007");
    connector.login();
    assertEquals(null, connector.getStartDate());
  }

  public void testStartDate6() throws Exception {
    connector.setStartDate("2007-09-27 01:12:13");
    connector.login();
    Date startDate = connector.getStartDate();
    assertNotNull(startDate);
    assertEquals("Thu Sep 27 01:12:13 PDT 2007", startDate.toString());
    connector.login();
    startDate = connector.getStartDate();
    assertNotNull(startDate);
    assertEquals("Thu Sep 27 01:12:13 PDT 2007", startDate.toString());
  }

  public void testStartDate7() throws Exception {
    connector.setStartDate("Sep 27, 2007");
    connector.login();
    assertEquals(null, connector.getStartDate());
    connector.login();
    assertEquals(null, connector.getStartDate());
  }

  /**
   * A simple test that verifies that at least one of the connecion
   * properties is being correctly assigned in the client factory.
   */
  public void testServer() throws RepositoryException {
    connector.login();
    MockClientFactory clientFactory =
        (MockClientFactory) connector.getClientFactory();
    Map values = clientFactory.getValues();
    assertTrue(values.toString(), values.containsKey("setServer"));
    assertEquals(System.getProperty("connector.server"),
        values.get("setServer"));
  }

  /**
   * Tests enableNtlm, which requires httpUsername and httpPassword.
   */
  public void testEnableNtlmGood() throws RepositoryException {
    connector.setUseHttpTunneling(true);
    connector.setEnableNtlm(true);
    connector.setHttpUsername("username");
    connector.setHttpPassword("password");
    connector.login();
  }

  /**
   * Tests enableNtlm, which requires httpUsername and httpPassword.
   */
  public void testEnableNtlmBad() throws RepositoryException {
    connector.setUseHttpTunneling(true);
    connector.setEnableNtlm(true);
    try {
      connector.login();
      fail("Expected an exception");
    } catch (ConfigurationException e) {
    }
  }

  /**
   * Tests enableNtlm, which requires httpUsername and httpPassword.
   */
  public void testEnableNtlmIgnored() throws RepositoryException {
    connector.setUseHttpTunneling(false);
    connector.setEnableNtlm(true);
    connector.login();
  }

  /**
   * Tests the check for System Administration rights.
   */
  public void testNoAdminRights() throws RepositoryException {
    // This value can be anything other than "Admin".
    connector.setUsername("llglobal");
    try {
      connector.login();
      fail("Expected an exception");
    } catch (ConfigurationException e) {
    }
  }

  /** Tests showHiddenItems with an invalid null value. */
  public void testGetHiddenItemsSubtypes_null() {
    try {
      LivelinkConnector.getHiddenItemsSubtypes(null);
      fail("Expected a NullPointerException.");
    } catch (NullPointerException e) {
    }
  }

  /** Tests showHiddenItems with empty or false values. */
  public void testGetHiddenItemsSubtypes_empty() {
    HashSet<Object> expected = new HashSet<Object>();
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes(""));
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes(" "));
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes("\t"));
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes("false"));
    assertEquals(expected,
        LivelinkConnector.getHiddenItemsSubtypes("\tfaLsE  "));
  }

  /** Tests showHiddenItems with "all" or true values. */
  public void testGetHiddenItemsSubtypes_all() {
    HashSet<Object> expected = new HashSet<Object>();
    expected.add("all");
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes("All"));
  }

  /** Tests showHiddenItems with numeric subtype values. */
  public void testGetHiddenItemsSubtypes_subtypes() {
    HashSet<Object> expected = new HashSet<Object>();
    expected.add(new Integer(42));
    expected.add(new Integer(1729));
    assertEquals(expected,
        LivelinkConnector.getHiddenItemsSubtypes("42,1729"));
    assertEquals(expected,
        LivelinkConnector.getHiddenItemsSubtypes("1729,42"));
  }

  /** Tests showHiddenItems with "all" plus numeric subtype values. */
  public void testGetHiddenItemsSubtypes_allPlusSubtype() {
    HashSet<Object> expected = new HashSet<Object>();
    expected.add("all");
    expected.add(new Integer(42));
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes("All,42"));
    assertEquals(expected, LivelinkConnector.getHiddenItemsSubtypes("42,All"));
  }

  /** Tests showHiddenItems with other non-numeric values. */
  public void testGetHiddenItemsSubtypes_invalid() {
    // FIXME: Why is this allowed?
    HashSet<Object> expected = new HashSet<Object>();
    expected.add("invalid");
    assertEquals(expected,
        LivelinkConnector.getHiddenItemsSubtypes("Invalid"));
  }

  /** Tests showHiddenItems = false with useDTreeAncestors = true. */
  public void testShowHiddenItems_useDTreeAncestorsTrue()
      throws RepositoryException {
    connector.setShowHiddenItems("false");
    connector.setUseDTreeAncestors(true);
    connector.login();
  }

  /** Tests showHiddenItems = false with useDTreeAncestors = false. */
  public void testShowHiddenItems_useDTreeAncestorsFalseOK()
      throws RepositoryException {
    connector.setShowHiddenItems("true");
    connector.setUseDTreeAncestors(false);
    connector.login();
  }

  /** Tests showHiddenItems = false with useDTreeAncestors = false. */
  public void testShowHiddenItems_useDTreeAncestorsFalseError()
      throws RepositoryException {
    connector.setShowHiddenItems("false");
    connector.setUseDTreeAncestors(false);
    try {
      connector.login();
      fail("Expected an exception");
    } catch (ConfigurationException e) {
    }
  }
}
