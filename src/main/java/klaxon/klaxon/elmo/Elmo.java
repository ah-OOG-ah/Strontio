package klaxon.klaxon.elmo;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static klaxon.klaxon.elmo.Component.Type.RESISTOR;
import static klaxon.klaxon.elmo.Utils.boxPanel;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class Elmo {
    public static void main(String[] args) {

        FlatDarkLaf.setGlobalExtraDefaults(Map.of("@background", "#282a2b"));
        FlatDarkLaf.setup();

        Color base = UIManager.getColor("Table.background");
        int boost = 10;
        Color subtleAlt = new Color(
                clamp(base.getRed() - boost),
                clamp(base.getGreen() - boost),
                clamp(base.getBlue() - boost)
        );
        UIManager.put("Table.alternateRowColor", subtleAlt);

        SwingUtilities.invokeLater(Elmo::startSwing);
    }

    public static void startSwing() {
        final var mainframe = new MainFrame("Elmo");

        mainframe.pack();
        mainframe.setVisible(true);
    }

    static int clamp(int i) {
        return max(min(i, 255), 0);
    }
}
