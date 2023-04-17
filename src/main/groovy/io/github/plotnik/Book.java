package io.github.plotnik;

import java.util.ArrayList;
import java.util.List;

public class Book {

    String name;
    String title;
    String author;
    String source;
    String folder;

    List<String> links = new ArrayList<>();
    List<String> sections = new ArrayList<>();

    String obsidian;

    @Override
    public String toString() {
        return "Book [name=" + name + ", title=" + title + ", author=" + author + ", source=" + source + ", folder="
                + folder + ", links=" + links + ", sections=" + sections + ", obsidian=" + obsidian + "]";
    }

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

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }
    
    public String getObsidian() {
        return obsidian;
    }

    public void setObsidian(String obsidian) {
        this.obsidian = obsidian;
    }
}