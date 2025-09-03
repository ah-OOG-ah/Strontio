package klaxon.klaxon.elmo.core.cas;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public record Relation(Object answer, List<? extends Term> terms) {
    @Override
    public @NotNull String toString() {
        StringBuilder ret = new StringBuilder(answer.toString() + " =");
        for (var t : terms) {
            ret.append(t.toTerm());
        }
        return ret.toString();
    }
}
