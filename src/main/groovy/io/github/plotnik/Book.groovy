package io.github.plotnik

import groovy.json.*

public class Book {

    String name
    String title
    String author
    String source

    List links
    List sections

    String mstamp
    String img

    Object toc

    public Book(book, section1, mstamp) {
        name = book.@name
        title = book.@title
        author = book.@author
        source = book.@source
            
        sections = []
        sections.add(section1)
        sections.addAll(book.section*.@name)

        links = []
        sections.addAll(book.a*.@href)
        
        this.mstamp = mstamp
    }
    
    public String toJson() {
        println "toJson $toc ---" 
        String result = JsonOutput.toJson([
            "name":name, "title":title, 
            "author":author, "source": source,
            "links": links, 
            "sections": sections,
            "mstamp": mstamp, "img": img,
            "toc": toc
            ]);
        println "result: " + result
        return result
    }
}