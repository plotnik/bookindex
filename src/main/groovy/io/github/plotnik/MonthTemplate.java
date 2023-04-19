package io.github.plotnik;

import static java.lang.System.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class MonthTemplate {

    public static String bootstrapCDN = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3";

    String dirPath;
    String dirName;
    int bookFolderDepth;
    String indexPath;
    List<Book> books;
    String prevXmlName; 
    String nextXmlName;
    String depthPrefix;

    MonthTemplate(String dirPath, String dirName, int bookFolderDepth, 
                  String indexName,
                  List<Book> books,
                  String prevXmlName, String nextXmlName) {
        this.dirPath = dirPath;
        this.dirName = dirName;
        this.bookFolderDepth = bookFolderDepth;
        this.books = books;
        this.prevXmlName = prevXmlName;
        this.nextXmlName = nextXmlName;
 
        this.indexPath = Path.of(dirPath).relativize(Path.of(indexName)).toString();
 
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<bookFolderDepth; i++) {
            sb.append("../");
        }
        depthPrefix = sb.toString();

        //for (Book book: books) { System.out.println("\n- " + book); }
    }

    @Override
    public String toString() {
        return "MonthTemplate [dirPath=" + dirPath + ", dirName=" + dirName + ", bookFolderDepth=" + bookFolderDepth
                + "]";
    }

    boolean createHtml() throws FileNotFoundException {
        String outFile = dirPath + "/books.html";
        String text = String.format("""
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>%s</title>
                <link href="%s/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>
            <body>
                <div class="container">
                %s
                %s
                </div>
                <script src="%s/dist/js/bootstrap.bundle.min.js"></script>
            </body>
            </html>
            """,
            dirName, bootstrapCDN, createHeader(), createContent(), bootstrapCDN);
        return StrUtils.saveIfNeeded(outFile, text);
    }

    String createHeader() {
        return String.format("""
            <div class="row bg-primary bg-opacity-50 mb-3">
                <div class="col text-start">%s</div>
                <div class="col text-center">%s</div>
                <div class="col text-end">%s</div>
            </div>
            """, 
            headerLink(prevXmlName), dirName, headerLink(nextXmlName));
    }

    String headerLink(String xmlName) {
        if (xmlName == null) {
            return "";
        }
        // извлекаем из пути к дескриптору "books.xml" сигнатуру месяца
        String dirPath = xmlName.substring(0, xmlName.length() - "/books.xml".length());
        String dirName = dirPath.substring(dirPath.length()-5);  // сигнатура месяца
        return String.format("<a href=\"%s/books.html\">%s</a>", depthPrefix + dirPath, dirName);
    }

    String createContent() {
        if (books.size() == 0) {
            return "";
        }
        // Собрать карточки для книг из списка
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <div class="row gy-2">
                """);
        for (int i = 0; i < books.size(); i++) {
            sb.append(createCard(books.get(i)));
            // если индекс кратен 6, но не первый, и не последний
            if (i % 6 == 5 && i != books.size() - 1) {
                sb.append("""
                        </div>
                        <div class="row">
                        """);
            }
        }
        sb.append("</div>");
        return sb.toString();
    }

    String createCard(Book book) {
        String imageLink = "img/" + book.getName() + ".jpg";
        return String.format("""
            <div class="card mb-3" style="max-width: 500px;">
                <div class="row g-0">
                    <div class="col-md-4">
                    <img src="%s" class="img-fluid rounded-start" alt="%s">
                    </div>
                    <div class="col-md-8">
                    <div class="card-body">
                        <h5 id="%s" class="card-title">%s</h5>
                        <p class="card-text">%s</p>
                        %s
                        %s
                    </div>
                    </div>
                </div>
            </div>
            """,
            imageLink, 
            book.getTitle(),
            book.getSource(),
            book.getTitle(),
            book.getAuthor(),
            createLinks(book),
            createSections(book));
    }

    String createLinks(Book book) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul class=\"list-group list-group-flush\">");
        for (String link: book.getLinks()) {
            sb.append(String.format("""
              <li class="list-group-item">
                <a href="%s">%s</a>
              </li>
              """, 
              link, link));
        }
        sb.append("</ul>");
        return sb.toString();
    }

    String createSections(Book book) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul class=\"nav nav-pills card-footer-pills\">");
        for (String section: book.getSections()) {
            String sectionLink = ""; 
            try {
                sectionLink = indexPath + "#" + URLEncoder.encode(section, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
            }
            sb.append(String.format("""
              <li class="nav-item">
                <a class="nav-link" href="%s">%s</a>
              </li>
              """, 
              sectionLink, section));
        }
        sb.append("</ul>");
        return sb.toString();
    }

}
