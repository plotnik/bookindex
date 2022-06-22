package io.github.plotnik;

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
    name = "bookindex", mixinStandardHelpOptions = true, version = "1.1",
    description = "Generate html index of my library that contains a lot of PDF books.")
public class Main implements Runnable {

    @Option(names = {"-p", "--props"}, description = "Name of property file.")
    String propertyFileName;

    @Option(names = {"-d", "--dashboard"}, description = "Open dashboard")
    boolean dashboard;

    @Option(names = {"-b", "--books"}, description = "Books home folder")
    String bookHome;

    @Option(names = {"-f", "--file"}, description = "Input PDF file")
    String inputFilePdf;

    @Option(names = {"-s", "--safe"}, description = "Ask for confirmations when rewriting generated files")
    static boolean safeMode;

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    static boolean verbose;


    Object lock = new Object();

    Settings settings = new Settings();

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    public void run() {
        try {
            /* Загрузить файл с текущими настройками.
             */
            settings.loadProperties(propertyFileName);

            if (dashboard) {
                openDashboard();
            }

            if (bookHome != null) {
                createBookIndex();
            }

            if (inputFilePdf != null) {
                extractTOC(inputFilePdf);
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof BookException) {
                msg = ((BookException) e).getReason();
            }
            out.println("[ERROR] " + msg);
            //e.printStackTrace();
        }
    }

    void createBookIndex() {
        BookIndex bookIndex = new BookIndex(bookHome);
        bookIndex.scanBooksXml();
        bookIndex.generateAllSectionsHtml("all_sections.html");
    }

    void extractTOC(String inputFilePdf) {
        Console console = new Console();
        console.setTitle("bookindex");
        console.setVisible(true);
        PdfExtractor pe = new PdfExtractor(console, safeMode);
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

}