package klaxon.klaxon.elmo.core;

final class VoltSource extends Hand.TwoPin {
    private final double voltage;

    /**
     * @param low     component attached to the negative side
     * @param high    component attached to the positive side
     * @param voltage voltage difference between sides
     */
    VoltSource(Node low, Node high, double voltage) {
        this(voltage);
        setOne(low); setTwo(high);
    }

    VoltSource(double voltage) {
        this.voltage = voltage;
    }

    @Override
    public String extraInfo() {
        return ", voltage=" + voltage;
    }

    @Override
    public String name() {
        return "VoltSource";
    }

    public Node low() { return one(); }
    public Node high() { return two(); }
    public void setLow(Node n) { setOne(n); }
    public void setHigh(Node n) { setTwo(n); }
}
