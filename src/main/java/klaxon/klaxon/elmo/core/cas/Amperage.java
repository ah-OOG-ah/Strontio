package klaxon.klaxon.elmo.core.cas;

import java.util.List;
import klaxon.klaxon.elmo.core.Hand;

public sealed interface Amperage permits Amperage.Sum {
    double current();

    final class Sum implements Amperage {
        private final Hand.TwoPin owner;
        private final List<Amperage> sources;

        public Sum(Hand.TwoPin owner, List<Amperage> sources) {
            this.owner = owner;
            this.sources = sources;
        }

        @Override
        public double current() {
            return sources.stream().map(Amperage::current).reduce(Double::sum).orElse(0.0);
        }
    }
}
