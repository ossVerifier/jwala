Feature: Generate

  Scenario: Generate a JVM

    Given I logged in

    And I am in the Configuration tab

  # create media
    And I created a media with the following parameters:
      | mediaName       | jdk.media         |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | apache.tomcat.media         |
      | mediaType       | Apache Tomcat               |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | tomcat.media.remote.dir     |

  # create entities
    And I created a group with the name "seleniumGroup"
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm         |
      | tomcat     | apache.tomcat.media |
      | jdk        | jdk.media           |
      | hostName   | host1               |
      | portNumber | jvm.http.port       |
      | group      | seleniumGroup       |

  # create resources
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                   |
      | jvm          | seleniumJvm                     |
      | deployName   | setenv.bat                      |
      | deployPath   | jvm.setenv.resource.deploy.path |
      | templateName | setenv.bat.tpl                  |
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | jvm          | seleniumJvm                         |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |

    And I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group

  # do the test
    When I generate "seleniumJvm" JVM of "seleniumGroup" group
    Then I see the JVM was successfully generated

  Scenario: Generate a Webserver

    Given I logged in

    And I am in the Configuration tab

      # create media
    And I created a media with the following parameters:
      | mediaName       | apache.httpd.media         |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |

      # create entities
    And I created a group with the name "seleniumGroup"
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver  |
      | hostName           | host1              |
      | portNumber         | ws.http.port       |
      | httpsPort          | ws.https.port      |
      | group              | seleniumGroup      |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | ws.status.path     |

      # create resources
    And I created a web server resource with the following parameters:
      | group        | seleniumGroup              |
      | webServer    | seleniumWebserver          |
      | deployName   | httpd.conf                 |
      | deployPath   | httpd.resource.deploy.path |
      | templateName | httpdconf.tpl              |

    And I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group

      # do the test
    When I generate "seleniumWebserver" web server of "seleniumGroup" group
    Then I see the web server was successfully generated