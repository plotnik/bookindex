package io.github.plotnik;

import groovy.json.*;
import groovy.xml.XmlSlurper;

class GroovyUtils {

    static List<String> stringAtts(atts) {
        List<String> result = new ArrayList();
        for (def att in atts) {
            result.add(att.toString())
        }
        return result
    }

    static Object toJson(obj) {
        return JsonOutput.toJson(obj)
    }

    static List loadBooks(String folder, String fname, String mstamp) {
        def root = new XmlSlurper().parseText(new File(folder, fname).text);
        List result = new ArrayList<Book>();
        for (def section : root.section) {
            for (def xbook : section.book) {
                result.add(toBook(xbook, section.@name, mstamp));
            }
        }
        return result;
    }

    static Book toBook(book, section1, mstamp) {
        Book res = new Book();
        res.name = book.@name
        res.title = book.@title
        res.author = book.@author
        res.source = book.@source

        res.sections = new ArrayList()
        res.sections.add(section1.toString())
        res.sections.addAll(stringAtts(book.section*.@name))

        res.links = new ArrayList()
        res.links.addAll(stringAtts(book.a*.@href))

        res.mstamp = mstamp
        return res
    }

}