package klaxon.klaxon.elmo.core.math;

import static java.lang.Math.abs;

public class MatrixUtils {

    public static void reduceMatrix(Matrix matrix) {
        final int matW = matrix.cols;
        final int matH = matrix.rows;
        final var scratch = matrix.getScratchRow();

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

            if (pivot == 0.0) {
                pivotCol++; continue;
            }

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
    }
}
