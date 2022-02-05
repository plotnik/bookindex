package io.github.plotnik;

import java.awt.Color;

import javax.swing.JFrame;

interface IConsole {
    void log(String s);
    void log(String s, Color c);
    void error(String s);
    void append(String s);
    JFrame getFrame();
}