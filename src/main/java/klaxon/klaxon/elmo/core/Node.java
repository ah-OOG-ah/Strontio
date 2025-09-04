package klaxon.klaxon.elmo.core;

import java.util.LinkedHashSet;
import org.jetbrains.annotations.Contract;

class Node {
    LinkedHashSet<Circuit.TwoPin> components = new LinkedHashSet<>();

    public Node() {}
    public Node(Circuit.TwoPin t) {
        components.add(t);
    }

    @Contract("_ -> this")
    Node add(Circuit.TwoPin t) { components.add(t); return this; }

    @Override
    public String toString() {
        return "Node[connections=" + components.size() + "]";
    }
}
