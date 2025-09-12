package klaxon.klaxon.elmo.core.math;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static jdk.incubator.vector.FloatVector.broadcast;
import static jdk.incubator.vector.FloatVector.fromArray;

import java.util.Arrays;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class Matrix {
    public final float[] backing;
    public final int rows;
    public final int cols;
    public final int length;

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    private static final FloatVector VEPSILON = broadcast(SPECIES, FloatUtils.EPSILON);
    private static final FloatVector VZERO = broadcast(SPECIES, 0);

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.backing = new float[rows * cols];
        this.length = backing.length;
    }

    public float[] getScratchRow() { return new float[cols]; }

    public int idx(int row, int col) {
        return row * cols + col;
    }

    public int rowIdx(int row) {
        return row * cols;
    }

    public float get(int row, int col) {
        return backing[idx(row, col)];
    }

    public void set(int row, int col, float value) {
        backing[idx(row, col)] = value;
    }

    public void add(int row, int col, float value) {
        backing[idx(row, col)] += value;
    }

    /// Multiplies val by factor, then accumulates to matrix(row, col).
    public void fma(int row, int col, float val, float factor) {
        final int idx = idx(row, col);
        backing[idx] = Math.fma(val, factor, backing[idx]);
    }

    public void setRow(int row, float[] values) {
        arraycopy(values, 0, backing, rowIdx(row), cols);
    }

    /**
     * Multiplies row by factor. Skips columns before offset
     */
    public void mulRowUnmask(int row, float factor, int offset) {
        final var vfactor = broadcast(SPECIES, factor);
        int i = 0;
        for (; i < SPECIES.loopBound(cols - offset); i += SPECIES.length()) {
            final var idx = idx(row, i + offset);
            fromArray(SPECIES, backing, idx).mul(vfactor).intoArray(backing, idx);
        }

        for (i += offset; i < cols; ++i) {
            backing[idx(row, i)] *= factor;
        }
    }

    /**
     * Multiplies row1 by factor, then accumulates it to row2. Skips columns before offset
     */
    public void fmaRowUnmask(int row1, int row2, float factor, int offset) {
        final var vfactor = broadcast(SPECIES, factor);
        int i = 0;
        for (; i < SPECIES.loopBound(cols - offset); i += SPECIES.length()) {
            final var vrow1 = fromArray(SPECIES, backing, idx(row1, i + offset));
            final var vrow2 = fromArray(SPECIES, backing, idx(row2, i + offset));
            vrow1.fma(vfactor, vrow2).intoArray(backing, idx(row2, i + offset));
        }

        for (i += offset; i < cols; ++i) {
            fma(row2, i, get(row1, i), factor);
        }
    }

    /**
     * Multiplies row1 by factor, then accumulates it to row2. Skips entries before offset.
     */
    public void fmaRowPartial(int row1, int row2, float factor, int offset) {
        if (offset < 0) throw new IllegalArgumentException("Negative offsets are illegal!");
        final var vfactor = broadcast(SPECIES, factor);
        for (int i = offset; i < cols; i += SPECIES.length()) {
            final var mask = SPECIES.indexInRange(i, cols);
            final var vrow1 = fromArray(SPECIES, backing, idx(row1, i), mask);
            final var vrow2 = fromArray(SPECIES, backing, idx(row2, i), mask);
            vrow1.fma(vfactor, vrow2).intoArray(backing, idx(row2, i), mask);
        }
    }

    /**
     * Multiplies row1 by factor, then accumulates it to row2
     */
    public void fmaRowScalar(int row1, int row2, float factor, int offset) {
        for (int i = offset; i < cols; ++i) {
            fma(row2, i, get(row1, i), factor);
        }
    }

    /**
     * Removes any excessively-small values in the matrix (+- {@link FloatUtils#EPSILON})
     */
    public void supernormalize() {
        int i = 0;
        for (; i < SPECIES.loopBound(length); i += SPECIES.length()) {
            final var vec = fromArray(SPECIES, backing, i);
            final var m = vec.abs().lt(VEPSILON);
            VZERO.intoArray(backing, i, m);
        }

        for (; i < length; ++i) {
            if (abs(backing[i]) < FloatUtils.EPSILON) backing[i] = 0;
        }
    }

    public void swap(int row1, int row2, float[] scratch) {
        arraycopy(backing, rowIdx(row1), scratch, 0, cols);          // row 1 -> scratch
        arraycopy(backing, rowIdx(row2), backing, rowIdx(row1), cols);       // row 2 -> row 1
        arraycopy(scratch, 0, backing, rowIdx(row2), cols);          // scratch -> row 2
    }

    @Override
    public String toString() {
        final var tmp = getScratchRow();
        StringBuilder ret = new StringBuilder("[\n");
        for (int i = 0; i < length; i += cols) {
            arraycopy(backing, i, tmp, 0, tmp.length);
            ret.append(Arrays.toString(tmp)).append(",\n");
        }

        final var len = ret.length();
        ret.delete(len - 2, len);
        ret.append("]");

        return ret.toString();
    }

    public void rref() {
        final int matW = cols;
        final int matH = rows;
        final var scratch = getScratchRow();
        // The column of the pivot for each row.
        final var pivotCols = new int[min(matH, matW)];
        var pivotCount = 0;

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0f;
            int newPivotRow = pivotRow;
            for (int i = pivotRow; i < matH; ++i) {
                if (abs(pivot) < abs(get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = get(i, pivotCol);
                }
            }

            // No pivot in this column!
            if (FloatUtils.equals(pivot, 0)) {
                pivotCol++; continue;
            }

            // There *was* a pivot, record it!
            pivotCols[pivotCount++] = pivotCol;

            swap(newPivotRow, pivotRow, scratch);

            // For each row below the pivot...
            final var rf = 1 / get(pivotRow, pivotCol);
            for (int i = pivotRow + 1; i < matH; ++i) {

                // Divide the first element of the row by the first element of the pivot. That way, when we subtract the
                // pivot row times -factor from the row below, you get 0 in the pivot column.
                final var factor = get(i, pivotCol) * rf;
                fmaRowUnmask(pivotRow, i, -factor, pivotCol + 1);

                // Set the pivot element to 0, because floats are hard and we *know* it should be zero
                set(i, pivotCol, 0);
            }

            pivotCol++;
            pivotRow++;
        }

        pivotRow = pivotCount - 1;
        Arrays.fill(pivotCols, pivotCount, pivotCols.length, -1);

        supernormalize();

        // Now, back-substitute
        // TODO do something with singular matrices
        if (pivotCount < matW - 1) {
            reducePartialPivots(pivotRow, pivotCols);
        } else if (pivotCount == matW - 1) {
            reduceFullPivots(pivotRow);
        } else {
            throw new RuntimeException("Matrix is singular!");
        }

    }

    private void reduceFullPivots(int pivotRow) {
        final int lastColIdx = cols - 1;

        while (pivotRow > 0) {
            final int pivotCol = pivotRow;
            final var pivot = get(pivotRow, pivotCol);
            final var invPivot = 1 / pivot;
            final var lastCol = get(pivotRow, lastColIdx) * invPivot;

            // We only need to math the last column, and the pivot col can just get zeroed
            for (int i = pivotRow - 1; i > -1; --i) {
                add(i, lastColIdx, get(i, pivotCol) * -lastCol);
                set(i, pivotCol, 0);
            }

            // Normalize the row
            set(pivotRow, pivotCol, 1);
            set(pivotRow, cols - 1, lastCol);

            pivotRow--;
        }
    }

    private void reducePartialPivots(int pivotRow, int[] pivotCols) {
        while (pivotRow > 0) {
            final int pivotCol = pivotCols[pivotRow];
            final var pivot = get(pivotRow, pivotCol);
            final var invPivot = 1 / pivot;

            for (int i = pivotRow - 1; i > -1; --i) {
                final var factor = get(i, pivotCol) * invPivot;
                fmaRowUnmask(pivotRow, i, -factor, pivotCol + 1);
                set(i, pivotCol, 0);
            }

            // Normalize the row
            mulRowUnmask(pivotRow, invPivot, pivotCol);

            supernormalize();
            pivotRow--;
        }
    }
}
