package io.github.plotnik

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.apache.http.client.ResponseHandler
import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException

import groovy.json.*

// https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/client/CloseableHttpClient.html

public class ApperyClient {

    IWorker worker;
    Settings settings;
    List books;

    CloseableHttpClient httpclient = HttpClients.createDefault();
    JsonSlurper jsonSlurper = new JsonSlurper()
    
    public ApperyClient(IWorker worker) {
        this.worker = worker;
        settings = worker.settings;
        books = worker.settings.books;    
    }

    void updateBook(String mstamp, String title) {
    	worker.console(title)
        Book book = books.find { it.@title == title }
        if (recordExists(mstamp, title)) {
            worker.console("Record found")
        } else {
            worker.console("Record not found")
            String imgUrl = uploadImage(book.@name)
            worker.console("imgUrl: " + imgUrl);
        }
    }
    
    String uploadImage(String bookName) {
        worker.console("uploadImage " + bookName)
        File imageFile = new File(settings.folder + "/img/" + bookName + ".jpg")
        String file_name = bookName.replace(' ','_')
        
        HttpPost httppost = new HttpPost('https://api.appery.io/rest/1/db/files/');
        def reqEntity = MultipartEntityBuilder.create()
                        .setContentType(ContentType.MULTIPART_FORM_DATA)
                        .addPart(file_name, new FileBody(imageFile))
                        .build();
                        
        httppost.setEntity(reqEntity); 
        httppost.addHeader("X-Appery-Database-Id", settings.apperyDbId)
        httppost.addHeader("X-Appery-Master-Key", settings.apperyMasterKey)
        
        String responseBody = httpclient.execute(httppost, new BasicResponseHandler()); 
        return jsonSlurper.parseText(responseBody).success.filename[0] //.fileurl[0]    
    }
    
    boolean recordExists(String mstamp, String title) {
        URIBuilder uriBuilder = new URIBuilder()
            .setScheme("https")
            .setHost("api.appery.io")
            .setPath("/rest/1/db/collections/Books")
            .addParameter("where", JsonOutput.toJson([
                "mstamp":mstamp, "title":title]))
        
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.addHeader("X-Appery-Database-Id", settings.apperyDbId)
        httpGet.addHeader("X-Appery-Master-Key", settings.apperyMasterKey)
        
        String responseBody = httpclient.execute(httpGet, new BasicResponseHandler())
        def resp = jsonSlurper.parseText(responseBody)
        worker.console("--- responseBody: " + responseBody)
        return resp.size()!=0
    }
    
}