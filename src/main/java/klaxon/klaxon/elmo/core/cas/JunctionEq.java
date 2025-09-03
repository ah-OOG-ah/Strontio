package klaxon.klaxon.elmo.core.cas;

import java.util.List;
import klaxon.klaxon.elmo.core.MetaTwoPin;
import klaxon.klaxon.elmo.core.Resistor;
import org.jetbrains.annotations.NotNull;

public record JunctionEq(Resistor answer, List<MetaTwoPin> terms) {
    @Override
    public @NotNull String toString() {
        StringBuilder ret = new StringBuilder("I" + answer.name() + " =");
        for (var t : terms) {
            ret.append((t.forwards() ? " + " : " + -")).append(t.t().name());
        }
        return ret.toString();
    }
}
