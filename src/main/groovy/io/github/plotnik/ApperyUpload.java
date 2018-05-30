package io.github.plotnik;

import java.util.List;
import javax.swing.SwingWorker;

public class ApperyUpload extends SwingWorker<Void, String> implements IWorker {
    
    DashboardFrame dashboard;
    Settings settings;
    String monthStamp;

    public ApperyUpload(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        settings = dashboard.getSettings();
        monthStamp = settings.getMonthStamp();
    }
    
    @Override
    protected Void doInBackground() {
        List<String> titles = dashboard.titleList.getSelectedValuesList();
        ApperyClient appery = new ApperyClient(this);
                
        console("...Uploading to Appery");
        try {
            for (String title: titles) {
                console(title);
                appery.updateBook(monthStamp, title);
            }
        } catch(Exception e) {
            console("[ERROR] " + e);
        }
        return null;
    }
    
    @Override
    protected void process(List<String> msgs) {
        for (String msg: msgs) {
            dashboard.console(msg);
        }
    }
    
    public void console(String msg) {
        publish(msg);
    }    

    public Settings getSettings() {
        return settings;
    }
    
}
