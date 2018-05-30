package io.github.plotnik;

import java.io.File;
import java.util.regex.Pattern;

public class Settings {
    
    private String folder;
    private String monthStamp;
    
    private String apperyDbId;
    private String apperyMasterKey;

    List books = []

    public String getFolder() {
        return folder;
    }

    public String getMonthStamp() {
        return monthStamp;
    }

    public void setFolder(String folder) throws BookException {
        this.folder = folder;
        File f = new File(folder);
        if (!f.exists()) {
            throw new BookException("Folder not found");
        }
        String mstamp = f.getName();
        if (!Pattern.matches("\\d{2}-\\d{2}", mstamp)) {
            throw new BookException("Folder name should be a month stamp");
        }
        monthStamp = mstamp;
        File b = new File(folder, "books.xml");
        if (!b.exists()) {
            throw new BookException("Missing `books.xml` index in folder");
        }
    }

    public String getApperyDbId() {
        return apperyDbId;
    }

    public void setApperyDbId(String apperyDbId) {
        this.apperyDbId = apperyDbId;
    }

    public String getApperyMasterKey() {
        return apperyMasterKey;
    }

    public void setApperyMasterKey(String apperyMasterKey) {
        this.apperyMasterKey = apperyMasterKey;
    }

}
