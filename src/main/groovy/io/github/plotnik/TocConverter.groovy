package io.github.plotnik;

import groovy.json.*

class TocConverter {

    def root;
    def bookTitle;

    TocConverter(root, bookTitle) {
        this.root = root
        this.bookTitle = bookTitle
    }

    def convert() {
        def titleNode = findTextNode(root, bookTitle)
        if (titleNode==null) {
            throw new BookException("Missing book title: \"$bookTitle\"")
        }
        //return JsonOutput.toJson(goThroughNodes(titleNode))
        return goThroughNodes(titleNode)
    }

    def goThroughNodes(node) {
        //println "children: "+node.children().size()
        if (node.children().size()>0) {
            List result = [["t": node.@TEXT]]
            for (def ch in node.children()) {
                result.add(goThroughNodes(ch))
            }
            return result
        } else {
            return ["t": node.@TEXT]
        }
    }

    def findTextNode(node, text) {
        //println "text: "+node.@TEXT
        if (node.@TEXT==bookTitle) {
            return node
        }
        for (def ch in node.children()) {
            def result = findTextNode(ch, text)
            if (result!=null) {
                return result
            }
        }
        return null
    }
}