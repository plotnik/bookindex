package io.github.plotnik;

import java.io.File;
import java.util.List;
import javax.swing.SwingWorker;

public class ApperyUpload extends SwingWorker<Void, String> {
    
    DashboardFrame dashboard;
    String booksHome;
    String monthStamp;
    
    public ApperyUpload(DashboardFrame dashboard) {
        this.dashboard = dashboard;
    }
    
    void uploadFolder(String path) throws BookException {
        File f = new File(path);
        if (!f.exists()) {
            throw new BookException("File not found: " + f.getPath());
        }
        
    }

    @Override
    protected Void doInBackground() {
        console("Uploading to Appery");
        return null;
    }
    
    @Override
    protected void process(List<String> msgs) {
        String msg = msgs.get(msgs.size()-1);
        dashboard.console(msg);
    }
    
    public void console(String msg) {
        publish(msg);
    }    
}
