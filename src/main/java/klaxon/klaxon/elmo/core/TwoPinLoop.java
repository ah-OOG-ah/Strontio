package klaxon.klaxon.elmo.core;

import java.util.LinkedHashSet;

public class TwoPinLoop {
    final LinkedHashSet<Hand.TwoPin> loopElements = new LinkedHashSet<>();

    public TwoPinLoop() {}
    TwoPinLoop(Hand.TwoPin t) { loopElements.add(t); }
    TwoPinLoop(TwoPinLoop l, Hand.TwoPin t) {
        loopElements.addAll(l.loopElements);
        loopElements.add(t);
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
}
