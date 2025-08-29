package klaxon.klaxon.elmo;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
