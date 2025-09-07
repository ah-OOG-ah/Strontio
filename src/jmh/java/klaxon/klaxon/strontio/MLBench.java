package klaxon.klaxon.strontio;

import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.max;
import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import klaxon.klaxon.elmo.core.math.Matrix;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class MLBench {
    private static final boolean NARROW = true;
    private final Matrix MATRIX = NARROW
            ? new Matrix(78, 27)
            : new Matrix(27, 78);
    private final Random RANDOM = new Random(1337);

    @Setup
    public void setup() {
        for (int i = 0; i < MATRIX.length; ++i) {
            MATRIX.backing[i] = RANDOM.nextFloat();
        }
    }

    @Benchmark
    @Measurement(time = 5, iterations = 3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(2)
    @Warmup(iterations = 2)
    public void reduceMatrixVector() {
        final int matW = MATRIX.cols;
        final int matH = MATRIX.rows;
        final var scratch = MATRIX.getScratchRow();

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0.0;
            int newPivotRow = pivotRow;
            for (int i = pivotRow; i < matH; ++i) {
                if (abs(pivot) < abs(MATRIX.get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = MATRIX.get(i, pivotCol);
                }
            }

            if (pivot == 0.0) {
                pivotCol++; continue;
            }

            MATRIX.swap(newPivotRow, pivotRow, scratch);

            // For each row below the pivot...
            final var rf = 1 / MATRIX.get(pivotRow, pivotCol);
            for (int i = pivotRow + 1; i < matH; ++i) {

                // Divide the first element of the row by the first element of the pivot. That way, when we subtract the
                // pivot row times -factor from the row below, you get 0 in the pivot column.
                final var factor = MATRIX.get(i, pivotCol) * rf;
                MATRIX.fmaRowPartial(pivotRow, i, -factor, pivotCol + 1);

                // Set the pivot element to 0, because floats are hard and we *know* it should be zero
                MATRIX.set(i, pivotCol, 0);
            }

            pivotCol++;
            pivotRow++;
        }
    }

    @Benchmark
    @Measurement(time = 5, iterations = 3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(2)
    @Warmup(iterations = 2)
    public void reduceMatrixVUnmask() {
        final int matW = MATRIX.cols;
        final int matH = MATRIX.rows;
        final var scratch = MATRIX.getScratchRow();

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0.0;
            int newPivotRow = pivotRow;
            for (int i = pivotRow; i < matH; ++i) {
                if (abs(pivot) < abs(MATRIX.get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = MATRIX.get(i, pivotCol);
                }
            }

            if (pivot == 0.0) {
                pivotCol++; continue;
            }

            MATRIX.swap(newPivotRow, pivotRow, scratch);

            // For each row below the pivot...
            final var rf = 1 / MATRIX.get(pivotRow, pivotCol);
            for (int i = pivotRow + 1; i < matH; ++i) {

                // Divide the first element of the row by the first element of the pivot. That way, when we subtract the
                // pivot row times -factor from the row below, you get 0 in the pivot column.
                final var factor = MATRIX.get(i, pivotCol) * rf;
                MATRIX.fmaRowUnmask(pivotRow, i, -factor, pivotCol + 1);

                // Set the pivot element to 0, because floats are hard and we *know* it should be zero
                MATRIX.set(i, pivotCol, 0);
            }

            pivotCol++;
            pivotRow++;
        }
    }

    @Benchmark
    @Measurement(time = 5, iterations = 3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(2)
    @Warmup(iterations = 2)
    public void reduceMatrixScalar() {
        final int matW = MATRIX.cols;
        final int matH = MATRIX.rows;
        final var scratch = MATRIX.getScratchRow();

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0.0;
            int newPivotRow = pivotRow;
            for (int i = pivotRow; i < matH; ++i) {
                if (abs(pivot) < abs(MATRIX.get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = MATRIX.get(i, pivotCol);
                }
            }

            if (pivot == 0.0) {
                pivotCol++; continue;
            }

            MATRIX.swap(newPivotRow, pivotRow, scratch);

            // For each row below the pivot...
            final var rf = 1 / MATRIX.get(pivotRow, pivotCol);
            for (int i = pivotRow + 1; i < matH; ++i) {

                // Divide the first element of the row by the first element of the pivot. That way, when we subtract the
                // pivot row times -factor from the row below, you get 0 in the pivot column.
                final var factor = MATRIX.get(i, pivotCol) * rf;
                MATRIX.fmaRowScalar(pivotRow, i, -factor, pivotCol + 1);

                // Set the pivot element to 0, because floats are hard and we *know* it should be zero
                MATRIX.set(i, pivotCol, 0);
            }

            pivotCol++;
            pivotRow++;
        }
    }

    @Benchmark
    @Measurement(time = 5, iterations = 3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(2)
    @Warmup(iterations = 2)
    public void reduceMatrixWP() {
        final int matW = MATRIX.cols;
        final int matH = MATRIX.rows;
        final var scratch = MATRIX.getScratchRow();

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0.0;
            int newPivotRow = pivotRow;
            for (int i = pivotRow; i < matH; ++i) {
                if (abs(pivot) < abs(MATRIX.get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = MATRIX.get(i, pivotCol);
                }
            }

            if (pivot == 0.0) {
                pivotCol++; continue;
            }

            MATRIX.swap(newPivotRow, pivotRow, scratch);

            // For each row below the pivot...
            final var rf = 1 / MATRIX.get(pivotRow, pivotCol);
            for (int i = pivotRow + 1; i < matH; ++i) {
                final var f = MATRIX.get(i, pivotCol) * rf;

                // Set the pivot element to 0, because floats are hard and we *know* it should be zero
                MATRIX.set(i, pivotCol, 0);

                // Subtract the pivot row times f from the lower row
                for (int j = pivotCol + 1; j < matW; ++j) {
                    final var v = fma(MATRIX.get(pivotRow, j), -f, MATRIX.get(i, j));
                    MATRIX.set(i, j, v);
                }
            }

            pivotCol++;
            pivotRow++;
        }
    }
}
