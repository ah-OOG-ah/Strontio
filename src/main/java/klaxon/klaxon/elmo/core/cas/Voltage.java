package klaxon.klaxon.elmo.core.cas;

public sealed interface Voltage permits Voltage.Known, Voltage.Unknown {
    record Known(double voltage) implements Voltage {}
    record Unknown() implements Voltage {}
}
