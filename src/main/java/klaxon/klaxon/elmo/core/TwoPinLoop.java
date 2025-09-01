package klaxon.klaxon.elmo.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class TwoPinLoop {
    private final LinkedHashSet<DirectedTP> loopElements = new LinkedHashSet<>();
    private final HashSet<Hand.TwoPin> seen = new HashSet<>();

    public TwoPinLoop() {}
    TwoPinLoop(DirectedTP... t) { addAll(List.of(t)); }
    TwoPinLoop(TwoPinLoop l, DirectedTP t) {
        addAll(l.loopElements);
        add(t);
    }

    @Override
    public String toString() {
        return "TwoPinLoop{loopElements=" + printElements() + "}";
    }

    private String printElements() {
        StringBuilder ret = new StringBuilder();
        for (var e : loopElements) {
            ret.append("\n").append(e);
        }
        return ret.toString();
    }

    public String toEquation() {
        StringBuilder equation = new StringBuilder("0 =");
        for (var e : loopElements) {
            if (e.t() instanceof VoltSource v) {
                equation.append(" ").append(e.forwards() ? v.voltage * -1 : v.voltage).append("V");
            } else if (e.t() instanceof Resistor r) {
                equation.append(" ").append(e.forwards() ? "" : "-").append("I").append(r.name()).append("*").append(r.resistance);
            } else {
                throw new IllegalArgumentException("Unexpected element " + e);
            }

            equation.append(" +");
        }

        var l = equation.length();
        equation.delete(l - 2, l); // trim the trailing " +"
        return equation.toString();
    }

    void add(DirectedTP t) {
        loopElements.add(t);
        seen.add(t.t());
    }

    void addAll(Collection<DirectedTP> t) {
        loopElements.addAll(t);
        seen.addAll(t.stream().map(DirectedTP::t).toList());
    }

    boolean contains(Hand.TwoPin t) {
        return seen.contains(t);
    }

    DirectedTP getLast() {
        return loopElements.getLast();
    }
}
