package com.pushtechnology.support.logfiledigest.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorProvider {
    private final List<Color> colors = new ArrayList<>(Arrays.asList(
        Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY,
        Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW ));
    private int nextColor =0 ;


    public Color nextColor() {
        return colors.get(nextColor++ % colors.size());
    }

}
