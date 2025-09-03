package klaxon.klaxon.elmo.core;

final class VoltSource extends Hand.TwoPin {
    public final double voltage;

    /**
     * @param low     component attached to the negative side
     * @param high    component attached to the positive side
     * @param voltage voltage difference between sides
     */
    VoltSource(Node low, Node high, double voltage) {
        super(low, high);
        this.voltage = voltage;
    }

    VoltSource(double voltage) {
        this(null, null, voltage);
    }

    @Override
    public String extraInfo() {
        return ", voltage=" + voltage;
    }

    @Override
    public String name() {
        return "VoltSource";
    }

    @Override
    public String addToEquation() {
        return voltage + "V";
    }

    public Node low() { return one(); }
    public Node high() { return two(); }
    public void setLow(Node n) { setOne(n); }
    public void setHigh(Node n) { setTwo(n); }
}
