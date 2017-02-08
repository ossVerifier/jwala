package com.cerner.jwala.service.bootstrap;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.service.exception.ApplicationStartupException;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.media.MediaService;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.*;
import java.util.*;

/**
 * The application startup listener that checks for upgrades.
 * <p>
 * The initial use case for this class was to check for backwards compatibility for the JDK media. The addition
 * of the JDK media to the JVM configuration introduced a dependency that needs to be fulfilled in order for the
 * JVM generation to work. For deployed instances of the application that are not configured with the JDK media, this
 * startup listener will configure the application with a default JDK for the JVMs.
 */
public class ApplicationContextListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationContextListener.class);
    private static final String JWALA_BYPASS_JDK_MEDIA_BOOTSTRAP_CONFIGURATION = "jwala.jdk.media.bootstrap.configuration.bypass";

    @Autowired
    private MediaService mediaService;

    @Autowired
    private JvmService jvmService;

    /**
     * Implementation of the spring event listener interface.
     *
     * @param event the spring event from the application
     */
    @EventListener
    public void handleEvent(ApplicationEvent event) {
        // checking for start up event
        // order of events is BrokerAvailabilityEvent -> ContextRefreshedEvent[parent=null] -> ContextRefreshedEvent[with non-null parent]
        // so wait until the latest event is received: ContextRefreshedEvent[with non-null parent]

        // skip the BrokerAvailabilityEvent, and ignore all other events (SessionConnectedEvent, ServletRequestHandledEvent, ContextClosedEvent, etc.)
        if (!(event instanceof ContextRefreshedEvent)) {
            LOGGER.debug("Expecting ContextRefreshedEvent. Skipping.");
            return;
        }

        LOGGER.info("Received ContextRefreshedEvent {}", event);

        ContextRefreshedEvent crEvent = (ContextRefreshedEvent) event;
        final ApplicationContext applicationContext = crEvent.getApplicationContext();
        // skip the ContextRefreshedEvent[parent=null] but check for non-null context first
        if (null == applicationContext) {
            LOGGER.debug("Expecting non-null ApplicationContext. Skipping.");
            return;
        }
        if (null == applicationContext.getParent()) {
            LOGGER.debug("Expecting non-null ApplicationContext parent. Skipping.");
            return;
        }

        processBootstrapConfiguration();
    }

    /**
     * Run the upgrade steps
     */
    private void processBootstrapConfiguration() {
        LOGGER.info("Begin bootstrap configuration");
        configureJDKMedia();
        LOGGER.info("End bootstrap configuration");
    }

    private void configureJDKMedia() {

        // check a property to bypass the upgrade
        final Boolean bypassJDKMediaConfig = ApplicationProperties.getAsBoolean(JWALA_BYPASS_JDK_MEDIA_BOOTSTRAP_CONFIGURATION);
        LOGGER.info("Property {} {}", JWALA_BYPASS_JDK_MEDIA_BOOTSTRAP_CONFIGURATION, bypassJDKMediaConfig);
        if (bypassJDKMediaConfig) {
            LOGGER.info("Skip JDK media bootstrap configuration.");
            return;
        }

        // check if any JVM's are configured
        if (jvmService.getJvms().isEmpty()) {
            LOGGER.info("No JVMs configured. Exit JDK media upgrade.");
            return;
        }

        // set the JDK in the media tab
        JpaMedia jdkMedia = populateJDKMedia();

        // associate the JVMs if no JDK media is associated
        associateJDKtoJVMs(jdkMedia);
    }

    private void associateJDKtoJVMs(JpaMedia jdkMedia) {
        List<Jvm> allJvms = jvmService.getJvms();
        for (Jvm jvm : allJvms) {
            if (null == jvm.getJdkMedia()) {
                LOGGER.info("Updating JVM {} with JDK media {}", jvm.getJvmName(), jdkMedia.getName());
                UpdateJvmRequest addMediaRequest = new UpdateJvmRequest(
                        jvm.getId(),
                        jvm.getJvmName(),
                        jvm.getHostName(),
                        convertGroupIds(jvm.getGroups()),
                        jvm.getHttpPort(),
                        jvm.getHttpsPort(),
                        jvm.getRedirectPort(),
                        jvm.getShutdownPort(),
                        jvm.getAjpPort(),
                        jvm.getStatusPath(),
                        jvm.getSystemProperties(),
                        jvm.getUserName(),
                        jvm.getEncryptedPassword(),
                        new Identifier<Media>(jdkMedia.getId())
                );
                jvmService.updateJvm(addMediaRequest);
            }
        }
    }

    private Set convertGroupIds(Set<Group> groups) {
        final Set groupIdSet = new HashSet<>();
        for (final Group group : groups) {
            groupIdSet.add(group.getId());
        }
        return groupIdSet;
    }

    private JpaMedia populateJDKMedia() {
        JpaMedia retVal = null;
        List<JpaMedia> allMedia = mediaService.findAll();
        if (allMedia.isEmpty()) {
            LOGGER.info("No media configured, upload JDK media from jwala");
            retVal = uploadJDKFromJwala();
        } else {
            LOGGER.info("Found existing media. Current count {}", allMedia.size());
            for (JpaMedia media : allMedia) {
                if (com.cerner.jwala.persistence.jpa.type.MediaType.JDK.equals(media.getType())) {
                    LOGGER.info("Found existing JDK media {}", media.getName());
                    retVal = media;
                    break;
                }
            }
            if (null == retVal) {
                LOGGER.info("No JDK media found in configured media, upload JDK media from jwala");
                retVal = uploadJDKFromJwala();
            }
        }
        return retVal;
    }

    private JpaMedia uploadJDKFromJwala() {
        Map<String, String> mediaDataMap = createJDKAttributesMap();
        Map<String, Object> mediaFileDataMap = createJDKBinaryMap();

        return mediaService.create(mediaDataMap, mediaFileDataMap);
    }

    private Map<String, Object> createJDKBinaryMap() {
        Map<String, Object> mediaFileDataMap = new HashMap<>();
        final String binaryDirPath = ApplicationProperties.getRequired("jwala.binary.dir");
        FileInputStream jdkBinaryZipStream = null;
        final String jdkFileName = findJdkBinaryName(binaryDirPath);
        final String jdkBinaryPath = binaryDirPath + "/" + jdkFileName;
        LOGGER.debug("Creating JDK media file data map[contentPath->{}, filename->{}]", jdkBinaryPath, jdkFileName);

        try {
            jdkBinaryZipStream = new FileInputStream(jdkBinaryPath);
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not create stream for JDK file in jwala binary directory", e);
            throw new ApplicationStartupException("Could not create stream for JDK file in jwala binary directory :: " + jdkBinaryPath);
        }
        BufferedInputStream jdkStream = new BufferedInputStream(jdkBinaryZipStream);
        mediaFileDataMap.put("content", jdkStream);
        mediaFileDataMap.put("filename", jdkFileName);

        return mediaFileDataMap;
    }

    private String findJdkBinaryName(String binaryDirPath) {
        File binaryDir = new File(binaryDirPath);
        final String wildcard = ApplicationProperties.get("jwala.jdk.media.bootstrap.configuration.filter", "jdk*");
        FileFilter jdkFilter = new WildcardFileFilter(wildcard);
        File[] jdkFiles = binaryDir.listFiles(jdkFilter);
        if (null == jdkFiles || jdkFiles.length == 0) {
            LOGGER.error("Expecting a compressed jdk file to exist in the jwala binaries directory. None found in {}", binaryDirPath);
            throw new ApplicationStartupException("Expecting a compressed jdk file to exist in the jwala binaries directory. None found in " + binaryDirPath);
        }
        return jdkFiles[0].getName();
    }

    private Map<String, String> createJDKAttributesMap() {
        Map<String, String> mediaDataMap = new HashMap<>();
        final String remoteDir = ApplicationProperties.getRequired("remote.paths.deploy.dir");
        final String jdkName = ApplicationProperties.get("jwala.default.jdk.media.name", "jwala-default-JDK");
        LOGGER.debug("Creating JDK media data map[remoteDir->{}, name->{}]", remoteDir, jdkName);

        mediaDataMap.put("remoteDir", remoteDir);
        mediaDataMap.put("name", jdkName);
        mediaDataMap.put("type", "JDK");

        return mediaDataMap;
    }
}
