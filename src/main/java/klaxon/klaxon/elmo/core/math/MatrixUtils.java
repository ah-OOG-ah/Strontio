package klaxon.klaxon.elmo.core.math;

import static java.lang.Math.abs;

public class MatrixUtils {

    public static void reduceMatrix(Matrix matrix) {
        final int matW = matrix.cols;
        final int matH = matrix.rows;
        final double[] scratch = matrix.getScratchRow();

        int pivotRow = 0;
        int pivotCol = 0;

        // Shamelessly stolen from wikipedia
        while (pivotRow < matW && pivotCol < matH) {
            // Find the highest value in this column, that'll be the pivot
            var pivot = 0.0;
            int newPivotRow = pivotRow;
            for (int i = 0; i < matH; ++i) {
                if (abs(pivot) < abs(matrix.get(i, pivotCol))) {
                    newPivotRow = i;
                    pivot = matrix.get(i, pivotCol);
                }
            }

            if (pivot == 0.0) {
                pivotCol++; continue;
            }

            matrix.swap(newPivotRow, pivotRow, scratch);

            for (int i = pivotRow + 1; i < matH; ++i) {
                var f = matrix.get(i, pivotCol) / matrix.get(pivotRow, pivotCol);
                matrix.set(i, pivotCol, 0);
                for (int j = pivotCol + 1; j < matW; ++j) {
                    var v = matrix.get(i, j) - matrix.get(pivotRow, j) * f;
                    matrix.set(i, j, v);
                }
            }

            pivotCol++;
            pivotRow++;
        }
    }
}
