package klaxon.klaxon.elmo.core.math;

import static java.lang.System.arraycopy;

import java.util.Arrays;

public class Matrix {
    public final double[] backing;
    public final int rows;
    public final int cols;
    public final int length;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.backing = new double[rows * cols];
        this.length = backing.length;
    }

    public double[] getScratchRow() { return new double[cols]; }

    public int idx(int row, int col) {
        return row * cols + col;
    }

    public int rowIdx(int row) {
        return row * cols;
    }

    public double get(int row, int col) {
        return backing[idx(row, col)];
    }

    public void set(int row, int col, double value) {
        backing[idx(row, col)] = value;
    }

    public void setRow(int row, double[] values) {
        arraycopy(values, 0, backing, rowIdx(row), cols);
    }

    public void swap(int row1, int row2, double[] scratch) {
        arraycopy(backing, rowIdx(row1), scratch, 0, cols);          // row 1 -> scratch
        arraycopy(backing, rowIdx(row2), backing, rowIdx(row1), cols);       // row 2 -> row 1
        arraycopy(scratch, 0, backing, rowIdx(row1), cols);          // scratch -> row 2
    }

    @Override
    public String toString() {
        double[] tmp = getScratchRow();
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
