package klaxon.klaxon.elmo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Kirchoff {
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
