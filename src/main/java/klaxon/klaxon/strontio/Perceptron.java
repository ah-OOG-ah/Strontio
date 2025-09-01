package klaxon.klaxon.strontio;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public record Perceptron(float[] weights, float bias, Function<Float, Float> transfer) {
    public static final Random RAND = new Random();

    public Perceptron mutate(float amount) {
        final var toMut = RAND.nextInt(weights.length + 1);
        amount = (RAND.nextFloat() * 2 - 1) * amount;

        if (toMut == weights.length) return new Perceptron(weights, bias + amount, transfer);
        final var newWeights = weights.clone();
        newWeights[toMut] += amount;
        return new Perceptron(newWeights, bias, transfer);
    }

    public float accept(float[] inputs) {
        float sum = 0;
        for (int i = 0; i < weights.length; ++i) {
            sum += weights[i] * inputs[i] + bias;
        }
        return transfer.apply(sum);
    }

    public boolean accept(Pop pop) {
        return accept(new float[] {pop.weight(), pop.height()}) > 0;
    }

    @Override
    public @NotNull String toString() {
        return "Perceptron[weights=" + Arrays.toString(weights) + ", bias=" + bias + ", transfer=" + transfer.toString() + "]";
    }
}
