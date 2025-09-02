package klaxon.klaxon.elmo.core;

import klaxon.klaxon.elmo.core.cas.Amperage;
import klaxon.klaxon.elmo.core.cas.Voltage;
import org.jetbrains.annotations.NotNull;

/// Stores additional data about a component - forwards/backwards loop orientation, voltage, current, and more.
class MetaTwoPin {
    final Hand.TwoPin t;
    final boolean forwards;
    Voltage v;
    Amperage a;

    MetaTwoPin(Hand.TwoPin t, boolean forwards) {
        this.t = t;
        this.forwards = forwards;

        if (t instanceof VoltSource vin) v = new Voltage.Known(vin.voltage);
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
}
