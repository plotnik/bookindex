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

import picocli.CommandLine;
import static picocli.CommandLine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/* Useful links:
 *
 * ApperyUnit Dashboard::
 * https://github.com/a-services/apperyunit/blob/master/src/main/groovy/io/appery/apperyunit/DashboardFrame.java
 *
 * Swing FileChooser::
 * https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
 */

@Command(name = "bookindex", mixinStandardHelpOptions = true, version = "1.0", description = "Generate html index of my library that contains a lot of PDF books.")
public class bookindex implements Runnable {

    @Parameters(index = "0", description = "Name of property file.", defaultValue = "")
    String propertyFileName;

    @Option(names = "--noui", description = "No UI needed")
    boolean noUI;

    Object lock = new Object();

    public static void main(String[] args) {
        System.exit(new CommandLine(new bookindex()).execute(args));
    }

    public void run() {
        try {
            /* Загрузить файл с текущими настройками.
            */
            Settings settings = new Settings();
            settings.loadProperties(propertyFileName);

            /* Загрузить список книг из текущей папки в `ListModel` для виджета списка.
            */
            DefaultListModel listModel = new DefaultListModel();
            updateListModel(listModel, settings);

            if (noUI) {
                return;
            }

            /* Открыть окно дашборда.
            */
            DashboardFrame dbd = new DashboardFrame(listModel);
            dbd.setSettings(settings);
            dbd.setVisible(true);

            try {
                waitUntilClosed(dbd);
            } catch (InterruptedException e) {
                out.println("[InterruptedException] " + e.getMessage());
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof BookException) {
                msg = ((BookException) e).getReason();
            }
            out.println("[ERROR] " + msg);
            e.printStackTrace();
        }
    }

    void waitUntilClosed(JFrame frame) throws InterruptedException {
        Thread t = new Thread() {
            public void run() {
                synchronized (lock) {
                    while (frame.isVisible())
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    out.println("Working now");
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