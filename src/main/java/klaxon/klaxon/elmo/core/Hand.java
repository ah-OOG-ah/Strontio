package klaxon.klaxon.elmo.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hand {
    static final Logger LOGGER = LoggerFactory.getLogger(Hand.class);

    public static void main(String[] args) {
        final var battery = new VoltSource(5);
        final var r1 = new Resistor(battery.high(), null, 47_000);
        final var r2 = new Resistor(r1.two(), battery.low(), 620);
        final var r3 = new Resistor(r1.two(), null, 2_200);
        final var r4 = new Resistor(r3.two(), battery.low(), 750);
        final var r5 = new Resistor(r3.two(), battery.low(), 1_000);

        if (!validate(battery, r1, r2, r3, r4, r5)) return;

        // Now generate Kirchhoff loops
        for (var l : generateLoops(battery)) {
            LOGGER.info("{}", l);
        }
    }

    static List<TwoPinLoop> generateLoops(VoltSource v) {
        // Starting from the battery, we breadth-first search.
        // Get everything attached to the battery, and start loops from them.
        var heads = v.high().components.stream()
                .filter(p -> p != v)
                .map(TwoPinLoop::new)
                .collect(Collectors.toCollection(ArrayDeque::new));
        var loops = new ArrayList<TwoPinLoop>();

        // BFS! For each component, continue the loop. If there are multiple options, copy the loop and keep going. Kill
        // loops that repeat elements.
        while (!heads.isEmpty()) {
            var head = heads.remove();
            var node = head.loopElements.getLast().two;

            // Add each new element to the BFS
            // TODO: less allocation spam
            for (var c : node.components) {
                if (head.loopElements.contains(c)) continue;
                if (c == v) {
                    // This loop is done, drop it
                    head.loopElements.add(v);
                    loops.add(head);
                    continue;
                }

                heads.add(new TwoPinLoop(head, c));
            }
        }

        return loops;
    }

    /// TODO: make this more thorough
    static boolean validate(TwoPin... components) {
        var ret = true;
        for (var c : components) {
            if (c.one == null || c.two == null) {
                LOGGER.error("Component validation failed! Component: {}", c);
                ret = false;
            }
        }

        if (ret) LOGGER.info("All components valid!");

        return ret;
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
    }

}
