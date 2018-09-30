package io.github.plotnik;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import groovy.json.*;
import groovy.util.XmlSlurper;
import java.io.File;
import java.util.List;
import org.apache.http.entity.StringEntity;

// https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/client/CloseableHttpClient.html

public class ApperyClient {

    IWorker worker;
    Settings settings;
    List books;

    CloseableHttpClient httpclient = HttpClients.createDefault();
    JsonSlurper jsonSlurper = new JsonSlurper();

    String protocol = "https";
    String apperyHost = "api.appery.io";
    String apperyBooksPath = "/rest/1/db/collections/Books";
    String apperyBooksUrl = protocol + "://" + apperyHost + apperyBooksPath;

    public ApperyClient(IWorker worker) {
        this.worker = worker;
        settings = worker.getSettings();
        books = settings.getBooks();
    }

    void updateBook(String mstamp, String title) {
        worker.console("Book: " + title);
        Book book = books.find { it.mstamp == mstamp && it.title == title }
        updateToc(book);
        String id = getRecordId(mstamp, title)
        if (id != null) {
            updateBookRecord(book, id);
        } else {
            book.img = uploadImage(book.@name);
            createBookRecord(book);
        }
    }

    void updateToc(Book book) {
        File toc = new File(settings.folder + "/" + book.@source + ".mm");
        if (!toc.exists()) {
            worker.console("*** toc not found");
            return
        }
        def root = new XmlParser().parseText(toc.text)
        book.toc = new TocConverter(root, book.title).convert();
    }

    void createBookRecord(Book book) {
        HttpPost httppost = new HttpPost(apperyBooksUrl);
        String json = book.toJson()
        //worker.console("--- book json:" + json);
        httppost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        httppost.addHeader("Content-Type", "application/json");
        httppost.addHeader("X-Appery-Database-Id", settings.apperyDbId);
        httppost.addHeader("X-Appery-Master-Key", settings.apperyMasterKey);
        String responseBody = httpclient.execute(httppost, new BasicResponseHandler());
        //worker.console("--- createBookRecord response: " + responseBody);
        worker.console("Book uploaded")
    }

    void updateBookRecord(book, String id) {
        HttpPut httpput = new HttpPut(apperyBooksUrl + "/" + id);
        String json = book.toJson()
        httpput.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        httpput.addHeader("Content-Type", "application/json");
        httpput.addHeader("X-Appery-Database-Id", settings.apperyDbId);
        httpput.addHeader("X-Appery-Master-Key", settings.apperyMasterKey);
        String responseBody = httpclient.execute(httpput, new BasicResponseHandler());
        //worker.console("--- updateBookRecord response: " + responseBody);
        worker.console("...updated")
    }

    String uploadImage(String bookName) {
        //worker.console("uploadImage " + bookName)
        File imageFile = new File(settings.folder + "/img/" + bookName + ".jpg")
        String file_name = bookName.replace(' ','_')

        HttpPost httppost = new HttpPost("${protocol}://${apperyHost}/rest/1/db/files/");
        def reqEntity = MultipartEntityBuilder.create()
                        .setContentType(ContentType.MULTIPART_FORM_DATA)
                        .addPart(file_name, new FileBody(imageFile))
                        .build();

        httppost.setEntity(reqEntity);
        httppost.addHeader("X-Appery-Database-Id", settings.apperyDbId);
        httppost.addHeader("X-Appery-Master-Key", settings.apperyMasterKey);

        String responseBody = httpclient.execute(httppost, new BasicResponseHandler());
        worker.console("Image uploaded");
        return jsonSlurper.parseText(responseBody).success.filename[0] //.fileurl[0]
    }

    String getRecordId(String mstamp, String title) {
        URIBuilder uriBuilder = new URIBuilder()
            .setScheme(protocol)
            .setHost(apperyHost)
            .setPath(apperyBooksPath)
            .addParameter("where", JsonOutput.toJson([
                "mstamp":mstamp, "title":title]))

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.addHeader("X-Appery-Database-Id", settings.apperyDbId)
        httpGet.addHeader("X-Appery-Master-Key", settings.apperyMasterKey)

        String responseBody = httpclient.execute(httpGet, new BasicResponseHandler())
        def resp = jsonSlurper.parseText(responseBody)
        //worker.console("--- recordExists response: " + responseBody)
        if (resp.size()==0) {
            return null
        } else {
            return resp[0]._id
        }
    }

}