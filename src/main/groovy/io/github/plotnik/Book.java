package io.github.plotnik;

import java.util.List;

public class Book {

    String name;
    String title;
    String author;
    String source;
    String folder;

    List links;
    List sections;

    String mstamp;
    String img;

    String toc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public List getLinks() {
        return links;
    }

    public void setLinks(List links) {
        this.links = links;
    }

    public List getSections() {
        return sections;
    }

    public void setSections(List sections) {
        this.sections = sections;
    }

    public String getMstamp() {
        return mstamp;
    }

    public void setMstamp(String mstamp) {
        this.mstamp = mstamp;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getToc() {
        return toc;
    }

    public void setToc(String toc) {
        this.toc = toc;
    }

}