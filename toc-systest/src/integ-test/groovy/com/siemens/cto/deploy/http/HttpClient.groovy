package com.siemens.cto.deploy.http

import groovy.json.JsonSlurper
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

public class HttpClient {

    protected TocContext httpContext;

    public HttpClient(TocContext httpContext) {
        this.httpContext = httpContext
    }

    public String execute(String url, String string) {
        HttpPost post = new HttpPost(url);

        StringEntity stringEntity = new StringEntity(string, "UTF-8");
        stringEntity.setContentType("application/json");
        post.setEntity(stringEntity)

        HttpResponse response = httpContext.httpClient.execute(post, httpContext.httpContext);
        String responseString = EntityUtils.toString(response.getEntity())

	    println "Got response: $responseString"

        def slurper = new JsonSlurper()
        def result = slurper.parseText(responseString);
        if (!result.message.equals("SUCCESS")) {
            throw new RestException("TOC API request did not succeed", responseString)
        }

        EntityUtils.consume(response.getEntity())

        return responseString;
    }
    
    public String put(String url, String string) {
        HttpPut put = new HttpPut(url);

        StringEntity stringEntity = new StringEntity(string, "UTF-8");
        stringEntity.setContentType("application/json");
        
        println "Sending request: $string"
        
        put.setEntity(stringEntity)


                HttpResponse response = httpContext.httpClient.execute(put, httpContext.httpContext);
        String responseString = EntityUtils.toString(response.getEntity())

        println "Got response: $responseString"

        def slurper = new JsonSlurper()
        def result = slurper.parseText(responseString);
        if (!result.message.equals("SUCCESS")) {
            throw new RestException("TOC API request did not succeed", responseString)
        }

        EntityUtils.consume(response.getEntity())

        return responseString;
    }

    public String get(String url) {
        String responseString = doGet(url)

        def slurper = new JsonSlurper()
        def result = slurper.parseText(responseString);
        if (!result.message.equals("SUCCESS")) {
            throw new RestException("TOC API request did not succeed", responseString)
        }

        return responseString;
    }

    public String delete(String url) {
        doDelete(url)
    }

    public void doDelete(String url) {
        HttpDelete delete = new HttpDelete(url);
        println "in doDelete with url = ${url}"
        
        httpContext.httpClient.execute(delete, httpContext.httpContext);

        // TODO no response, so check status code, bad status should causes exceptions
    }

    public String doGet(String url) {
        HttpGet get = new HttpGet(url);
        println "in doGet with url = ${url}"
        
        HttpResponse response = httpContext.httpClient.execute(get, httpContext.httpContext);
        String responseStr = EntityUtils.toString(response.getEntity())
        
        EntityUtils.consume(response.getEntity())

        println "responseStr = ${responseStr}"
        return responseStr
    }
    
    public String login() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("userName", httpContext.username));
        params.add(new BasicNameValuePair("password", httpContext.password));

        HttpPost post = new HttpPost(httpContext.urls.loginUrl);
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"))

        HttpResponse response = httpContext.httpClient.execute(post, httpContext.httpContext);
        String responseString = EntityUtils.toString(response.getEntity())
        EntityUtils.consume(response.getEntity())
        return responseString;
    }
}
