package klaxon.klaxon.jmatplot.figure;

import java.util.ArrayDeque;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class Manager {
    public final ArrayDeque<Figure> figs = new ArrayDeque<>();

    @NotNull
    public Figure peek() {
        var ret = figs.peekFirst();
        if (ret == null) {
            ret = new Figure();
            figs.push(ret);
        }

        return ret;
    }
}
