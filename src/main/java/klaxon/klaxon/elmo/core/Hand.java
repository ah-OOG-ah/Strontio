package klaxon.klaxon.elmo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hand {
    static final Logger LOGGER = LoggerFactory.getLogger(Hand.class);

    static void main(String[] ignoredArgs) {
        // Generate Kirchhoff loops and print
        linearCircuit(5).printCurrents();
        linearCircuit(10).printCurrents();
        linearCircuit(15).printCurrents();
        superCircuit().p();
        superCircuit().printCurrents();

        scShort5().printCurrents();
        scShort10().printCurrents();
        //expandedShort().printCurrents();
    }

    private static Circuit linearCircuit(float voltage) {
        final var ret = new Circuit("Linear, " + voltage + "V");
        final var battery = ret.new VoltSource(voltage);
        final var r1 = ret.new Resistor(battery.high(), null, 468);
        final var r2 = ret.new Resistor(r1.two(), battery.low(), 621);
        final var r3 = ret.new Resistor(r1.two(), null, 2_210);
        final var r4 = ret.new Resistor(r3.two(), battery.low(), 749);
        final var r5 = ret.new Resistor(r3.two(), battery.low(), 998);
        ret.validate();

        return ret;
    }

    private static Circuit superCircuit() {
        final var ret = new Circuit("Superpositioned");
        final var b1 = ret.new VoltSource(10);
        final var r1 = ret.new Resistor(b1.high(), null, 470);
        final var r2 = ret.new Resistor(r1.two(), b1.low(), 620);
        final var r3 = ret.new Resistor(r1.two(), null, 2_200);
        final var rL = ret.new Resistor(r3.two(), b1.low(), 750);
        final var r5 = ret.new Resistor(b1.low(), null, 1_000);
        final var b2 = ret.new VoltSource(r5.two(), r3.two(), 5);
        ret.validate();

        return ret;
    }

    private static Circuit scShort5() {
        final var ret = new Circuit("SCShort5");
        final var b1 = ret.new VoltSource(10);
        final var r1 = ret.new Resistor(b1.high(), null, 470);
        final var r2 = ret.new Resistor(r1.two(), b1.low(), 2_000);
        final var r3 = ret.new Resistor(r1.two(), null, 2_200);
        final var rL = ret.new Resistor(r3.two(), b1.low(), 750);
        final var r5 = ret.new Resistor(r3.two(), b1.low(), 1_000);

        ret.validate();

        return ret;
    }

    private static Circuit scShort10() {
        final var ret = new Circuit("SCShort10");

        final var r1 = ret.new Resistor(null, null, 470);
        final var r2 = ret.new Resistor(r1.two(), r1.one(), 2_000);
        final var r3 = ret.new Resistor(r1.two(), null, 2_200);
        final var rL = ret.new Resistor(r3.two(), r1.one(), 750);

        final var b2 = ret.new VoltSource(null, r3.two(), 5);

        final var r5 = ret.new Resistor(b2.low(), r1.one(), 1_000);
        ret.validate();

        return ret;
    }

    private static Circuit expandedShort() {
        final var ret = new Circuit("Expanded Short");
        final var b1 = ret.new VoltSource(10);
        final var r1 = ret.new Resistor(b1.high(), null, 470);
        final var r2 = ret.new Resistor(r1.two(), null, 4_700);
        final var r3 = ret.new Resistor(r1.two(), r2.two(), 2_200);
        final var r4 = ret.new Resistor(r2.two(), b1.low(), 1_000);
        ret.validate();

        return ret;
    }

}
