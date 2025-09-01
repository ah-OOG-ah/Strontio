package klaxon.klaxon.elmo.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hand {
    static final Logger LOGGER = LoggerFactory.getLogger(Hand.class);

    public static void main(String[] args) {
        // Generate Kirchhoff loops and print
        printKirchoffs(linearCircuit(15));
        printKirchoffs(superCircuit());
    }

    private static TwoPin linearCircuit(double voltage) {
        final var battery = new VoltSource(voltage);
        final var r1 = new Resistor(battery.high(), null, 468, 1);
        final var r2 = new Resistor(r1.two(), battery.low(), 621, 2);
        final var r3 = new Resistor(r1.two(), null, 2_210, 3);
        final var r4 = new Resistor(r3.two(), battery.low(), 749, 4);
        final var r5 = new Resistor(r3.two(), battery.low(), 998, 5);
        validate(battery, r1, r2, r3, r4, r5);

        return battery;
    }

    private static TwoPin superCircuit() {
        final var b1 = new VoltSource(10);
        final var r1 = new Resistor(b1.high(), null, 470, 1);
        final var r2 = new Resistor(r1.two(), b1.low(), 620, 2);
        final var r3 = new Resistor(r1.two(), null, 2_200, 3);
        final var rL = new Resistor(r3.two(), b1.low(), 750, 4);
        final var r5 = new Resistor(b1.low(), null, 1_000, 5);
        final var b2 = new VoltSource(r5.two(), r3.two(), 5);
        validate(b1, r1, r2, r3, rL, r5, b2);

        return b1;
    }

    private static void printKirchoffs(TwoPin v) {
        final var loops = generateLoops(v);
        LOGGER.info("Printing Kirchhoff equations...");
        for (var l : loops) {
            LOGGER.info("{}", l.toEquation());
        }
    }

    static List<TwoPinLoop> generateLoops(TwoPin v) {
        // Starting from the battery, we breadth-first search.
        // Get everything attached to the battery, and start loops from them.
        var heads = new ArrayDeque<TwoPinLoop>();
        heads.add(new TwoPinLoop(new MetaTwoPin(v, true)));
        var loops = new ArrayList<TwoPinLoop>();

        // BFS! For each component, continue the loop. If there are multiple options, copy the loop and keep going. Kill
        // loops that repeat elements.
        while (!heads.isEmpty()) {
            var head = heads.remove();
            var node = head.getLast().next();

            if (node == (v.one)) {
                // This loop is done, send it!
                loops.add(head);
                continue;
            } else if (head.contains(node)) {
                // We looped around to a previously-visited node - this isn't a valid circuit
                continue;
            }

            // Add each new element to the BFS
            // TODO: less allocation spam
            for (var c : node.components) {
                // This is a trivial -V + V = 0 loop, discard it
                if (c == v) continue;

                heads.add(new TwoPinLoop(head, new MetaTwoPin(c, c.one == node)));
            }
        }

        return loops;
    }

    /// Throws an exception if any component has a null pin
    /// TODO: make this more thorough
    static void validate(TwoPin... components) {
        var ret = true;
        for (var c : components) {
            if (c.one == null || c.two == null) {
                LOGGER.error("Component validation failed! Component: {}", c);
                ret = false;
            }
        }

        if (ret) LOGGER.info("All components valid!");
        else throw new RuntimeException("Validation failure!");
    }

    static abstract class TwoPin {
        private Node one;
        private Node two;

        TwoPin(Node one, Node two) {
            if (one != null) setOne(one);
            if (two != null) setTwo(two);
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
        public abstract void addToEquation(StringBuilder sb, boolean positive);
    }

}
