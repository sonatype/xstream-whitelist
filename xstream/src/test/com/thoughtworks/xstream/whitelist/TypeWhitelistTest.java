package com.thoughtworks.xstream.whitelist;

import java.util.Set;

import com.thoughtworks.xstream.whitelist.TypeWhitelist.TypeNotAllowedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link TypeWhitelist}.
 */
public class TypeWhitelistTest
{
  private TypeWhitelist underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new TypeWhitelist();
  }

  @After
  public void tearDown() throws Exception {
    TypeWhitelist.allowedTypesProperty.remove();
    TypeWhitelist.allowedPackagesProperty.remove();
  }

  @Test
  public void propertyConfiguration() {
    TypeWhitelist.allowedTypesProperty.set("test.Foo,test.Bar");
    TypeWhitelist.allowedPackagesProperty.set("test2,test3");
    TypeWhitelist.allowedPatternsProperty.set("org\\.sonatype\\..*DTO");

    // re-construct to pickup configuration
    underTest = new TypeWhitelist();

    assertThat(underTest.isAllowed("test.Foo"), is(true));
    assertThat(underTest.isAllowed("test.Bar"), is(true));
    assertThat(underTest.isAllowed("test2.A"), is(true));
    assertThat(underTest.isAllowed("test2.B"), is(true));
    assertThat(underTest.isAllowed("test3.A"), is(true));
    assertThat(underTest.isAllowed("test3.C"), is(true));
    assertThat(underTest.isAllowed("org.sonatype.foo.FooDTO"), is(true));

    assertThat(underTest.isAllowed("test.Baz"), is(false));
    assertThat(underTest.isAllowed("test4.A"), is(false));
    assertThat(underTest.isAllowed("org.sonatype.foo.Foo"), is(false));
  }

  @Test
  public void allowAll() {
    // default should be false
    assertThat(underTest.isAllowAll(), is(false));

    TypeWhitelist.allowAllProperty.set(true);

    // re-construct to pickup configuration
    underTest = new TypeWhitelist();

    assertThat(underTest.isAllowAll(), is(true));
    assertThat(underTest.isAllowed("test.Foo"), is(true));
  }

  @Test
  public void denyType() {
    assertThat(underTest.isAllowed("test.Foo"), is(false));
  }

  @Test
  public void allowType() {
    underTest.allowType("test.Bar");
    assertThat(underTest.isAllowed("test.Foo"), is(false));
    assertThat(underTest.isAllowed("test.Bar"), is(true));

    Set<String> allowedTypes = underTest.getAllowedTypes();
    assertThat(allowedTypes, hasSize(1));
    assertThat(allowedTypes, contains("test.Bar"));

    assertThat(underTest.getAllowedPackages(), hasSize(0));
  }

  @Test
  public void parsePackageName() {
    assertThat(underTest.parsePackageName("foo.Bar"), is("foo"));
    assertThat(underTest.parsePackageName("Baz"), is(TypeWhitelist.DEFAULT_PACKAGE_NAME));
  }

  @Test
  public void allowPackage() {
    underTest.allowPackage("test");
    assertThat(underTest.isAllowed("test.Foo"), is(true));
    assertThat(underTest.isAllowed("test.Bar"), is(true));

    // different package or sub-package should deny
    assertThat(underTest.isAllowed("sub.A"), is(false));
    assertThat(underTest.isAllowed("test.sub.A"), is(false));

    Set<String> allowedPackages = underTest.getAllowedPackages();
    assertThat(allowedPackages, hasSize(1));
    assertThat(allowedPackages, contains("test"));

    assertThat(underTest.getAllowedTypes(), hasSize(0));
  }

  @Test
  public void allowPattern() {
    underTest.allowPattern("org\\.sonatype\\..*DTO");
    assertThat(underTest.isAllowed("org.sonatype.foo.FooDTO"), is(true));
    assertThat(underTest.isAllowed("org.sonatype.foo.BarDTO"), is(true));
    assertThat(underTest.isAllowed("org.sonatype.foo.Foo"), is(false));
  }

  @Test
  public void sonatypeModelPatterns() {
    underTest.allowPattern(
        "^(org|com)\\.sonatype\\..*\\.model\\..*",
        "^(org|com)\\.sonatype\\..*\\.dto\\..*"
    );

    assertThat(underTest.isAllowed("org.sonatype.foo.model.FooResourceResponse"), is(true));
    assertThat(underTest.isAllowed("com.sonatype.foo.model.FooResourceResponse"), is(true));
    assertThat(underTest.isAllowed("org.sonatype.bar.api.dto.BarDTO"), is(true));
    assertThat(underTest.isAllowed("com.sonatype.bar.api.dto.BarDTO"), is(true));

    assertThat(underTest.isAllowed("org.sonatype.foo.Foo"), is(false));
    assertThat(underTest.isAllowed("com.sonatype.foo.Foo"), is(false));
    assertThat(underTest.isAllowed("org.sonatype.bar.Bar"), is(false));
    assertThat(underTest.isAllowed("com.sonatype.bar.Bar"), is(false));
  }

  @Test
  public void allowDefaultPackage() {
    underTest.allowPackage(TypeWhitelist.DEFAULT_PACKAGE_NAME);
    assertThat(underTest.isAllowed("A"), is(true));
    assertThat(underTest.isAllowed("test.Foo"), is(false));
  }

  @Test
  public void ensureAllowedDeny() {
    try {
      underTest.ensureAllowed("test.Foo");
      fail();
    }
    catch (TypeNotAllowedException e) {
       assertThat(e.getMessage(), is("test.Foo"));
    }
  }

  @Test
  public void ensureAllowedAllowType() {
    underTest.allowType("test.Foo");
    underTest.ensureAllowed("test.Foo");
  }

  @Test
  public void ensureAllowedAllowPackage() {
    underTest.allowPackage("test");
    underTest.ensureAllowed("test.Foo");
  }

  @Test
  public void frozenImmutable() {
    assertThat(underTest.isFrozen(), is(false));
    underTest.allowType("test.Foo");
    underTest.freeze();
    assertThat(underTest.isFrozen(), is(true));

    try {
      underTest.allowType("test");
      fail();
    }
    catch (IllegalStateException e) {
      // expected
    }

    try {
      underTest.allowPackage("test");
      fail();
    }
    catch (IllegalStateException e) {
      // expected
    }

    try {
      underTest.allowPattern("test");
      fail();
    }
    catch (IllegalStateException e) {
      // expected
    }
  }
}
