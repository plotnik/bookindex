package io.github.plotnik;

import java.io.IOException;
import java.nio.file.Files;
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

}
