package com.thoughtworks.xstream.whitelist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to access System properties.
 */
@VisibleForTesting
class SystemProperty
{
  private static final Logger log = LoggerFactory.getLogger(SystemProperty.class);

  private final String name;

  public SystemProperty(final String name) {
    this.name = checkNotNull(name);
  }

  public SystemProperty(final Class type, final String suffix) {
    this(checkNotNull(type, "type").getName() + "." + checkNotNull(suffix, "suffix"));
  }

  @VisibleForTesting
  Properties properties() {
    return System.getProperties();
  }

  public String name() {
    return name;
  }

  public void set(final @Nullable Object value) {
    String str = String.valueOf(value);
    properties().setProperty(name, str);
    log.trace("Set {}={}", name, str);
  }

  public boolean isSet() {
    return get() != null;
  }

  @Nullable
  public String get() {
    return properties().getProperty(name);
  }

  public String get(final String defaultValue) {
    String value = get();
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(final Class<T> type) {
    checkNotNull(type);
    PropertyEditor editor = PropertyEditorManager.findEditor(type);
    if (editor == null) {
      throw new RuntimeException("No property-editor for type: " + type.getName());
    }
    String value = get();
    if (value == null) {
      return null;
    }
    editor.setAsText(value);
    return (T) editor.getValue();
  }

  public <T> T get(final Class<T> type, final T defaultValue) {
    T value = get(type);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public void remove() {
    properties().remove(name);
    log.trace("Remove: {}", name);
  }

  // TODO: This could probably be converted into a PropertyEditor, for now leave as custom code

  public List<String> asList() {
    String value = properties().getProperty(name);
    if (value == null) {
      return Collections.emptyList();
    }
    String[] items = value.split(",");
    List<String> result = Lists.newArrayListWithCapacity(items.length);
    for (String item : items) {
      result.add(item.trim());
    }
    return result;
  }

  @Override
  public String toString() {
    return name + "=" + get();
  }
}
