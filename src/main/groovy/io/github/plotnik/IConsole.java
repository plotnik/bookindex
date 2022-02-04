package io.github.plotnik;

import java.awt.Color;

interface IConsole {
    void log(String s);
    void log(String s, Color c);
    void error(String s);
}
