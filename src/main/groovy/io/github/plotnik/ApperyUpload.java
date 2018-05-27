package io.github.plotnik;

import java.io.File;

public class ApperyUpload {
    
    String booksHome;
    String monthStamp;
    
    void uploadFolder(String path) throws BookException {
        File f = new File(path);
        if (!f.exists()) {
            throw new BookException("File not found: " + f.getPath());
        }
        
    }
}
