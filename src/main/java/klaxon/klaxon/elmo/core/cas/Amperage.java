package klaxon.klaxon.elmo.core.cas;

import java.util.List;

public sealed interface Amperage permits Amperage.Known, Amperage.Sum, Amperage.Driven {
    double current();

    record Known(double current) implements Amperage {}
    record Sum(List<Amperage> sources) implements Amperage {
        @Override
        public double current() {
            return sources.stream().map(Amperage::current).reduce(Double::sum).orElse(0.0);
        }
    }
    record Driven(double voltage, double resistance) implements Amperage {
        @Override
        public double current() {
            return voltage / resistance;
        }
    }
}
