package klaxon.klaxon.elmo.core;

final class Resistor extends Hand.TwoPin {
    private final double resistance;

    /**
     * @param one        component attached to pin 1
     * @param two        component attached to pin 2
     * @param resistance in ohms
     */
    Resistor(Node one, Node two, double resistance) {
        super(one, two);
        this.resistance = resistance;
    }

    Resistor(double resistance) {
        this(null, null, resistance);
    }

    @Override
    public String extraInfo() {
        return ", resistance=" + resistance;
    }

    @Override
    public String name() {
        return "Resistor";
    }
}
