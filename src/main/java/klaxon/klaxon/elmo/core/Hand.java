package klaxon.klaxon.elmo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hand {
    static final Logger LOGGER = LoggerFactory.getLogger(Hand.class);

    public static void main(String[] args) {
        final var battery = new VoltSource(5);
        final var r1 = new Resistor(battery.high(), null, 47_000);
        final var r2 = new Resistor(r1.two(), battery.low(), 620);
        final var r3 = new Resistor(r1.two(), null, 2_200);
        final var r4 = new Resistor(r3.two(), battery.low(), 750);
        final var r5 = new Resistor(r3.two(), battery.low(), 1_000);

        if (!validate(battery, r1, r2, r3, r4, r5)) return;
    }

    static boolean validate(TwoPin... components) {
        var ret = true;
        for (var c : components) {
            if (c.one == null || c.two == null) {
                LOGGER.error("Component validation failed! Component: {}", c);
                ret = false;
            }
        }

        if (ret) LOGGER.info("All components valid!");

        return ret;
    }

    static abstract class TwoPin {
        private Node one;
        private Node two;

        public Node one() {
            if (one == null) one = new Node();

            return one;
        }

        public Node two() {
            if (two == null) two = new Node();

            return two;
        }

        public void setOne(Node n) {
            one = n;
        }

        public void setTwo(Node n) {
            two = n;
        }

        public String toString() {
            return name() + "[" +
                    "one=" + one() + ", " +
                    "two=" + two() + extraInfo() + "]";
        }

        public String extraInfo() {
            return "";
        }

        public abstract String name();
    }

}
