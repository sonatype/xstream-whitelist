package com.thoughtworks.xstream.whitelist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to read Maven pom.properties metadata.
 */
public class PomProperties
  extends Properties
{
  public static final String UNKNOWN = "unknown";

  public static final String GROUP_ID = "groupId";

  public static final String ARTIFACT_ID = "artifactId";

  public static final String VERSION = "version";

  private static final Logger log = LoggerFactory.getLogger(PomProperties.class);

  public PomProperties(final Class owner, final String groupId, final String artifactId) {
    checkNotNull(owner);
    checkNotNull(groupId);
    checkNotNull(artifactId);
    try {
      loadMetadata(owner, groupId, artifactId);
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private void loadMetadata(final Class owner, final String groupId, final String artifactId) throws IOException {
    String path = String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId);
    URL url = owner.getResource(path);
    if (url == null) {
      log.warn("Missing Maven pom.properties metadata: {}", path);
      return;
    }

    log.debug("Loading properties from: {}", url);
    InputStream input = url.openStream();
    try {
      this.load(input);
    }
    finally {
      input.close();
    }

    // Complain if there is a mismatch between what we expect the gav to be and what it really is
    String foundGroupId = getGroupId();
    if (!groupId.equals(foundGroupId)) {
      log.warn("Artifact groupId mismatch; expected: {}, found: {}", groupId, foundGroupId);
    }
    String foundArtifactId = getArtifactId();
    if (!artifactId.equals(foundArtifactId)) {
      log.warn("Artifact artifactId mismatch; expected: {}, found: {}", artifactId, foundArtifactId);
    }
  }

  public String getGroupId() {
    return getProperty(GROUP_ID, UNKNOWN);
  }

  public String getArtifactId() {
    return getProperty(ARTIFACT_ID, UNKNOWN);
  }

  public String getVersion() {
    return getProperty(VERSION, UNKNOWN);
  }
}
