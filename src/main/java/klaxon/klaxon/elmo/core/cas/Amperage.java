package klaxon.klaxon.elmo.core.cas;

public sealed interface Amperage permits Amperage.Known, Amperage.Unknown {
    record Known(double current) implements Amperage {}
    record Unknown() implements Amperage {}
}
