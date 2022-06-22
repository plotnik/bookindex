package io.github.plotnik;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.lang.System.*;
import static io.github.plotnik.Main.verbose;

/**
 * Файл с текущими настройками.
 * По дефолту мы храним его в каталоге пользователя
 * под именеме `~/.plotnik/bookindex.properties`.
 */
public class Settings {

    /**
     * Путь к папке с книгами для определенного месяца,
     * например `~/Dropbox/Public/books/18-06`
     */
    private String folder;

    private String monthStamp;

    private String apperyDbId;
    private String apperyMasterKey;

    private List<Book> books;

    String targetFolderPath = System.getProperty("user.home") + "/.plotnik";
    String targetPropName = targetFolderPath + "/bookindex.properties";
    Properties pp = new Properties();


    public String getFolder() {
        return folder;
    }

    public String getMonthStamp() {
        return monthStamp;
    }

    public void setFolder(String folder) throws BookException {
        File f = new File(folder);
        if (!f.exists()) {
            throw new BookException("Folder not found: " + folder);
        }
        String mstamp = f.getName();
        if (!Pattern.matches("\\d{2}-\\d{2}", mstamp)) {
            throw new BookException("Folder name should be a month stamp: " + f.getPath());
        }
        monthStamp = mstamp;
        File b = new File(folder, "books.xml");
        if (!b.exists()) {
            throw new BookException("Missing `books.xml` file in folder: " + f.getPath());
        }
        this.folder = folder;
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

    /**
     * Загрузить файл с текущими настройками.
     *
     * @param propName  Имя файла настроек.
     *                  Для `null` или пустой строки будет использовано значение по умолчанию.
     * @throws FileNotFoundException
     * @throws IOException
     */
    void loadProperties(String propName) throws FileNotFoundException, IOException {
        if (propName == null || propName.length() == 0) {
            propName = targetPropName;
        }

        File f = new File(propName);
        if (f.exists()) {
            FileInputStream fin = new FileInputStream(f.getPath());
            pp.load(fin);
            fin.close();

            setFolder(getSetting("folder"));
            apperyDbId = getSetting("appery_db_id");
            apperyMasterKey = getSetting("appery_master_key");
        }

        if (verbose) {
            out.println("Using properties: " + propName);
            out.println(pp.toString());
        }
    }

    String getSetting(String name) {
        String value = pp.getProperty(name);
        if (value == null) {
            throw new BookException("Property required: " + name);
        }
        return value;
    }

    /** We are changing properties in `user.home` only. */
    void saveProperties() throws FileNotFoundException, IOException {
        // update `folder` value
        pp.setProperty("folder", folder);

        // check if folder in `user.home` exists
        File targetFolder = new File(targetFolderPath);
        if (!targetFolder.exists()) {
            targetFolder.mkdir();
        }

        // write property-file
        FileOutputStream out = new FileOutputStream(targetPropName);
        pp.store(out, null);
        out.close();
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

}
