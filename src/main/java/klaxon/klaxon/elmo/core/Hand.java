package klaxon.klaxon.elmo.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import klaxon.klaxon.elmo.core.cas.JunctionEq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hand {
    static final Logger LOGGER = LoggerFactory.getLogger(Hand.class);

    static void main(String[] ignoredArgs) {
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
        final var kirchoff = generateLoops(v);
        LOGGER.info("Printing Kirchhoff equations...");
        for (var l : kirchoff.loops) {
            LOGGER.info("{}", l.toEquation());
        }
        for (var e : generateJunctions(kirchoff)) {
            LOGGER.info("{}", e);
        }
    }

    static Kirchoff generateLoops(TwoPin v) {
        // Starting from the battery, we breadth-first search.
        // Get everything attached to the battery, and start loops from them.
        var heads = new ArrayDeque<TwoPinLoop>();
        var loops = new ArrayList<TwoPinLoop>();
        var components = new HashMap<TwoPin, MetaTwoPin>();


        final var entryPoint = new MetaTwoPin(v, true);
        heads.add(new TwoPinLoop(entryPoint));
        components.put(v, entryPoint);

        // BFS! For each component, continue the loop. If there are multiple options, copy the loop and keep going. Kill
        // loops that repeat elements.
        while (!heads.isEmpty()) {
            var loop = heads.remove();
            var head = loop.getLast();
            var node = head.next();

            if (node == (v.one)) {
                // This loop is done, send it!
                loops.add(loop);
                continue;
            } else if (loop.contains(node)) {
                // We looped around to a previously-visited node - this isn't a valid circuit
                continue;
            }

            // Add each new element to the BFS, and set up for junction calculation
            // TODO: less allocation spam
            for (var c : node.components) {
                // No step back!
                if (c == head.t()) continue;

                boolean forwards = c.one == node;
                final var mtp = new MetaTwoPin(c, forwards);
                components.put(c, mtp);
                heads.add(new TwoPinLoop(loop, mtp));
            }
        }

        return new Kirchoff(components, loops);
    }

    static List<JunctionEq> generateJunctions(Kirchoff kirchoff) {
        final var ret = new ArrayList<JunctionEq>();
        final var lookup = kirchoff.components;
        for (var e : lookup.entrySet()) {
            final var k = e.getKey();
            if (!(k instanceof Resistor r)) continue;

            final var mtp = e.getValue();
            final var downstreams = new HashSet<>(mtp.sinks());
            var nonResistor = downstreams.stream().filter(c -> !(c instanceof Resistor)).findAny();
            while (nonResistor.isPresent()) {
                var nr = nonResistor.get();
                downstreams.remove(nr);
                downstreams.addAll(lookup.get(nr).sinks());
                nonResistor = downstreams.stream().filter(c -> !(c instanceof Resistor)).findAny();
            }

            ret.add(new JunctionEq(r, downstreams.stream().map(lookup::get).toList()));
        }

        return ret;
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

    public static abstract class TwoPin {
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
        public abstract String addToEquation();
    }

    record Kirchoff(Map<TwoPin, MetaTwoPin> components, List<TwoPinLoop> loops) {}
}
