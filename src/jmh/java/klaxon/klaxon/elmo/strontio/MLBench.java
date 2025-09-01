package klaxon.klaxon.elmo.strontio;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private Perceptron p;
    private ArrayList<Pop> POPULATION = new ArrayList<>();

    @Setup
    public void setup() {
        p = new Perceptron(new float[]{1, 1}, 0.5f, new Function<>() {
            @Override
            public Float apply(Float f) {
                return (float) round(f);
            }

            @Override
            public String toString() {
                return "f -> (float) round(f)";
            }
        });

        POPULATION.add(new Pop(34, 128, false));
        POPULATION.add(new Pop(35, 150, false));
        POPULATION.add(new Pop(40, 144, false));
        POPULATION.add(new Pop(50, 150, false));
        POPULATION.add(new Pop(40, 110, true));
        POPULATION.add(new Pop(60, 120, true));
        POPULATION.add(new Pop(75, 108, true));
        POPULATION.add(new Pop(80, 144, true));
    }

    @Benchmark
    @Measurement(time = 5, iterations = 3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(1)
    @Warmup(iterations = 1)
    public void trainPerceptron() {
        float lastHit = 0;
        var lastPerceptron = p;

        while (true) {
            float hitrate = 0;
            for (var pop : POPULATION) {
                final var result = p.accept(new float[]{pop.weight(), pop.height()}) > 0;
                final var correct = result == pop.obese();
                hitrate += correct ? (100.0f / POPULATION.size()) : 0.0f;
            }

            if (lastHit > hitrate) p = lastPerceptron;
            else {
                lastPerceptron = p;
                lastHit = hitrate;
                p = p.mutate(0.5f);
            }

            if (abs(hitrate - 100.0) < 0.01) return;
        }
    }
}
