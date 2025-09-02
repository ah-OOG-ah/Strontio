package klaxon.klaxon.elmo.core.cas;

import java.util.List;

public sealed interface Voltage permits Voltage.Known, Voltage.Sum {
    record Known(double voltage) implements Voltage {}
    record Sum(List<Voltage> sources) implements Voltage {}
}
