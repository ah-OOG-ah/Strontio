package klaxon.klaxon.elmo.core;

final class Resistor extends Hand.TwoPin {
    public final double resistance;
    private final int idx;

    Resistor(Node one, Node two, double resistance) {
        this(one, two, resistance, -1);
    }

    /**
     * @param one        component attached to pin 1
     * @param two        component attached to pin 2
     * @param resistance in ohms
     * @param idx        resister number. ignored if < 0. if > 0, changes resistor's name to Ridx
     */
    Resistor(Node one, Node two, double resistance, int idx) {
        super(one, two);
        this.resistance = resistance;
        this.idx = idx;
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
        return idx < 0 ? "Resistor" : "R" + idx;
    }

    @Override
    public void addToEquation(StringBuilder sb, boolean positive) {
        sb.append(" ")
                .append(positive ? "" : "-")
                .append("I")
                .append(name().toLowerCase())
                .append("*")
                .append(resistance);
}
