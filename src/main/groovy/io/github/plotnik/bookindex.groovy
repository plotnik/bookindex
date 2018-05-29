package io.github.plotnik

import javax.swing.DefaultListModel

println "-- bookindex"
println "   Parameter: property_file"

// http://mvnrepository.com
// https://github.com/a-services/apperyunit/blob/master/src/main/groovy/io/appery/apperyunit/DashboardFrame.java
// https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html


pp = new Properties();

targetPropName = System.getProperty('user.home') + '/.plotnik/bookindex.properties'
propName = targetPropName
if (args.length > 0) {
    propName = args[0]
}

void loadProperties() {
    File f = new File(propName)
    if (f.exists()) {
        FileInputStream fin = new FileInputStream(f.path);
        pp.load(fin);
        fin.close();
    }
}

loadProperties()

String getSetting(String name) {
    String value = pp.getProperty(name)
    if (value == null) {
        throw new BookException("Property required: " + name)
    }
    return value
}

try {
    settings = new Settings()
    settings.folder = getSetting('folder')
    settings.apperyDbId = getSetting('appery_db_id')
    settings.apperyMasterKey = getSetting('appery_master_key')

    books = new XmlSlurper().parseText(new File(settings.folder, 'books.xml').text)


    listModel = new DefaultListModel();
    for (section in books.section) {
        for (book in section.book) {
            listModel.addElement(book.@title)
        }
    }

    dbd = new DashboardFrame(listModel)
    dbd.setSettings(settings)
    dbd.setVisible(true)
} catch(BookException e) {
    println "[ERROR] " + e.reason
}