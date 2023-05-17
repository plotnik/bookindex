package io.github.plotnik;

import java.io.IOException;
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

}
