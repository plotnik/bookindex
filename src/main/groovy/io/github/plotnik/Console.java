package io.github.plotnik;

import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.io.*;

/**
   Консоль для вывода.
   Пример использования:

   ```
   console = new Console();
   console.setTitle("Console Test");
   console.setSize(200, 100);
   console.setVisible(true);

   while (!console.isDone()) {
        console.log(new Date().toString());
        Thread.sleep(1000);
   }
   ```

   Клавиша `Esc` закрывает окно консоли.
   Клавиша `F1` приостанавливет прокрутку в окне.
 */
public class Console extends JFrame implements IConsole {

    private JPanel jContentPane = null;

    private JScrollPane jScrollPane = null;

    private JTextPane jTextPane = null;

    private boolean done = false;

    private StyledDocument doc;

    private Style style;

    private boolean locked = false;

    private String title;

    public static final Color COLOR_INFO = new Color(200, 200, 200);
    public static final Color COLOR_ERROR = new Color(255, 120, 120);

    public Console() {
        super();
        initialize();
        setLocationRelativeTo(null);
    }

    private void initialize() {
        this.setSize(800, 600);
        this.setContentPane(getJContentPane());
        this.setTitle("Console");
        this.refreshTitle();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                done = true;
            }
        });
        getJTextPane().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String key = KeyEvent.getKeyText(e.getKeyCode());
                //System.out.println("key="+key);
                if ("F1".equals(key)) {
                    locked = !locked;
                    refreshTitle();
                } else if ("Escape".equals(key)) {
                    dispose();
                    done = true;
                }
            }
        });
    }

    public void refreshTitle() {
        super.setTitle(title + (locked ? " [locked]" : ""));
    }

    public void setTitle(String title) {
        this.title = title;
        refreshTitle();
    }

    public void log(String s) {
        log(s, COLOR_INFO);
    }

    public void log(String s, Color c) {
        append(s + "\n", c);
    }

    public void error(String s) {
        log(s, COLOR_ERROR);
    }

    public void error(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        error(sw.toString());
    }

    public void append(String s, Color c) {
        JTextPane console = getJTextPane();
        StyleConstants.setForeground(style, c);
        try {
            doc.insertString(doc.getLength(), s, style);
        } catch (BadLocationException e) {
            System.err.println("[ERROR]  BadLocationException: " + e.getMessage());
        }

        if (!locked) {
            int len = console.getDocument().getLength();
            console.setCaretPosition(len);
        }
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            jScrollPane.setViewportView(getJTextPane());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextPane
     */
    public JTextPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            jTextPane.setBackground(Color.black);
            jTextPane.setFont(new Font("Monospaced", Font.PLAIN, 16));
            doc = (StyledDocument) jTextPane.getDocument();
            style = doc.addStyle("StyleName", null);
        }
        return jTextPane;
    }

    public boolean isDone() {
        return done;
    }

    public void center() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Insets desktopInsets = tk.getScreenInsets(gc);
        Dimension scr = tk.getScreenSize();
        Dimension size = getSize();
        int x = desktopInsets.left
                + (scr.width - desktopInsets.left - desktopInsets.right - size.width)
                / 2;
        int y = desktopInsets.top
                + (scr.height - desktopInsets.top - desktopInsets.bottom - size.height)
                / 2;
        setLocation(x, y);
    }

}
