package io.github.plotnik

public class ApperyClient {

    IWorker worker;
    List books;

    public ApperyClient(IWorker worker) {
        this.worker = worker;
        books = worker.settings.books    
    }

    void updateBook(String mstamp, String title) {
    	worker.console("mstamp: ${mstamp}, title: ${title}")
    }	
}