package org.sonatype.plexus.rest;

import com.thoughtworks.xstream.XStream;

/**
 * Helper to mimic automatic white-list when constructed via restlet-bridge.
 */
public class PlexusRestletApplicationBridge
{
  public static XStream createXstream(final ClassLoader classLoader) {
    XStream xstream = new XStream();
    xstream.setClassLoader(classLoader);
    return xstream;
  }
}
