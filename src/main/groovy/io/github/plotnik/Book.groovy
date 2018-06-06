package io.github.plotnik

import groovy.json.*

public class Book {

    String name;
    String title;
    String author;
    String source;

    List links;
    List sections;

    String mstamp;
    String img;

    String toc;

    public Book(book, section1, mstamp) {
        this.name = book.@name
        this.title = book.@title
        this.author = book.@author
        this.source = book.@source

        this.sections = []
        this.sections.add(section1.toString())
        this.sections.addAll(stringAtts(book.section*.@name))

        this.links = []
        this.links.addAll(stringAtts(book.a*.@href))

        this.mstamp = mstamp
    }

    List<String> stringAtts(atts) {
        List<String> result = []
        for (def att in atts) {
            result.add(att.toString())
        }
        return result
    }

    public String toJson() {
        return JsonOutput.toJson(this)
    }
}