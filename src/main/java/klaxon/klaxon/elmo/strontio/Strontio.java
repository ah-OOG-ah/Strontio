package klaxon.klaxon.elmo.strontio;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Function;

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
    }
}
