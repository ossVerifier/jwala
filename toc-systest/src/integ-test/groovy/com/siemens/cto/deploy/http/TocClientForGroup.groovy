package com.siemens.cto.deploy.http

import groovy.json.JsonSlurper

/**
 * Created by z003e5zv on 4/7/2015.
 */
class TocClientForGroup extends AbstractTocClient {

    public TocClientForGroup(TocHttpClient httpClient) {
        super(httpClient, "groups");
    }

    public void deleteGroup(int groupId) {
        tocHttpClient.delete(getV1Url() + "/" + groupId)
    }

    public int getOrCreateGroup(String groupName) {
        def groupsUrl = "${getV1Url()}?name=${groupName}"
        println "url = ${groupsUrl}";
        def response = tocHttpClient.get(groupsUrl);

        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" && result.applicationResponseContent.size() > 0) {
            def groupId = result.applicationResponseContent[0].id.id;
            return groupId;
        }
        else {
            // otherwise create
            response = tocHttpClient.execute(getV1Url(), groupName);

            result = slurper.parseText(response);
            def groupId = result.applicationResponseContent.id.id;
            println "groupId = ${groupId}"
            return groupId;
        }
    }

    public int createGroup(String groupName) {
        String response = tocHttpClient.execute(getV1Url(), groupName);

        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);
        def groupId = result.applicationResponseContent.id.id;
        println "groupId = ${groupId}";
        return groupId;
    }

    public String getGroups(String id = 'all') {
        def groupsUrl = getV1Url();
        if (id && id != 'all') {
            groupsUrl += "/" + id;
        }
        println "url = ${groupsUrl}";
        tocHttpClient.get(groupsUrl);
    }
    public Map<String,Integer> getJvmIdsForGroupAndServer(String groupName, String hostName) {
        def groupsUrl = "${getV1Url()}?name=${groupName}"
        println "url = ${groupsUrl}";
        String response = tocHttpClient.get(groupsUrl);

        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode != "0" ) {
            return null;
        }

        def resultMap = new HashMap<String, Integer>();

        for(def jvm : result.applicationResponseContent[0].jvms) {
            if(jvm.hostName == hostName) {
                resultMap.put(jvm.jvmName, jvm.id.id)
            }
        }
        println(resultMap)
        return resultMap;
    }
}
