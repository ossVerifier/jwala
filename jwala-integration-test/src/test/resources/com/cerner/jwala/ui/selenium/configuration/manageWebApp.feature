Feature: Add, Edit and Delete a Web Server

Scenario: Add Web Application

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "GROUP_FOR_ADD_WEBAPP_TEST"
    And I am in the web apps tab
    When I click the add web app button
    And I see the web app add dialog
    And I fill in the web app "Name" field with "WEBAPP_X"
    And I fill in the web app "Context Path" field with "webapp"
    And I associate the web app to the following groups:
        |GROUP_FOR_ADD_WEBAPP_TEST|
    And I click the add web app dialog ok button
    Then I see the following web app details in the web app table:
        |name   |WEBAPP_X                 |
        |context|webapp                   |
        |group  |GROUP_FOR_ADD_WEBAPP_TEST|

Scenario: Upload Web App War file

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "GROUP_FOR_WAR_UPLOAD_TEST"
    And I created a web app with the following parameters:
        |name   |WEBAPP_X                 |
        |context|webapp                   |
        |group  |GROUP_FOR_WAR_UPLOAD_TEST|
    And I click web app file upload for "WEBAPP_X"
    And I see the web app upload war dialog
    And I choose a web app war file "webapp.war"
    And I select the web app war deploy path
    When I click web app upload war dialog upload button
    Then I see "webapp.war upload successful" message
    And I click the ok button of the web app war upload message
    And I see "webapp.war" under the web archive column
