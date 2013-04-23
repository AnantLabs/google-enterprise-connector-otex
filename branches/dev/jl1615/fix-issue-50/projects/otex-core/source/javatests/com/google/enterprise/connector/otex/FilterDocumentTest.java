// Copyright 2011 Google Inc.
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

import junit.framework.TestCase;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mostly useful for other FilterDocument tests to subclass.
 */
/* TODO (bmj): This should really be in the Connctor Manager. */
public class FilterDocumentTest extends TestCase {

  protected static final String PROP1 = "property1";
  protected static final String PROP2 = "property2";
  protected static final String PROP3 = "property3";
  protected static final String PROP4 = "property4";
  protected static final String PROP5 = "property5";
  protected static final String PROP6 = "property6";
  protected static final String TEST_STRING = "The_.quick_brown_fox.jumped.over";
  protected static final String CLEAN_STRING = "The quick brown fox jumped over";
  protected static final String TEST_EXTRA_STRING = "lazy.dog's_back";
  protected static final String EXTRA_STRING = "lazy dog's back";
  protected static final String PATTERN = "[_\\.]+";
  protected static final String SPACE = " ";

  /** Creates a source Document. */
  protected static Document createDocument() {
    return new SimpleDocument(createProperties());
  }

  protected static Map<String, List<Value>> createProperties() {
    Map<String, List<Value>> props = new HashMap<String, List<Value>>();
    props.put(PROP1, valueList(TEST_STRING));
    props.put(PROP2, valueList(CLEAN_STRING));
    props.put(PROP3, valueList(TEST_STRING, EXTRA_STRING));
    props.put(PROP4, valueList(CLEAN_STRING, EXTRA_STRING));
    props.put(PROP5, valueList(CLEAN_STRING, TEST_EXTRA_STRING));
    props.put(PROP6, valueList(TEST_STRING, TEST_EXTRA_STRING));
    return props;
  }

  protected static List<Value> valueList(String... values) {
    LinkedList<Value> list = new LinkedList<Value>();
    for (String value : values) {
      list.add(Value.getStringValue(value));
    }
    return list;
  }

  /** Checks that the Document Properties match the expected Properties. */
  protected static void checkDocument(Document document,
      Map<String, List<Value>> expectedProps) throws Exception {
    assertEquals(expectedProps.keySet(), document.getPropertyNames());
    for (Map.Entry<String, List<Value>> entry : expectedProps.entrySet()) {
      Property prop = document.findProperty(entry.getKey());
      assertNotNull(prop);
      for (Value expectedValue : entry.getValue()) {
        Value value = prop.nextValue();
        assertNotNull(value);
        assertEquals(expectedValue.toString(), value.toString());
      }
      assertNull(prop.nextValue());
    }
  }

  /** Test createDocument. */
  public void testCreateDocument() throws Exception {
    checkDocument(createDocument(), createProperties());
  }
}