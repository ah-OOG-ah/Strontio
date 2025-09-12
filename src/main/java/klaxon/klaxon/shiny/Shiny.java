package klaxon.klaxon.shiny;

import java.util.Arrays;
import java.util.Scanner;

public class Shiny {
    static void main(String[] ignoredArgs) {
        final var scanner = new Scanner(System.in);
        System.out.print("Enter sizes: ");
        final int[] sizes = Arrays.stream(scanner.nextLine().split(",")).mapToInt(Integer::parseInt).toArray();
        Arrays.sort(sizes);

        final int bigSize = sizes[sizes.length - 1];
        final int maxL = (bigSize + 1) * 2;
        final char[] output = new char[maxL + 1];

        for (int y = 0; y <= maxL; y += 2) {
            for (int x = 0; x <= maxL; ++x) {
                char set = ' ';
                for (var size : sizes) {
                    set = getChar(x, y, size, bigSize - size);
                    if (set != ' ') break;
                }
                output[x] = set;
            }

            System.out.println("|" + new String(output) + "|");
        }
    }

    public static char getChar(int x, int y, int size, int offset) {
        x -= offset;
        y -= offset;
        if (x < 0 || y < 0) return ' ';
        final int max = (size + 1) * 2;
        if (x > max || y > max) return ' ';

        // Return * for corners
        final boolean centerCorners = y == size + 1 && (x == 0 || x == max);
        final boolean sideCorners = x == size + 1 && (y == 0 || y == max);
        if (sideCorners || centerCorners) return '*';

        if (y <= size) {
            if (x == size + 1 + y) return '\\';
            else if (x == size + 1 - y) return '/';
            else return ' ';
        } else {
            if (x == y - size - 1) return '\\';
            else if (x == 3 * (size + 1) - y) return '/';
            else return ' ';
        }
    }
}
