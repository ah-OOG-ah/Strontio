package klaxon.klaxon.elmo.core;

import static klaxon.klaxon.elmo.core.Hand.LOGGER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import org.jetbrains.annotations.Contract;

public class Circuit {
    private int nextIdx = 0;
    private final HashMap<Class<? extends TwoPin>, Integer> nextCIdx = new HashMap<>();
    public ArrayList<TwoPin> components = new ArrayList<>();
    private final String name;

    public Circuit(String name) {
        this.name = name;
    }

    public void printCurrents() {
        final var kirchoff = Hand.generateLoops(this);
        LOGGER.info("Printing currents for {}", this);
        kirchoff.printCurrents();
    }

    /// Throws an exception if any component has a null pin
    /// TODO: make this more thorough
    void validate() {
        var ret = true;
        for (var c : components) {
            if (c.one() == null || c.two() == null) {
                LOGGER.error("Component validation failed! Component: {}", c);
                ret = false;
            }
        }

        if (ret) LOGGER.debug("All components valid!");
        else throw new RuntimeException("Validation failure!");
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract sealed class TwoPin permits Resistor, VoltSource {
        private Node one;
        private Node two;
        public final int idx;
        public final int cidx;

        TwoPin(Node one, Node two) {
            if (one != null) setOne(one);
            if (two != null) setTwo(two);

            // Maintain a global ID for components in the circuit...
            idx = nextIdx++;
            components.add(this);

            // ...and a component-specific one
            cidx = nextCIdx.computeIfAbsent(this.getClass(), c -> 0);
            nextCIdx.put(this.getClass(), cidx + 1);
        }

        public Node one() {
            if (one == null) one = new Node(this);

            return one;
        }

        public Node two() {
            if (two == null) two = new Node(this);

            return two;
        }

        public void setOne(Node n) {
            one = n.add(this);
        }

        public void setTwo(Node n) {
            two = n.add(this);
        }

        public String toString() {
            return name() + "[" +
                    "one=" + one + ", " +
                    "two=" + two + extraInfo() + "]";
        }

        public String extraInfo() {
            return "";
        }

        public abstract String name();
    }

    public final class Resistor extends TwoPin {
        public final float resistance;

        /**
         * @param one        component attached to pin 1
         * @param two        component attached to pin 2
         * @param resistance in ohms
         */
        Resistor(Node one, Node two, float resistance) {
            super(one, two);
            this.resistance = resistance;
        }

        Resistor(float resistance) {
            this(null, null, resistance);
        }

        @Override
        public String extraInfo() {
            return ", resistance=" + resistance;
        }

        @Override
        public String name() {
            return "R" + (cidx + 1);
        }

    }

    public final class VoltSource extends TwoPin {
        public final float voltage;

        /**
         * @param low     component attached to the negative side
         * @param high    component attached to the positive side
         * @param voltage voltage difference between sides
         */
        VoltSource(Node low, Node high, float voltage) {
            super(low, high);
            this.voltage = voltage;
        }

        VoltSource(float voltage) {
            this(null, null, voltage);
        }

        @Override
        public String extraInfo() {
            return ", voltage=" + voltage;
        }

        @Override
        public String name() {
            return "V" + (cidx + 1);
        }

        public Node low() { return one(); }
        public Node high() { return two(); }
    }

    public class Node {
        LinkedHashSet<TwoPin> components = new LinkedHashSet<>();

        public Node(TwoPin t) {
            components.add(t);
        }

        @Contract("_ -> this")
        Node add(TwoPin t) { components.add(t); return this; }

        @Override
        public String toString() {
            return "Node[connections=" + components.size() + "]";
        }
    }
}
