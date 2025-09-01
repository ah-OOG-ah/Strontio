package klaxon.klaxon.elmo.core;

import java.util.LinkedHashSet;
import org.jetbrains.annotations.Contract;

class Node {
    LinkedHashSet<Hand.TwoPin> components = new LinkedHashSet<>();

    public Node() {}
    public Node(Hand.TwoPin t) {
        components.add(t);
    }

    @Contract("_ -> this")
    Node add(Hand.TwoPin t) { components.add(t); return this; }
}
