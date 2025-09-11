package klaxon.klaxon.elmo.core;

import static klaxon.klaxon.elmo.core.Formatter.fmtUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import klaxon.klaxon.elmo.core.math.Matrix;
import klaxon.klaxon.elmo.core.math.MatrixUtils;

public class Kirchoff {
    public final Circuit c;
    final Map<Circuit.TwoPin, MetaTwoPin> components;
    final Set<Circuit.Node> nodes;
    final List<TwoPinLoop> loops;
    public final int resistorCount;

    Kirchoff(Circuit c, Map<Circuit.TwoPin, MetaTwoPin> components, Set<Circuit.Node> nodes, List<TwoPinLoop> loops) {
        this.c = c;
        this.components = components;
        this.nodes = nodes;
        this.loops = loops;

        int rc = 0;
        for (final var e : components.keySet()) {
            if (e instanceof Circuit.Resistor) ++rc;
        }
        resistorCount = rc;
    }

    /// The goal here is to print out a list of components with solved currents.
    /// To that end - we first take the loop equations, then fill out the matrix with junction equations.
    public void printCurrents() {
        final int terms = components.size() + 1;
        final var loops = this.loops;
        final var junctions = generateJunctions();

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
        for (int i = 0; i < components.size(); ++i) {
            //noinspection unchecked
            currents.computeIfAbsent(
                    mat.get(i, mat.cols - 1),
                        _ -> new ArrayList<Circuit.TwoPin>())
                    .add(c.components.get(i));
        }

        currents.forEach((current, components) -> {
            var names = new StringBuilder();
            //noinspection unchecked
            ((ArrayList<Circuit.TwoPin>) components).forEach(t -> names.append(t.name()).append(", "));
            names.delete(names.length() - 2, names.length());
            Hand.LOGGER.info("{}: {}", names, fmtUnit(current, "A"));
        });
    }

    public List<float[]> generateJunctions() {
        final var lookup = components;
        final var ret = new ArrayList<float[]>();

        // One term per component, plus the voltage sum
        final int nTerms = lookup.size() + 1;
        for (var node : nodes) {
            // Add each component to the equation
            var equation = new float[nTerms];
            for (var c : node.components) {
                var mtp = lookup.get(c);
                var forwards = mtp.forwards();
                var startsHere = c.one() == node;

                // If the component is draining current, subtract our current
                if ((forwards && startsHere) || (!forwards && !startsHere)) equation[c.idx] = -1;
                else equation[c.idx] = 1; // otherwise, we're dumping current in
            }

            ret.add(equation);
        }

        return ret;
    }
}
