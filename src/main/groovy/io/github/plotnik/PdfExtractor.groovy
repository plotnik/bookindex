package io.github.plotnik;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import javax.swing.*;

public class PdfExtractor {

    final boolean useSwingProgress = true;
    final boolean createTocHtml = true;

    IConsole console;
    String bookFolder;
    String bookTitle;

    FileWriter writer;
    FileWriter toc;
    String content;
    int tocNum;

    PdfExtractor(IConsole console) {
        this.console = console;
        bookFolder = new File(".").getCanonicalPath() + "/";
    }

    // Check "books.xml" for book code
    def getBookCode(bookName) {
        def books = new XmlSlurper().parseText(new File(bookFolder+"books.xml").text)
        //println "books: "+books
        for (def section in books.section) {
            //println "section: "+section
            for (def book in section.book) {
                //println "book: "+book
                if (book.@name==bookName) {
                    bookTitle = book.@title
                    return book.@source.text()
                }
            }
        }
        return null
    }

    public int process(String bookName) {

        if (!new File(bookName).exists()) {
            console.error("[ERROR] File not found: " + bookName);
            return 1;
        }

        /* Generate ".mm" and ".toc.html" files from PDF
         */
        String bookCode = getBookCode(bookName);
        console.log("$bookName in $bookFolder");

        if (bookCode==null) {
            console.error("$bookName not found in books.xml");
            return 1;
        }

        String mmname = bookCode + ".mm"
        String tocname = bookCode + ".toc.html"
        boolean overwriteMM = true
        if (new File(bookFolder+mmname).exists()) {
            int ok = JOptionPane.showConfirmDialog(
                console.frame,
                "$mmname already exists. Overwrite?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
            if (ok!=0) {
                return 1;
            }
        }

        def doc = null
        def root = null
        try {
            doc = PDDocument.load(new File(bookFolder+bookName));
            root = doc.getDocumentCatalog().getDocumentOutline();
            //println "root: "+root
        } catch(Exception ex) {
            ex.printStackTrace()
            JOptionPane.showMessageDialog(console.frame, "Exception: "+ex.getMessage(),
                        "pdf2mm", JOptionPane.ERROR_MESSAGE);
            return 1
        }

        if (overwriteMM) {
            writer = new FileWriter(mmname)
        }
        if (createTocHtml) {
            toc = new FileWriter(tocname)
        }
        content = ''

        // print ".toc.html" header
        if (createTocHtml) {
            tocNum = 0
            toc.println """<!DOCTYPE html>
                <html>

                <head>
                    <title>$bookName</title>

                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">

                    <link href="../../js/bootstrap-3.1.1-dist/css/bootstrap.min.css" rel="stylesheet">

                    <script src="../../js/jquery-1.11.0.min.js"></script>
                    <script src="../../js/bootstrap-3.1.1-dist/js/bootstrap.min.js"></script>

                    <!--[if lt IE 9]>
                        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/
                        html5shiv.js"></script>
                        <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/
                        respond.min.js"></script>
                    <![endif]-->
                </head>

                <body>

                <div class="container">

                    <h1>$bookName</h1>
                    <ul class="list-group">
                    """.stripIndent()
        }

        PDOutlineItem item = root.getFirstChild();
        goThroughNodes(item);

        print "\n"

        // print `content` into ".mm" file

        def xml = """<map version="0.9.0">
        <!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
        <node TEXT="$bookFolder">
        <node TEXT="$bookTitle">
        $content
        </node>

        <node POSITION="left">
        <richcontent TYPE="NODE"><html>
          <head>

          </head>
          <body>
            <img src="img/${bookName}.jpg" />
          </body>
        </html>
        </richcontent>
        </node>

        <node LINK="${bookName}" POSITION="left" TEXT="PDF"/>

        </node>
        </map>"""

        if (overwriteMM) {
            writer << xml
            writer.close()
            println "$mmname created"
        }

        if (createTocHtml) {
            toc.println "</ul></div></body></html>"
            toc.close()
            println "$tocname created"
        }

        if (useSwingProgress) {
            JOptionPane.showMessageDialog(console.frame, "$mmname created");
        }

        return 0;
    }

    // Visit TOC items in PDF
    void goThroughNodes(PDOutlineItem item) {
        while (item != null) {
            PDOutlineItem child = item.getFirstChild();
            def title = item.title.replaceAll("&","&amp;")
                                  .replaceAll("\"","&quot;")
                                  .replaceAll("<","&lt;")
                                  .replaceAll("<","&gt;");
            if (child!=null) {
                content += /<node TEXT="${title}" FOLDED="true">/
                // add Bootstrap collapsible to ".toc.html"
                if (createTocHtml) {
                    tocNum++
                    toc.println """
                      <div class="panel panel-default">
                        <div class="panel-heading" role="tab" id="heading${tocNum}">
                          <h4 class="panel-title">
                            <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse${tocNum}" aria-expanded="false" aria-controls="collapse${tocNum}">
                              ${title}
                            </a>
                          </h4>
                        </div>
                        <div id="collapse${tocNum}" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading${tocNum}">
                          <div class="panel-body">
                            <ul class="list-group">""".stripIndent()
                }

            } else {
                content += /<node TEXT="${title}">/
                if (createTocHtml) {
                    toc.println """<li class="list-group-item">${title}</li>"""
                }
            }
            printDot();

            goThroughNodes(child);
            item = item.getNextSibling();

            content += '</node>\n'
            if (child!=null) {
                // close Bootstrap collapsible in ".toc.html"
                if (createTocHtml) {
                    toc.println """
                            </ul>
                          </div>
                        </div>
                      </div>""".stripIndent()
                }
            }
        }
    }

    int lastLineLength(String s) {
        int k = s.lastIndexOf('>');
        return s.length()-k-1;
    }

    def printDot() {
        console.append(". ");
    }
}