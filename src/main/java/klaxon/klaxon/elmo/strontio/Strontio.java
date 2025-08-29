package klaxon.klaxon.elmo.strontio;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Function;
import org.jfree.data.xy.DefaultXYDataset;

public class Strontio {
    public static final ArrayList<Pop> POPULATION = new ArrayList<>();

    public static void main(String[] args) {
        var p = new Perceptron(new float[]{1, 1}, 0.5f, new Function<Float, Float>() {
            @Override
            public Float apply(Float f) {
                return (float) round(f);
            }

            @Override
            public String toString() {
                return "f -> (float) round(f)";
            }
        });
        final var reader = new Scanner(System.in);

        POPULATION.add(new Pop(34, 128, false));
        POPULATION.add(new Pop(35, 150, false));
        POPULATION.add(new Pop(40, 144, false));
        POPULATION.add(new Pop(50, 150, false));
        POPULATION.add(new Pop(40, 110, true));
        POPULATION.add(new Pop(60, 120, true));
        POPULATION.add(new Pop(75, 108, true));
        POPULATION.add(new Pop(80, 144, true));

        float lastHit = 0;
        var lastPerceptron = p;

        while (true) {
            float hitrate = 0;
            for (var pop : POPULATION) {
                final var result = p.accept(new float[]{pop.weight(), pop.height()}) > 0;
                final var correct = result == pop.obese();
                hitrate += correct ? (100.0f / POPULATION.size()) : 0.0f;
            }

            System.out.println("Hitrate: " + hitrate + "%");

            if (lastHit > hitrate) p = lastPerceptron;
            else {
                lastPerceptron = p;
                lastHit = hitrate;
                p = p.mutate(0.5f);
            }

            if (abs(hitrate - 100.0) < 0.01) {
                System.out.println("Perceptron success!");
                System.out.println(p);
                break;
            }
        }

        final var data = new DefaultXYDataset();
        Perceptron finalP = p;
        double[] xs = POPULATION.stream().filter(finalP::accept).mapToDouble(Pop::weight).toArray();
        double[] ys = POPULATION.stream().filter(finalP::accept).mapToDouble(Pop::height).toArray();
        data.addSeries("fat", new double[][] { xs, ys });
        xs = POPULATION.stream().filter(po -> !finalP.accept(po)).mapToDouble(Pop::weight).toArray();
        ys = POPULATION.stream().filter(po -> !finalP.accept(po)).mapToDouble(Pop::height).toArray();
        data.addSeries("not fat", new double[][] { xs, ys });

        // wx/-w + 2b/-w = y
        final var minX = POPULATION.stream().mapToDouble(Pop::weight).min().getAsDouble();
        final var maxX = POPULATION.stream().mapToDouble(Pop::weight).max().getAsDouble();
        final var len = 10;
        xs = new double[len];
        ys = new double[len];
        final var b = p.bias();
        final var wx = p.weights()[0];
        final var wy = p.weights()[1];

        for (int i = 0; i < len; ++i) {
            var x = lerp(minX, maxX, i / (len - 1.0));
            ys[i] = (wx * x / -wy) + 2 * b / -wy;
            xs[i] = x;
        }

        data.addSeries("perceptron", new double[][] {xs, ys});

        ScatterChart.chart("Height and Weight of People", data, "x", "y");
    }

    static double lerp(double a, double b, double f) {
        return (b - a) * f + a;
    }
}
