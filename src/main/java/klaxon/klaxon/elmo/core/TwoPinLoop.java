package klaxon.klaxon.elmo.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class TwoPinLoop {
    private final LinkedHashSet<MetaTwoPin> loopElements = new LinkedHashSet<>();
    private final HashSet<Node> seen = new HashSet<>();

    public TwoPinLoop() {}
    TwoPinLoop(MetaTwoPin... t) { addAll(List.of(t)); }
    TwoPinLoop(TwoPinLoop l, MetaTwoPin t) {
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
            e.t.addToEquation(equation, e.forwards);
            equation.append(" +");
        }

        var l = equation.length();
        equation.delete(l - 2, l); // trim the trailing " +"
        return equation.toString();
    }

    void add(MetaTwoPin t) {
        loopElements.add(t);
        seen.add(t.previous());
    }

    void addAll(Collection<MetaTwoPin> t) {
        loopElements.addAll(t);
        seen.addAll(t.stream().map(MetaTwoPin::previous).toList());
    }

    boolean contains(Node n) {
        return seen.contains(n);
    }

    MetaTwoPin getLast() {
        return loopElements.getLast();
    }
}
