package jmandle;

import jmandle.gui.JmandleMainGui;

/**
 *
 */
public class Jmandle {

    private static final JmandleMainGui GUI = new JmandleMainGui();

    public static void main(final String... args) {
        GUI.display();
    }
}
