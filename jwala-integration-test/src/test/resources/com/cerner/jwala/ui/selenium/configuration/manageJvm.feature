Feature: Add, Edit and Delete a JVM

Scenario: Add JVM

    Given I logged in
    And I am in the Configuration tab
    And I created a media with the following parameters:
            |mediaName      |jdk-media        |
            |mediaType      |JDK              |
            |archiveFilename|jdk.media.archive|
            |remoteDir      |media.remote.dir |
    And I created a media with the following parameters:
            |mediaName      |tomcat-media               |
            |mediaType      |Apache Tomcat              |
            |archiveFilename|apache.tomcat.media.archive|
            |remoteDir      |media.remote.dir           |
    And I created a group with the name "GROUP_FOR_ADD_JVM_TEST"
    And I am in the jvm tab
    When I click the add jvm button
    And I see the jvm add dialog
    And I fill in the "JVM Name" field with "JVM_X"
    And I fill in the "JVM Host Name" field with "host1"
    And I fill in the "JVM HTTP Port" field with "9100"
    And I click the "JVM status path" field to auto generate it
    And I select the "JVM JDK" version "jdk-media"
    And I select the "JVM Apache Tomcat" version "tomcat-media"
    And I associate the JVM to the following groups:
        |GROUP_FOR_ADD_JVM_TEST|
    And I click the jvm add dialog ok button
    And I see "JVM_X" in the jvm table
