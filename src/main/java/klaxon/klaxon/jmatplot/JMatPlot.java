package klaxon.klaxon.jmatplot;

import klaxon.klaxon.jmatplot.figure.Figure;
import klaxon.klaxon.jmatplot.figure.Manager;
import org.jetbrains.annotations.NotNull;

public class JMatPlot {
    public static final ScopedValue<Manager> figureManager = ScopedValue.newInstance();

    /// Get the current figure. If none is present, creates one.
    @NotNull
    public static Figure gcf() {
        return figureManager.get().peek();
    }
}
