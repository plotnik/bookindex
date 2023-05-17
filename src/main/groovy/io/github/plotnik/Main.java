package io.github.plotnik;

import static java.lang.System.err;
import static java.lang.System.out;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.ListModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import picocli.CommandLine;
import static picocli.CommandLine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


@Command(header = {
    "@|cyan    888                        888      d8b               888                   |@",
    "@|cyan    888                        888      Y8P               888                   |@",
    "@|cyan    888                        888                        888                   |@",
    "@|cyan    88888b.   .d88b.   .d88b.  888  888 888 88888b.   .d88888  .d88b.  888  888 |@",
    "@|cyan    888 \"88b d88\"\"88b d88\"\"88b 888 .88P 888 888 \"88b d88\" 888 d8P  Y8b `Y8bd8P' |@",
    "@|cyan    888  888 888  888 888  888 888888K  888 888  888 888  888 88888888   X88K   |@",
    "@|cyan    888 d88P Y88..88P Y88..88P 888 \"88b 888 888  888 Y88b 888 Y8b.     .d8\"\"8b. |@",
    "@|cyan    88888P\"   \"Y88P\"   \"Y88P\"  888  888 888 888  888  \"Y88888  \"Y8888  888  888 |@",
    ""
    },
    name = "bookindex", mixinStandardHelpOptions = true, version = "1.2",
    description = "Generate html index of my library with PDF books on programming.")
public class Main implements Callable<Integer> {

    @Parameters(index = "0", description = "Books home folder",
                defaultValue = ".")
    String bookHome;
    
    @Option(names = {"-p", "--props"}, description = "Name of property file.")
    String propertyFileName;

    @Option(names = {"-g", "--gui-dashboard"}, description = "Open GUI dashboard")
    boolean dashboard;

    @Option(names = {"-f", "--file"}, description = "PDF file to extract TOC as mindmap")
    String inputFilePdf;

    @Option(names = {"-d", "--dropbox"}, description = "Dropbox folder")
    String dropboxFolder;

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    static boolean verbose;

    @Option(names = {"-o", "--obsidian"}, description = "Show Obsidian books")
    static boolean checkObsidianMode;


    Object lock = new Object();

    Settings settings = new Settings();

    BookIndex bookIndex;

    // Depth of book folders.        
    int bookFolderDepth;

    // Index output file
    String indexFile;

    @Override
    public Integer call() {
        try {
            if (dashboard) {
                /* Загрузить файл с текущими настройками.
                */
                settings.loadProperties(propertyFileName);
                openDashboard();
            }

            if (bookHome.startsWith("~/")) {
                bookHome = System.getProperty("user.home") + bookHome.substring(1);
            }

            if (inputFilePdf != null) {
                extractTOC(inputFilePdf);
                return 0;
            }
                        
            calcBookFolderDepth(bookHome);
            indexFile = Path.of(bookHome, "all_sections.html").toString();
            out.println("Book Index: " + indexFile);

            if (dropboxFolder == null) {
                String dropbox = System.getProperty("user.home") + "/Dropbox/Public/books";
                if (Files.exists(Path.of(dropbox))) {
                    dropboxFolder = dropbox;
                }
            }
        
            bookIndex = new BookIndex(bookHome, bookFolderDepth, indexFile, 
                                      dropboxFolder, checkObsidianMode, verbose);

            try {
                bookIndex.scanBooksXml();

                if (checkObsidianMode) {
                    bookIndex.showObsidianChecks();
                    return 0;
                }

                if (bookIndex.generateAllSectionsHtml()) {
                    out.println("Book Index updated: " + indexFile);
                }

            } catch(FileNotFoundException e) {
                err.println("\n[ERROR] " + e.getMessage());
                return 1;
            }


        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof BookException) {
                msg = ((BookException) e).getReason();
            }
            out.println("[ERROR] " + msg);
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    void calcBookFolderDepth(String bookHome) throws BookException {
        Path p = Path.of(bookHome);
        int k = p.getNameCount();
        String root = p.getName(0).toString();
        if (k > 2) {
            throw new BookException("Books folder must have no more than 2 subfolders");
        }
        if ("..".equals(root)) {
            throw new BookException("Books folder must be inside the current folder");
        }
        if (".".equals(root)) {
            k--;
        }

        bookFolderDepth = 2 - k;
    }

    void extractTOC(String inputFilePdf) {
        Console console = new Console();
        console.setTitle("bookindex");
        console.setVisible(true);
        PdfExtractor pe = new PdfExtractor(console, true);
        try {
            int result = pe.process(inputFilePdf);
            console.log("\n== Result: " + (result==0? "SUCCESS":"ERROR"));

        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof BookException) {
                msg = ((BookException) e).getReason();
            }
            out.println("[ERROR] " + msg);
            console.error("[ERROR] " + msg);
            //e.printStackTrace();
        }

        waitUntilClosed(console);
    }

    void openDashboard() {
        /* Загрузить список книг из текущей папки в `ListModel` для виджета списка.
         */
        DefaultListModel listModel = new DefaultListModel();
        updateListModel(listModel, settings);

        /* Открыть окно дашборда.
         */
        DashboardFrame dbd = new DashboardFrame(listModel);
        dbd.setSettings(settings);
        dbd.setVisible(true);

        waitUntilClosed(dbd);
    }

    void waitUntilClosed(JFrame frame) {
        try {
            Thread t = new Thread() {
                public void run() {
                    synchronized (lock) {
                        while (frame.isVisible()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //out.println("Working now");
                    }
                }
            };
            t.start();

            frame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent arg0) {
                    synchronized (lock) {
                        frame.setVisible(false);
                        lock.notify();
                    }
                }

            });

            t.join();
        } catch (InterruptedException e) {
            out.println("[InterruptedException] " + e.getMessage());
        }
    }

    /**
    * Загрузить список книг из текущей папки в `ListModel` для виджета списка.
    */
    static void updateListModel(DefaultListModel listModel, Settings settings) {
        listModel.clear();
        List<Book> books = GroovyUtils.loadBooks(settings.getFolder(), "books.xml",
             settings.getMonthStamp());
        for (Book book: books) {
            listModel.addElement(book.getTitle());
        }
        settings.setBooks(books);

        /*
        try {
            String xml = StrUtils.loadStr(settings.getFolder(), "books.xml");
            XmlMapper xmlMapper = new XmlMapper();
            List<Section> sections = xmlMapper.readValue(xml, new TypeReference<List<Section>>() {
            });
        } catch (JsonProcessingException e) {
            out.println("[JsonProcessingException] " + e.getMessage());
        }
        */
    }
    
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

}