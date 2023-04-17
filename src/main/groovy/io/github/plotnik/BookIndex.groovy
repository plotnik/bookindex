package io.github.plotnik;

import groovy.xml.*

public class BookIndex {

    String bookHome;
    int bookFolderDepth;
    boolean verbose;

    String[] ignoreFolders = ['bookindex','code'];

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


    BookIndex(String bookHome, int bookFolderDepth, boolean verbose) {
        this.bookHome = bookHome;
        this.bookFolderDepth = bookFolderDepth;
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
        for (String xmlName in xmlNames) {
            // извлекаем из пути к дескриптору "books.xml" сигнатуру месяца
            String dirPath = xmlName[0..-1-'/books.xml'.length()]
            String dirName = dirPath[-5..-1]  // сигнатура месяца
            print "[$dirName]"
            if (verbose) {
                println ""
            }


            def document = new XmlParser().parse(new FileReader(xmlName))
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
                    Book bookInfo = new Book()
                    bookInfo.name = book['@title']
                    bookInfo.author = book['@author']
                    bookInfo.source = book['@source']
                    bookInfo.folder = dirName

                    // отметиться в уникальном соответствии книг секциям allSections
                    //addBookForSection(allSections, bookInfo, section['@name'])

                    // отметиться в дубликатном соответствии книг секциям allSections2
                    addBookForSection(allSections2, bookInfo, section['@name'])
                    def moreSections = book.section
                    moreSections.each { sect ->
                        addBookForSection(allSections2, bookInfo, sect['@name'])
                    }
                }
            }
            pdfChecker.verifyAllPdfsAdded(dirPath, dirName)
        }
        // завершаем вывод папок в одну строку
        println ""
    }

    void scanDirList(File[] dirs, int depth) {
        for (File dir in dirs) {
            if (!dir.isDirectory()) continue;
            if (ignoreFolders.contains(dir.name)) continue;
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
    void addBookForSection(allSections, bookInfo, sectionName) {
        def bookList = allSections.get(sectionName)
        if (bookList==null) {
            bookList = new LinkedList()
            allSections.put(sectionName, bookList)
        }
        bookList.add(bookInfo)
    }

    /** Remove file extension */
    String nameOnly(String fname) {
        int k = fname.lastIndexOf('.')
        return fname.substring(0,k+1)
    }

    void generateAllSectionsHtml(String indexName) {
        // allSections содержит уникальное соответствие книг секциям
        // allSections2 содержит дубликатное соответствие книг секциям
        generateAllSectionsHtml(indexName, allSections2)
    }

    /**
     * Generates "all_sections.html" file aka "Book Index"
     */
    def generateAllSectionsHtml(String indexName, Map<String, List<Book>> allSections) {
        def keys = new ArrayList(allSections.keySet())
        keys.sort { it.toLowerCase() }

        def excludedSections = ['Palm C']
        keys.removeAll(excludedSections)

        def writer2 = new FileWriter(indexName)
        writer2.println '''<html>
                           <head>
                             <meta charset="UTF-8">
                             <title>Book Index</title>
                             <style>
                             .other-sections {
                               padding-left: 40px;
                               font-size: small;
                             }
                             </style>
                           </head>
                           <body>
                           <font face="Georgia">
                           '''

        /* Записать в html-файл заголовки секций
         */
        for (key in keys) {
            writer2.println "<a href='#${URLEncoder.encode(key)}'>$key</a> - "
        }
        writer2.println "<hr/>"

        for (key in keys) {
            writer2.println """<a name='${URLEncoder.encode(key)}'></a>
                               <h3>$key</h3>
                               <ul>"""
            def bookList = allSections.get(key)
            def otherSections = new HashSet()
            for (bookInfo in bookList) {
                // собрать строку для html файла
                String folderLink = '../'+bookInfo.folder // 'file:///'+bookInfo.addr.substring(0, bookInfo.addr.lastIndexOf("/"))
                writer2 << "<li> <a href='${folderLink}/books.html'><code>${bookInfo.folder}</code></a> "+
                           "<i>${bookInfo.author}</i> "+
                           "<a href='${folderLink}/${bookInfo.source}.mm.html'>\"${bookInfo.name}\"</a> </li>\n"
                otherSections.addAll(getOtherSections(allSections, bookInfo))
            }
            writer2.println "</ul>"

            /* Вывести линки на другие секции для книг этой секции
             */
            otherSections.remove(key)
            writer2.println "<div class='other-sections'>"
            for (key2 in otherSections) {
                writer2.println "+ <a href='#${URLEncoder.encode(key2)}'>$key2</a>"
            }
            writer2.println "</div>"
        }
        writer2.println '''</font>
                           </body>
                           </html>'''
        writer2.close()
        println "Book Index created: " + indexName
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
