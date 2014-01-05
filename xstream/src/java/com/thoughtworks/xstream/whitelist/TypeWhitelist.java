package com.thoughtworks.xstream.whitelist;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Type white-list.
 */
public class TypeWhitelist
{
  private static final Logger log = LoggerFactory.getLogger(TypeWhitelist.class);

  public static final String DEFAULT_PACKAGE_NAME = "<default>";

  @VisibleForTesting
  public static final SystemProperty allowAllProperty = new SystemProperty(TypeWhitelist.class, "allowAll");

  @VisibleForTesting
  public static final SystemProperty allowedTypesProperty = new SystemProperty(TypeWhitelist.class, "allowedTypes");

  @VisibleForTesting
  public static final SystemProperty allowedPackagesProperty = new SystemProperty(TypeWhitelist.class, "allowedPackages");

  @VisibleForTesting
  public static final SystemProperty allowedPatternsProperty = new SystemProperty(TypeWhitelist.class, "allowedPatterns");

  private final Set<String> allowedTypes = Sets.newHashSet();

  private final Set<String> allowedPackages = Sets.newHashSet();

  private final List<Pattern> allowedPatterns = Lists.newArrayList();

  private boolean allowAll;

  public TypeWhitelist() {
    setAllowAll(allowAllProperty.get(Boolean.class, false));
    configureDefaults();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "allowAll=" + allowAll +
        ", allowedTypes=" + allowedTypes +
        ", allowedPackages=" + allowedPackages +
        ", allowedPatterns=" + allowedPatterns +
        '}';
  }

  @VisibleForTesting
  void configureDefaults() {
    final boolean trace = log.isTraceEnabled();

    // configure default allowed types from system properties
    allowedTypes.addAll(allowedTypesProperty.asList());
    if (trace && !allowedTypes.isEmpty()) {
      log.trace("Default allowed types:");
      for (String name : sort(allowedTypes)) {
        log.trace("  {}", name);
      }
    }

    // configure default allowed packages from system properties
    allowedPackages.addAll(allowedPackagesProperty.asList());
    if (trace && !allowedPackages.isEmpty()) {
      log.trace("Default allowed packages:");
      for (String name : sort(allowedPackages)) {
        log.trace("  {}", name);
      }
    }

    // configure default allowed patterns from system properties
    allowedPatterns.addAll(Lists.transform(allowedPatternsProperty.asList(), new Function<String, Pattern>()
    {
      public Pattern apply(final String input) {
        return compilePattern(input);
      }
    }));
    if (trace && !allowedPatterns.isEmpty()) {
      log.trace("Default allowed patterns:");
      for (Pattern pattern : allowedPatterns) {
        log.trace("  {}", pattern);
      }
    }
  }

  public boolean isAllowAll() {
    return allowAll;
  }

  public void setAllowAll(final boolean allowAll) {
    this.allowAll = allowAll;
    if (allowAll) {
      log.warn("All types are allowed");
    }
  }

  private Collection<String> sort(final Collection<String> collection) {
    List<String> list = Lists.newArrayList(collection);
    Collections.sort(list);
    return list;
  }

  public Set<String> getAllowedTypes() {
    return ImmutableSet.copyOf(allowedTypes);
  }

  public Set<String> getAllowedPackages() {
    return ImmutableSet.copyOf(allowedPackages);
  }

  public void allowType(final String... names) {
    if (names == null) {
      return;
    }
    for (String name : names) {
      if (name != null) {
        allowedTypes.add(name);
        log.trace("Allow type: {}", name);
      }
    }
  }

  public void allowType(final Class... types) {
    if (types == null) {
      return;
    }
    for (Class type : types) {
      if (type != null) {
        allowType(type.getName());
      }
    }
  }

  public void allowPackage(final String... names) {
    if (names == null) {
      return;
    }
    for (String name : names) {
      if (name != null) {
        allowedPackages.add(name);
        log.trace("Allow package: {}", name);
      }
    }
  }

  public void allowPackage(final Package... packages) {
    if (packages == null) {
      return;
    }
    for (Package pkg : packages) {
      if (pkg != null) {
        allowPackage(pkg.getName());
      }
    }
  }

  public void allowPattern(final Pattern... patterns) {
    if (patterns == null) {
      return;
    }
    for (Pattern pattern : patterns) {
      if (pattern != null) {
        allowedPatterns.add(pattern);
        log.trace("Allow pattern: {}", pattern);
      }
    }
  }

  public void allowPattern(final String... patterns) {
    if (patterns == null) {
      return;
    }
    for (String pattern : patterns) {
      if (pattern != null) {
        allowPattern(compilePattern(pattern));
      }
    }
  }

  @VisibleForTesting
  Pattern compilePattern(final String pattern) {
    // TODO: Consider if we want to support a simpler form of pattern (glob-style for example)
    return Pattern.compile(pattern);
  }

  public boolean isAllowed(final String className) {
    checkNotNull(className);

    if (allowAll) {
      log.trace("All types allowed: {}", className);
      return true;
    }

    log.trace("Checking type allowance: {}", className);
    if (allowedTypes.contains(className)) {
      log.trace("Type allowed: {}", className);
      return true;
    }

    String packageName = parsePackageName(className);
    log.trace("Checking package allowance: {}", packageName);
    if (allowedPackages.contains(packageName)) {
      log.trace("Package allowed: {}", packageName);
      return true;
    }

    for (Pattern pattern : allowedPatterns) {
      if (pattern.matcher(className).matches()) {
        log.trace("Type allowed: {}, by pattern: {}", className, pattern);
        return true;
      }
    }

    log.warn("Type NOT allowed: {}", className);
    return false;
  }

  @VisibleForTesting
  String parsePackageName(final String className) {
    int i = className.lastIndexOf(".");
    if (i == -1) {
      return DEFAULT_PACKAGE_NAME;
    }
    else {
      return className.substring(0, i);
    }
  }

  public boolean isAllowed(final Class type) {
    checkNotNull(type);
    return isAllowed(type.getName());
  }

  /**
   * Throw when type is not allowed.
   *
   * @see #ensureAllowed(String)
   * @see #ensureAllowed(Class)
   */
  public static class TypeNotAllowedException
    extends CannotResolveClassException
  {
    public TypeNotAllowedException(final String className) {
      super(className);
    }
  }

  public void ensureAllowed(final String name) throws TypeNotAllowedException {
    checkNotNull(name);
    if (!isAllowed(name)) {
      log.warn("Type NOT allowed: {}", name);
      throw new TypeNotAllowedException(name);
    }
  }

  public void ensureAllowed(final Class type) throws TypeNotAllowedException {
    checkNotNull(type);
    ensureAllowed(type.getName());
  }
}
