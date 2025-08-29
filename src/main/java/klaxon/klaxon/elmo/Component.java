package klaxon.klaxon.elmo;

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public record Component(Type t, double value, int pin1, int pin2) {
    enum Type {
        RESISTOR("Resistor"),
        VOLTAGE_SRC("Voltage Source"),
        CURRENT_SRC("Amperage Source");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }

    public static class ComponentTable extends JTable {
        private final Model m = new Model();

        public ComponentTable() {
            super();
            setModel(m);
        }

        public void addRow(Component c) {
            m.addRow(c);
            repaint();
        }

        public static class Model extends AbstractTableModel {
            private final ArrayList<Component> components = new ArrayList<>();

            @Override
            public int getRowCount() {
                return components.size();
            }

            @Override
            public int getColumnCount() {
                return 4;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return getValue(components.get(rowIndex), columnIndex);
            }

            @Override
            public String getColumnName(int column) {
                return switch (column) {
                    case 0 -> "Type";
                    case 1 -> "Value";
                    case 2 -> "Pin 1";
                    case 3 -> "Pin 2";
                    default -> throw new IllegalArgumentException("Invalid index!");
                };
            }

            public void addRow(Component c) {
                components.add(c);
            }

            private static Object getValue(Component c, int idx) {
                return switch (idx) {
                    case 0 -> c.t.name;
                    case 1 -> c.value;
                    case 2 -> c.pin1;
                    case 3 -> c.pin2;
                    default -> throw new IllegalArgumentException("Invalid index!");
                };
            }
        }
    }

}
