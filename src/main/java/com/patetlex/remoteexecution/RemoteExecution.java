package com.patetlex.remoteexecution;

import com.patetlex.displayphoenix.Application;
import com.patetlex.displayphoenix.enums.WidgetStyle;
import com.patetlex.displayphoenix.system.web.Website;
import com.patetlex.displayphoenix.ui.ColorTheme;
import com.patetlex.displayphoenix.ui.Theme;
import com.patetlex.displayphoenix.util.ImageHelper;
import com.patetlex.remoteexecution.ui.GUI;
import com.patetlex.remoteexecution.vcs.ExecutablePacket;
import com.patetlex.remoteexecution.vcs.VersionControl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RemoteExecution {

    public static final Website WEBSITE = new Website("https://www.patetlex.com/app/");

    public static ColorTheme[] THEMES;

    public static void main(String[] args) {
        List<ColorTheme> colorThemes = new ArrayList<>();
        colorThemes.add(new ColorTheme(new Color(93, 92, 97), new Color(85,122,149).darker(), new Color(177,162,150), new Color(147,142,148).brighter()));
        colorThemes.add(new ColorTheme(Color.GRAY, Color.DARK_GRAY.darker().darker(), Color.GREEN.darker().darker().darker(), Color.WHITE));
        colorThemes.add(new ColorTheme(new Color(29, 45, 80), new Color(30, 95, 116), new Color(252, 218, 183), Color.WHITE));
        colorThemes.add(new ColorTheme(new Color(26, 26, 29), new Color(195, 7, 63), new Color(78, 78, 80), new Color(230, 230, 230)));
        colorThemes.add(new ColorTheme(new Color(52, 61, 63), new Color(27, 32, 33), new Color(218, 223, 225), Color.WHITE));
        THEMES = colorThemes.toArray(new ColorTheme[colorThemes.size()]);
        Theme theme = new Theme(colorThemes.get(0), WidgetStyle.POPPING, new Font(Font.MONOSPACED, Font.PLAIN, 14), 1200, 800);

        VersionControl.PORT = 53972;
        Application.create(RemoteExecution.class, ImageHelper.resize(ImageHelper.getImage("remoteexecution"), 80, 95), theme);

        GUI.open();
    }


}
