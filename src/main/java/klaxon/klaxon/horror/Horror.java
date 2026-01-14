package klaxon.klaxon.horror;

import static java.util.Arrays.asList;
import static klaxon.klaxon.horror.Files.readString;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.form.tex.TeXFormFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Horror {
    private static final Logger LOGGER = LoggerFactory.getLogger("Horror");

    static void main(String[] args) {

        // Load input equation and variables
        final var varPath = args.length < 1 ? "./inputs.csv" : args[0];
        final var varOpt = readString(Path.of(varPath), false);
        if (varOpt.isEmpty()) { LOGGER.error("Failed to read variables from {}!", varPath); return; }

        final var varFile = varOpt.get().lines().filter(s -> !s.isBlank()).toList();
        if (varFile.size() < 2) { LOGGER.error("Missing data in input file!"); return; }
        if (varFile.size() > 2) { LOGGER.warn("Line count mismatch in variable file... should only need two"); }

        final var headers = varFile.getFirst().split(",");
        final var values = varFile.get(1).split(",");
        final var equationString = values[0];
        final var varNames = Arrays.copyOfRange(headers, 1, headers.length);
        final var varVals = Arrays.copyOfRange(values, 1, values.length);

        if (varVals.length < varNames.length) {
            LOGGER.error("Not enough values! Expected {} values, found {}.", varNames.length, varVals.length);
            return;
        } else if (varVals.length > varNames.length) {
            LOGGER.warn("Too many values! Expected {} values, found {}.", varNames.length, varVals.length);
        }

        // Start parsing things
        final var evaluator = new ExprEvaluator();
        final var javaFunc = evaluator.eval(equationString);
        LOGGER.info("Evaluating: {}", javaFunc);
        LOGGER.info("Using variables: {}", Arrays.toString(varNames));

        // Load variable-error pairs
        final var symbols = new HashSet<>(asList(varNames));
        final var variables = new ArrayList<String>();
        final var constants = new ArrayList<String>();

        for (var sym : varNames) {
            switch (sym) {
                case null -> {} // ??? but handle anyway
                case String s when s.startsWith("δ") -> {} // error, skip it
                case String _ when symbols.contains("δ" + sym) -> variables.add(sym); // variable
                default -> constants.add(sym); // must be a constant then, it has no error
            }
        }

        // Line 1: spit out the error preparation
        // Generate the expressions!
        final var sumOfSquares = variables.stream().map(var -> {
            var symbol = evaluator.defineVariable(var);
            var errorSymbol = evaluator.defineVariable("δ" + var);
            var partialDeriv = F.D(javaFunc, symbol);
            return F.Sqr(F.Times(errorSymbol, partialDeriv));
        }).reduce(F::Plus).orElseThrow();

        final var quadrature = F.Sqrt(sumOfSquares);

        LOGGER.info("First line: {}", quadrature);
        LOGGER.info("Second line: {}", evaluator.eval(quadrature));

        // Third line fills in variables
        evaluator.clearVariables();

        final var latexFactory = new TeXFormFactory();
        final var buffer = new StringBuilder();
        latexFactory.convert(buffer, quadrature);
        TeXHelper.writeTex(buffer.toString(), varPath.replaceFirst(".csv", ".tex"), false);
    }
}
