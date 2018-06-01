package io.github.plotnik

import javax.swing.DefaultListModel

println "-- bookindex"
println "   Parameter: property_file"

// http://mvnrepository.com
// https://github.com/a-services/apperyunit/blob/master/src/main/groovy/io/appery/apperyunit/DashboardFrame.java
// https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html


propName = null
if (args.length > 0) {
    propName = args[0]
}

try {
    settings = new Settings()
    settings.loadProperties(propName)

    books = new XmlSlurper().parseText(new File(settings.folder, 'books.xml').text)
    settings.books = []
    
    listModel = new DefaultListModel();
    for (section in books.section) {
        for (book in section.book) {
            listModel.addElement(book.@title.toString())
            settings.books.add(new Book(book, section.@name, settings.getMonthStamp()))
        }
    }
    
    dbd = new DashboardFrame(listModel)
    dbd.setSettings(settings)
    dbd.setVisible(true)
    
} catch(BookException e) {
    println "[ERROR] " + e.reason
}