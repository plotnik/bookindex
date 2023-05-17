package io.github.plotnik;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Obsidian {

    Path dir;
    Path dropbox;
    String source;
    
    String bookLink;
    String htmlLink;
    String dropboxLink;
    
    Obsidian(String dirPath, String source, String dropboxPath) {
        this.source = source;
        dir = Path.of(dirPath, source + "_code");
        dropbox = Path.of(dropboxPath, dirPath, source + "_code");
        if (Files.exists(dropbox)) {
            dropboxLink = checkDropboxLink();
        }
        if (!Files.exists(dir)) {
           return;
        }
        bookLink = checkBookLink();
        htmlLink = checkHtmlLink();
    }

    @Override
    public String toString() {
        return "Obsidian [dir=" + dir + ", dropbox=" + dropbox + ", source=" + source 
                + ", bookLink=" + bookLink 
                + ", htmlLink=" + htmlLink 
                + ", dropboxLink=" + dropboxLink 
                + "]";
    }

    String checkBookLink() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "* Book")) {
            for (Path entry: stream) {
                String name = entry.getFileName().toString();
                //String obsidianPath = entry.resolve(name + ".md").relativize(dirPath).toString();
                //System.out.println("obsidianPath: " + obsidianPath);
                String obsidianPath = source + "_code/" + name + "/" + name + ".md";
                return obsidianPath;
            }
        } catch (IOException e) {
        }
        return null;
    }
    
    String checkDropboxLink() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dropbox, "* Book")) {
            for (Path entry: stream) {
                String name = entry.getFileName().toString();
                String obsidianPath = source + "_code/" + name + "/" + name + ".md";
                return obsidianPath;
            }
        } catch (IOException e) {
        }
        return null;
    }
        
    String checkHtmlLink() {
        Path path = dir.resolve("html/index.html");
        if (!Files.exists(path)) {
            return null; 
        } else {
            return source + "_code/html/index.html";
        }
    }

    public String getBookLink() {
        return bookLink;
    }

    public String getHtmlLink() {
        return htmlLink;
    }

    public String getDropboxLink() {
        return dropboxLink;
    }

}
