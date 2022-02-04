package io.github.plotnik;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

public class PdfExtractor {

    IConsole console;
    String bookFolder;
    String bookTitle;

    public PdfExtractor(IConsole console) {
        this.console = console;
        bookFolder = new File(".").getCanonicalPath() + "/";
    }

    public void process(String bookName) {

        if (!new File(bookName).exists()) {
            console.error("[ERROR] File not found: " + bookName);
            return;
        }

        String bookCode = getBookCode(bookName);
        console.log("$bookName in $bookFolder");
    }

    String getBookCode(bookName) {
        def books = new XmlSlurper().parseText(new File(bookFolder+"books.xml").text)
        for (def section in books.section) {
            for (def book in section.book) {
                if (book.@name==bookName) {
                    bookTitle = book.@title
                    return book.@source.text()
                }
            }
        }
        return null
    }
}
