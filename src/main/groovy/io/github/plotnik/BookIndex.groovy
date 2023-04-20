package io.github.plotnik;

import groovy.xml.*

import java.nio.file.Path;

public class BookIndex {

    String bookHome;
    Path bookHomePath;
    int bookFolderDepth;
    String indexName;
    boolean obsidianOnly;
    boolean verbose;

    XmlParser xmlParser = new XmlParser();

    /** Пути к найденным файлам `books.xml` */
    List<String> xmlNames = [];

    /** Названия книг */
    List<String> names = [];

    /** Местоположения книг */
    Map<String, String> address = new HashMap<>();

    /** Номер строки в файле по имени книги */
    Map<String, Integer> lineno = new HashMap<>();

    PdfChecker pdfChecker = new PdfChecker();

    /**
    Структура словаря `allSections`
    -------------------------------
    allSections = [
            'Spring':[
                    BookInfo(name:'Pro Spring 3',folder:'12-06'),
                    BookInfo(name:'Spring Roo in Action',folder:'12-07'),
            ],
            'JavaScript':[
                    BookInfo(name:'Pro Android Web Apps',folder:'11-12'),
            ]
    ]
    В словаре `allSections` каждая книга встречается только один раз и присоединена к своей главной секции.
    В словаре `allSections2` книга присоединена к каждой своей секции.
    */
    //Map<String, List<Book>> allSections = new HashMap<>()  // уникальное соответствие книг секциям
    Map<String, List<Book>> allSections2 = new HashMap<>() // дубликатное соответствие книг секциям

    /**
     `allBooks` содержит книги за последний обработанный месяц
     */
    List<Book> allBooks = new ArrayList<>();

    /**
     `allBooks2` содержит все книги библиотеки
     */
    List<Book> allBooks2 = new ArrayList<>();


    BookIndex(String bookHome, 
              int bookFolderDepth, 
              String indexName,
              boolean obsidianOnly, 
              boolean verbose) {
        this.bookHome = bookHome;
        this.bookHomePath = Path.of(bookHome);
        this.bookFolderDepth = bookFolderDepth;
        this.indexName = indexName;
        this.obsidianOnly = obsidianOnly;
        this.verbose = verbose;
    }

    void scanBooksXml() throws FileNotFoundException {

        /* Собрать имена файлов "books.xml", пропуская `ignoreFolders`.
           Отсортировать их в обратном порядке, чтобы более новые папки
           индексировались первыми.
         */
        File home = new File(bookHome); 
        if (bookFolderDepth == 0) {
            addBooksXml(home);
        } else {
            File[] dirs = home.listFiles();
            scanDirList(dirs, bookFolderDepth);
        }
        xmlNames.sort { a,b -> -a.compareTo(b) }

        /* Основной цикл, в котором мы проходим подряд все файлы "books.xml"
           и печатаем их сигнатуры в консоли.
         */
        println "Updating html pages for each month:"
        int pagesUpdated = 0 
        for (int n=0; n<xmlNames.size(); n++) {
            String xmlName = xmlNames.get(n);
            // извлекаем из пути к дескриптору "books.xml" сигнатуру месяца
            String dirPath = xmlName[0..-1-'/books.xml'.length()]
            String dirName = dirPath[-5..-1]  // сигнатура месяца
            
            allBooks2.addAll(allBooks);
            allBooks = new ArrayList<>();
            print "[$dirName]"
            if (verbose) {
                println ""
            }

            def document = xmlParser.parse(new FileReader(xmlName))
            def sections = document.section
            sections.each { section ->
                if (verbose) {
                    println "section: ${section['@name']}"
                }
                def books = section.book
                books.each { book ->
                    String addr = dirPath+'/'+book['@name']
                    if (verbose) {
                        println "  "+book['@name']
                    }
                    names << book['@title']
                    address.put(book['@title'], addr)
                    lineno.put(book['@title'], dirPath)
                    pdfChecker.addName(book['@name'])

                    /* Создать book info
                     */
                    Book b = new Book()
                    b.name = book['@name']
                    b.title = book['@title']
                    b.author = book['@author']
                    b.source = book['@source']
                    b.folder = dirName
                    b.path = dirPath

                    b.obsidian = StrUtils.getObsidianLink(dirPath, b.source)

                    book.a.each {
                        b.links.add(it['@href'])
                    }

                    if (b.obsidian != null) {
                        b.links.add(b.obsidian)
                    }

                    // добавить книгу в общий список
                    allBooks.add(b)

                    // отметиться в дубликатном соответствии книг секциям allSections2
                    addBookForSection(allSections2, b, section['@name'])
                    def moreSections = book.section
                    moreSections.each { sect ->
                        addBookForSection(allSections2, b, sect['@name'])
                    }
                    if (b.obsidian != null) {
                        addBookForSection(allSections2, b, "Obsidian")
                    }
                }
            }
            if (!obsidianOnly) {
                pdfChecker.verifyAllPdfsAdded(dirPath, dirName)
                
                // сгенерировать html-файл для текущего месяца 
                String nextXmlName = (n==0)? null : xmlNames.get(n-1); 
                String prevXmlName = (n==xmlNames.size()-1)? null : xmlNames.get(n+1); 
                MonthTemplate monthTemplate = new MonthTemplate(
                    dirPath, dirName, bookFolderDepth, 
                    indexName, allBooks, 
                    prevXmlName, nextXmlName)
                if (monthTemplate.createHtml()) {
                    pagesUpdated++
                }
            }
        }
        println "\n-----------------"
        println pagesUpdated + " page(s) updated"
    }

