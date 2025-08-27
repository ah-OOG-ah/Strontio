package klaxon.klaxon.elmo;

import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static klaxon.klaxon.elmo.Component.Type.RESISTOR;
import static klaxon.klaxon.elmo.Utils.boxPanel;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MainFrame extends JFrame {
    public MainFrame(String name) {
        super(name);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var mainPane = boxPanel(Y_AXIS);
        mainPane.add(createComponentFrame());
        add(mainPane);
    }

    private JComponent createComponentFrame() {
        final var componentPane = new JScrollPane(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

        final var componentTable = new Component.ComponentTable();
        componentTable.addRow(new Component(RESISTOR, 1000, 0, 1));

        componentPane.add(componentTable);
        return componentPane;
    }
}
