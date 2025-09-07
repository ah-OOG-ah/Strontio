package klaxon.klaxon.elmo.core.math;

import static java.lang.Math.fma;
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

    public void setRow(int row, float[] values) {
        arraycopy(values, 0, backing, rowIdx(row), cols);
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
            backing[idx(row2, i)] = fma(backing[idx(row1, i)], factor, backing[idx(row2, i)]);
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
            backing[idx(row2, i)] = fma(backing[idx(row1, i)], factor, backing[idx(row2, i)]);
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
}
