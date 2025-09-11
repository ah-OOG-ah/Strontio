package klaxon.klaxon.elmo.core;

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
}
