package klaxon.klaxon.elmo.core;

import org.jetbrains.annotations.NotNull;

/**
 * While each two-pin has an absolute direction via {@link Hand.TwoPin#one()} and {@link Hand.TwoPin#two()},
 * Kirchhoff's laws require the loop direction to be recorded as well. This record simply stores whether we found
 * the component going forwards or backwards, w.r.t. the absolute direction.
 */
record DirectedTP(Hand.TwoPin t, boolean forwards) {
    @Override
    public @NotNull String toString() {
        return "[" + t + ", " + (forwards ? "normal" : "reverse") + "]";
    }

    /**
     * @return {@link Hand.TwoPin#two()} if {@link #forwards()}, otherwise {@link Hand.TwoPin#one()}
     */
    public Node next() {
        return forwards ? t.two() : t.one();
    }

    /**
     * @return the opposite of {@link #next()}
     */
    public Node previous() {
        return !forwards ? t.two() : t.one();
    }
}
