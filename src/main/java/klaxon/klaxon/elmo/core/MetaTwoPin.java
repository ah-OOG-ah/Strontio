package klaxon.klaxon.elmo.core;

import java.util.List;
import klaxon.klaxon.elmo.core.cas.Term;
import org.jetbrains.annotations.NotNull;

/// Stores additional data about a component - forwards/backwards loop orientation, voltage, current, and more.
public record MetaTwoPin(Hand.TwoPin t, boolean forwards, @NotNull List<Hand.TwoPin> sinks) implements Term {
    MetaTwoPin(Hand.TwoPin t, boolean forwards) {
        this(t, forwards, (forwards ? t.two() : t.one()).components.stream().filter(o -> o != t).toList());
    }

    @Override
    public @NotNull String toString() {
        return "[" + t + ", " + (forwards ? "normal" : "reverse") + "]";
    }

    /// @return {@link Hand.TwoPin#two()} if {@link #forwards)}, otherwise {@link Hand.TwoPin#one()}
    public Node next() {
        return forwards ? t.two() : t.one();
    }

    /// @return the opposite of {@link #next()}
    public Node previous() {
        return !forwards ? t.two() : t.one();
    }

    @Override
    public String toTerm() {
        return (forwards ? "" : "-") + t.addToEquation();
    }
}
