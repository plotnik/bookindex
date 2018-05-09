package io.github.plotnik

println "-- bookindex"

// http://mvnrepository.com
// https://github.com/a-services/apperyunit/blob/master/src/main/groovy/io/appery/apperyunit/DashboardFrame.java
// https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html


pp = new Properties();

void loadProperties() {
    String userHome = System.getProperty('user.home')
    File f = new File(userHome, '.plotnik/bookindex.properties')
    if (f.exists()) {
        FileInputStream fin = new FileInputStream(f.path);
        pp.load(fin);
        fin.close();
    }
}

loadProperties()

String getFolder() {
    String path = pp.getProperty('folder') 
    if (path==null) {
        path = new File('.').canonicalPath
    }
    return path
}

folder = getFolder()

books = new XmlSlurper().parseText(new File(folder, 'books.xml').text)

import javax.swing.DefaultListModel

listModel = new DefaultListModel();
for (section in books.section) {
    for (book in section.book) {
        listModel.addElement(book.@title)
    }
}

dbd = new DashboardFrame(listModel)
dbd.setFolder(folder)
dbd.setVisible(true)
