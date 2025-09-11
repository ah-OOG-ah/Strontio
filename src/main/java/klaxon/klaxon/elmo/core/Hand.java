package klaxon.klaxon.elmo.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import klaxon.klaxon.elmo.core.math.Matrix;
import klaxon.klaxon.elmo.core.math.MatrixUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hand {
    static final Logger LOGGER = LoggerFactory.getLogger(Hand.class);

    static void main(String[] ignoredArgs) {
        // Generate Kirchhoff loops and print
        printKirchoffs(linearCircuit(5));
        printKirchoffs(linearCircuit(10));
        printKirchoffs(linearCircuit(15));
        printKirchoffs(superCircuit());
    }

    private static Circuit linearCircuit(float voltage) {
        final var ret = new Circuit("Linear, " + voltage + "V");
        final var battery = ret.new VoltSource(voltage);
        final var r1 = ret.new Resistor(battery.high(), null, 468);
        final var r2 = ret.new Resistor(r1.two(), battery.low(), 621);
        final var r3 = ret.new Resistor(r1.two(), null, 2_210);
        final var r4 = ret.new Resistor(r3.two(), battery.low(), 749);
        final var r5 = ret.new Resistor(r3.two(), battery.low(), 998);
        ret.validate();

        return ret;
    }

    private static Circuit superCircuit() {
        final var ret = new Circuit("Superpositioned");
        final var b1 = ret.new VoltSource(10);
        final var r1 = ret.new Resistor(b1.high(), null, 470);
        final var r2 = ret.new Resistor(r1.two(), b1.low(), 620);
        final var r3 = ret.new Resistor(r1.two(), null, 2_200);
        final var rL = ret.new Resistor(r3.two(), b1.low(), 750);
        final var r5 = ret.new Resistor(b1.low(), null, 1_000);
        final var b2 = ret.new VoltSource(r5.two(), r3.two(), 5);
        ret.validate();

        return ret;
    }

    private static void printKirchoffs(Circuit v) {
        final var kirchoff = generateLoops(v);
        LOGGER.info("Printing currents for {}", v);
        printCurrents(kirchoff);
    }

    /// The goal here is to print out a list of components with solved currents.
    /// To that end - we first take the loop equations, then fill out the matrix with junction equations.
    private static void printCurrents(Kirchoff k) {
        final int terms = k.components.size() + 1;
        final var loops = k.loops;
        final var junctions = k.generateJunctions();

        final var mat = new Matrix(terms, loops.size() + junctions.size());
        var ridx = 0;

        for (final var loop : loops) {
            final var elements = loop.getElements();

            // Set up one row of the matrix
            final var nums = new float[terms];

            // Find the known voltage around the loop and fill in resistor info
            var voltage = 0f;
            for (var e : elements) {
                if (e.t() instanceof Circuit.VoltSource v) {
                    voltage += e.forwards() ? v.voltage : -v.voltage;
                } else if (e.t() instanceof Circuit.Resistor r) {
                    nums[r.idx] = e.forwards() ? r.resistance : -r.resistance;
                }
            }
            nums[terms - 1] = voltage;

            mat.setRow(ridx++, nums);
        }

        for (var j : junctions) { mat.setRow(ridx++, j); }

        MatrixUtils.rref(mat);

        final var currents = new FloatArrayMap<>(ArrayList.class);
        for (int i = 0; i < k.components.size(); ++i) {
            //noinspection unchecked
            currents.computeIfAbsent(
                    mat.get(i, mat.cols - 1),
                        _ -> new ArrayList<Circuit.TwoPin>())
                    .add(k.c.components.get(i));
        }

        currents.forEach((current, components) -> {
            var names = new StringBuilder();
            //noinspection unchecked
            ((ArrayList<Circuit.TwoPin>) components).forEach(t -> names.append(t.name()).append(", "));
            names.delete(names.length() - 2, names.length());
            LOGGER.info("{}: {}", names, current);
        });
    }

    static Kirchoff generateLoops(Circuit circuit) {
        // Starting from the battery, we breadth-first search.
        // Get everything attached to the battery, and start loops from them.
        var heads = new ArrayDeque<TwoPinLoop>();
        var loops = new ArrayList<TwoPinLoop>();
        var components = new HashMap<Circuit.TwoPin, MetaTwoPin>();
        var nodes = new HashSet<Circuit.Node>();
        final var first = circuit.components.getFirst();

        final var entryPoint = new MetaTwoPin(first, true);
        heads.add(new TwoPinLoop(entryPoint));
        components.put(first, entryPoint);

        // BFS! For each component, continue the loop. If there are multiple options, copy the loop and keep going. Kill
        // loops that repeat elements.
        while (!heads.isEmpty()) {
            var loop = heads.remove();
            var head = loop.getLast();
            var node = head.next();
            nodes.add(node);

            if (node == (first.one())) {
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

                boolean forwards = c.one() == node;
                final var mtp = new MetaTwoPin(c, forwards);
                components.put(c, mtp);
                heads.add(new TwoPinLoop(loop, mtp));
            }
        }

        return new Kirchoff(circuit, components, nodes, loops);
    }

}
