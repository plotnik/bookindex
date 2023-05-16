package io.github.plotnik;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StrUtils {

    /**
     * Load string from file.
     */
    static String loadStr(String folder, String fname) {
        try {
            return Files.readString(Paths.get(folder, fname));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Save string to file.
     */
    static void saveStr(String fname, String text) throws IOException {
        Files.writeString(Path.of(fname), text);
    }    

    public static boolean saveIfNeeded(String fname, String text) {
        try {
            String oldText = loadStr("", fname);
            if (text.equals(oldText)) {
                return false;
            }
            saveStr(fname, text);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getObsidianLink(String dirPath, String source) {
        Path dir = Paths.get(dirPath, source + "_code");
        if (!Files.exists(dir)) {
            return null;
        }
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
    
    public static String getHtmlLink(String dirPath, String source) {
        String link = source + "_code/html/index.html";
        Path path = Path.of(dirPath, link);
        if (!Files.exists(path)) {
            return null; 
        } else {
            return link;
        }
    }
}
