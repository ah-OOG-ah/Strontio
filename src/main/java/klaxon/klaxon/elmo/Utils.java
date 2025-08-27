package klaxon.klaxon.elmo;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class Utils {
    public static JPanel boxPanel(int axis) {
        var panel = new JPanel();
        //noinspection MagicConstant # intellij, this is *not a constant*
        panel.setLayout(new BoxLayout(panel, axis));
        return panel;
    }
}
