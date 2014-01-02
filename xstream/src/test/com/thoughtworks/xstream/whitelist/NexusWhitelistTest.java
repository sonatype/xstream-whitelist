package com.thoughtworks.xstream.whitelist;

import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.test.AnotherDTO;
import org.sonatype.test.TypeDto;
import org.sonatype.test.TypeRequest;
import org.sonatype.test.TypeResponse;

import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test of white-list as expected inside of NX.
 */
public class NexusWhitelistTest
{
  private XStream plain;

  private XStream underTest;

  @Before
  public void setUp() throws Exception {
    this.plain = new XStream();
    this.plain.setClassLoader(getClass().getClassLoader());
    this.underTest = PlexusRestletApplicationBridge.createXstream(getClass().getClassLoader());
  }

  public static class TypeInvalid
  {
    // empty
  }

  @Test
  public void invalidType() {
    String xml = plain.toXML(new TypeInvalid());
    try {
      underTest.fromXML(xml);
      fail();
    }
    catch (Exception e) {
      // expected
    }
  }

  @Test
  public void dtoType() {
    String xml = plain.toXML(new TypeDto());
    Object obj = underTest.fromXML(xml);
    assertThat(obj, notNullValue());
    assertThat(obj, instanceOf(TypeDto.class));
  }

  @Test
  public void anotherDTO() {
    String xml = plain.toXML(new AnotherDTO());
    Object obj = underTest.fromXML(xml);
    assertThat(obj, notNullValue());
    assertThat(obj, instanceOf(AnotherDTO.class));
  }

  @Test
  public void requestType() {
    String xml = plain.toXML(new TypeRequest());
    Object obj = underTest.fromXML(xml);
    assertThat(obj, notNullValue());
    assertThat(obj, instanceOf(TypeRequest.class));
  }

  @Test
  public void responseType() {
    String xml = plain.toXML(new TypeResponse());
    Object obj = underTest.fromXML(xml);
    assertThat(obj, notNullValue());
    assertThat(obj, instanceOf(TypeResponse.class));
  }
}
