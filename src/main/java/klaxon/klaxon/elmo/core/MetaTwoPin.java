package klaxon.klaxon.elmo.core;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/// Stores additional data about a component - forwards/backwards loop orientation, voltage, current, and more.
public record MetaTwoPin(Circuit.TwoPin t, boolean forwards, @NotNull List<Circuit.TwoPin> sinks) {
    MetaTwoPin(Circuit.TwoPin t, boolean forwards) {
        this(t, forwards, (forwards ? t.two() : t.one()).components.stream().filter(o -> o != t).toList());
    }

    @Override
    public @NotNull String toString() {
        return "[" + t + ", " + (forwards ? "normal" : "reverse") + "]";
    }

    /// @return {@link Circuit.TwoPin#two()} if {@link #forwards)}, otherwise {@link Circuit.TwoPin#one()}
    public Circuit.Node next() {
        return forwards ? t.two() : t.one();
    }

    /// @return the opposite of {@link #next()}
    public Circuit.Node previous() {
        return !forwards ? t.two() : t.one();
    }
}
