package com.thoughtworks.xstream;

import java.util.List;

import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for white-list enabled {@link XStream}.
 */
public class XStreamWhitelistTest
{
  private XStream xstream;

  @Before
  public void setUp() throws Exception {
    XStream.whitelistForce.set(true);
    xstream = new XStream();
  }

  @After
  public void tearDown() throws Exception {
    XStream.whitelistForce.remove();
  }

  @Test
  public void list_enabled() {
    Object obj = xstream.fromXML("<list/>");
    assertThat(obj, instanceOf(List.class));
  }

  @Test
  public void dynamicProxy_disabled() {
    try {
      xstream.fromXML("<dynamic-proxy/>");
      fail();
    }
    catch (Exception e) {
      // ignore
    }
  }

  @Test
  public void javaClass_disabled() {
    try {
      xstream.fromXML("<java-class>foo</java-class>");
      fail();
    }
    catch (CannotResolveClassException e) {
      assertThat(e.getMessage(), is("java-class"));
    }
  }
}
