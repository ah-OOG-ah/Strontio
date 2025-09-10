package klaxon.klaxon.elmo.core.math;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;

public class MatrixUtils {

    public static void rref(Matrix matrix) {
        final int matW = matrix.cols;
        final int matH = matrix.rows;
        final var scratch = matrix.getScratchRow();
        // The column of the pivot for each row.
        final var pivotCols = new int[min(matH, matW)];
        var pivotCount = -1;

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0.0;
            int newPivotRow = pivotRow;
            for (int i = pivotRow; i < matH; ++i) {
                if (abs(pivot) < abs(matrix.get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = matrix.get(i, pivotCol);
                }
            }

            // No pivot in this column!
            if (pivot == 0.0) {
                pivotCol++; continue;
            }

            // There *was* a pivot, record it!
            pivotCols[++pivotCount] = pivotCol;

            matrix.swap(newPivotRow, pivotRow, scratch);

            // For each row below the pivot...
            final var rf = 1 / matrix.get(pivotRow, pivotCol);
            for (int i = pivotRow + 1; i < matH; ++i) {

                // Divide the first element of the row by the first element of the pivot. That way, when we subtract the
                // pivot row times -factor from the row below, you get 0 in the pivot column.
                final var factor = matrix.get(i, pivotCol) * rf;
                matrix.fmaRowUnmask(pivotRow, i, -factor, pivotCol + 1);

                // Set the pivot element to 0, because floats are hard and we *know* it should be zero
                matrix.set(i, pivotCol, 0);
            }

            pivotCol++;
            pivotRow++;
        }

        pivotRow = pivotCount - 1;
        Arrays.fill(pivotCols, pivotCount, pivotCols.length, -1);

        matrix.supernormalize();

        // Now, back-substitute
        // TODO optimize for pivots == matW case
        while (pivotRow > 0) {
            pivotCol = pivotCols[pivotRow];
            final var pivot = matrix.get(pivotRow, pivotCol);
            final var invPivot = 1 / pivot;

            for (int i = pivotRow - 1; i > -1; --i) {
                final var factor = matrix.get(i, pivotCol) * invPivot;
                matrix.fmaRowUnmask(pivotRow, i, -factor, pivotCol + 1);
                matrix.set(i, pivotCol, 0);
            }

            // Normalize the row
            matrix.mulRowUnmask(pivotRow, invPivot, pivotCol);

            matrix.supernormalize();
            pivotRow--;
        }
    }
}