    void scanDirList(File[] dirs, int depth) {
        for (File dir in dirs) {
            if (!dir.isDirectory()) continue;
            if (dir.name.startsWith("-")) continue;
            if (depth == 1) {
                addBooksXml(dir);
            } else {
                File[] dirs2 = dir.listFiles();
                scanDirList(dirs2, depth - 1);
            }
        }
    }

    void addBooksXml(File dir) {
        String xmlName = dir.getPath()+'/books.xml'
        xmlNames << xmlName
    }

    /**
     В словаре `allSections` присоединяет информацию о книге `bookInfo`
     к секции `sectionName`.
     */
    void addBookForSection(Map<String, List<Book>> allSections, Book book, String sectionName) {
        def bookList = allSections.get(sectionName)
        if (bookList==null) {
            bookList = new LinkedList()
            allSections.put(sectionName, bookList)
        }
        bookList.add(book)
        book.sections.add(sectionName)
    }

    /** Remove file extension */
    String nameOnly(String fname) {
        int k = fname.lastIndexOf('.')
        return fname.substring(0,k+1)
    }

    void showObsidian() {
        println "Find Obsidian books:"
        int ocount = 0;
        for (Book b: allBooks2) {
            if (b.obsidian != null) {
                println "- ${b.path}/${b.obsidian}"
                ocount++
            }
        }
        println "-----------------"
        println "Books in library: Total: " + allBooks2.size() + " / Obsidian: " + ocount
    }

    boolean generateAllSectionsHtml() {
        // allSections содержит уникальное соответствие книг секциям
        // allSections2 содержит дубликатное соответствие книг секциям
        return generateAllSectionsHtml(allSections2)
    }

    /**
     * Generates "all_sections.html" file aka "Book Index"
     */
    boolean generateAllSectionsHtml(Map<String, List<Book>> allSections) {
        String bootstrapCDN = MonthTemplate.bootstrapCDN;

        def keys = new ArrayList(allSections.keySet())
        keys.sort { it.toLowerCase() }

        def excludedSections = ['Palm C']
        keys.removeAll(excludedSections)

        StringWriter stw = new StringWriter();
        def writer2 = new PrintWriter(stw)
        writer2.println """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Book Index</title>
                <link href="${bootstrapCDN}/dist/css/bootstrap.min.css" rel="stylesheet">
                <style>
                    .other-sections {
                        padding-left: 40px;
                        font-size: small;
                    }
                </style>
            </head>
            <body>
                <div class="container">
            """.stripIndent()

        /* Записать в html-файл заголовки секций
         */
        writer2.println '<div class="alert alert-primary" role="alert">'

        for (key in keys) {
            writer2.println "<a href='#${URLEncoder.encode(key)}'>$key</a> - "
        }
        writer2.println "</div>"

        for (key in keys) {
            writer2.println """
                <a name='${URLEncoder.encode(key)}'></a>
                <h6><strong>$key</strong></h6>
                <ul>
                """.stripIndent()
            def bookList = allSections.get(key)
            def otherSections = new HashSet()
            for (b in bookList) {

                String bookLink = calcBookLink(b.path) 
                 
                // собрать строку для html файла
                writer2 << "<li> <a href='${bookLink}'><code>${b.folder}</code></a> "+
                           "<i>${b.author}</i> "+
                           "<a href='${bookLink}#${b.source}'>\"${b.title}\"</a> </li>\n"
                otherSections.addAll(getOtherSections(allSections, b))
            }
            writer2.println "</ul>"

            /* Вывести линки на другие секции для книг этой секции
             */
            otherSections.remove(key)
            writer2.println "<div class='other-sections'>"
            for (key2 in otherSections) {
                writer2.println "+ <a class='badge text-bg-light' href='#${URLEncoder.encode(key2)}'>$key2</a>"
            }
            writer2.println "</div>"
            writer2.println "<hr/>"
        }
        writer2.println """  
            </div> <!-- container -->              
            <script src="${bootstrapCDN}/dist/js/bootstrap.bundle.min.js"></script>
            </body>
            </html>
            """.stripIndent()

        //writer2.close()
        return StrUtils.saveIfNeeded(indexName, stw.toString())
    }

    String calcBookLink(String bookPath) {
        return bookHomePath.relativize(Path.of(bookPath, "books.html")).toString();
    }

    def getOtherSections(allSections, bookInfo) {
        def result = []
        def keys = allSections.keySet()
        for (String key: keys) {
            if (allSections.get(key).contains(bookInfo))
                result << key
        }
        return result
    }
}
