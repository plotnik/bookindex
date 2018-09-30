package io.github.plotnik;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class FileChooserFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.getName().equals("books.xml") || f.isDirectory();
    }

    @Override
    public String getDescription() {
        return "books.xml folder descriptor";
    }    
    
}
