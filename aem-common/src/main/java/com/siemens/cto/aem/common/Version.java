package com.siemens.cto.aem.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Version {

    private static final Logger LOGGER = LoggerFactory.getLogger(Version.class);

    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final String IMPLEMENTATION_TITLE = "Implementation-Title";
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";
    private static final String BUILD_TIME_ISO_8601 = "Build-Time-ISO-8601";

    private static final String AEM_COMMON = "aem-common";

    private static final Map<String, String> manifestAttributes = getManifestAttributes();

    private Version() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }

    public static String getTitle() {
        return manifestAttributes.get(IMPLEMENTATION_TITLE);
    }

    public static String getVersion() {
        return manifestAttributes.get(IMPLEMENTATION_VERSION);
    }

    public static String getBuildTime() {
        return manifestAttributes.get(BUILD_TIME_ISO_8601);
    }

    private static Map<String, String> getManifestAttributes() {
        final Map<String, String> result = new TreeMap<String, String>();
        result.put(IMPLEMENTATION_TITLE, AEM_COMMON);
        result.put(IMPLEMENTATION_VERSION, "1.0.0.0.100");
        result.put(BUILD_TIME_ISO_8601, "2014-01-01T00:00:00-0500");

        final Manifest manifest = getManifest();
        if (null != manifest) {
            final Attributes attributes = manifest.getMainAttributes();
            final Set<Object> keySet = attributes.keySet();
            for (final Object key : keySet) {
                final Object value = attributes.get(key);
                result.put(key.toString(), value.toString());
            }
        }
        return result;
    }

    private static Manifest getManifest() {
        Manifest result = null;
        final Class<Version> objectsClass = Version.class;
        final ClassLoader classLoader = objectsClass.getClassLoader();

        try {
            final Enumeration<URL> resources = classLoader.getResources(MANIFEST_NAME);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();

                final Object content = url.getContent();
                if (content instanceof InputStream) {

                    final InputStream fis = (InputStream) content;
                    try {
                        final Manifest m = new Manifest(fis);
                        final Attributes attributes = m.getMainAttributes();
                        final Set<Object> keySet = attributes.keySet();
                        for (final Object key : keySet) {
                            final Object value = attributes.get(key);

                            if (IMPLEMENTATION_TITLE.equals(key.toString()) && AEM_COMMON.equals(value.toString())) {
                                result = m;
                                break;
                            }
                        }
                    } finally {
                        fis.close();
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.warn("Unable to obtain manifest", e);
        }
        return result;
    }
}
