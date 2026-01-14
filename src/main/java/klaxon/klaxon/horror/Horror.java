package klaxon.klaxon.horror;

import static java.util.Arrays.asList;
import static klaxon.klaxon.horror.Files.readString;
import static klaxon.klaxon.horror.TeXHelper.appendTex;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.form.tex.TeXFormFactory;
import org.matheclipse.core.interfaces.IExpr;
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
        final var varNames = Arrays.copyOfRange(headers, 2, headers.length);

        final var resultString = values[0];
        final var equationString = values[1];
        final var varVals = Arrays.copyOfRange(values, 2, values.length);

        if (varVals.length < varNames.length) {
            LOGGER.error("Not enough values! Expected {} values, found {}.", varNames.length, varVals.length);
            return;
        } else if (varVals.length > varNames.length) {
            LOGGER.warn("Too many values! Expected {} values, found {}.", varNames.length, varVals.length);
        }

        // Start parsing things
        final var evaluator = new ExprEvaluator();
        final var equation = evaluator.eval(equationString);
        LOGGER.info("Evaluating: {}", equation);
        LOGGER.info("Using variables: {}", Arrays.toString(varNames));

        // Load variable-error pairs
        final var result = evaluator.defineVariable(resultString);
        final var resultError = evaluator.defineVariable("\\delta " + resultString);
        final var symbols = new HashSet<>(asList(varNames));
        final var variables = new ArrayList<IExpr>();
        final var errors = new ArrayList<IExpr>();
        final var constants = new ArrayList<IExpr>();

        for (var sym : varNames) {
            switch (sym) {
                case null -> {} // ??? but handle anyway
                case String s when s.startsWith("\\delta ") -> {} // error, skip it
                case String _ when symbols.contains("\\delta " + sym) -> {
                    variables.add(evaluator.defineVariable(sym)); // variable
                    // we add the corresponding error here, to ensure the lists share indices
                    errors.add(evaluator.defineVariable("\\delta " + sym));
                }
                default -> constants.add(evaluator.defineVariable(sym)); // must be a constant then, it has no error
            }
        }

        // Line 1: spit out the error preparation
        // Generate the expressions!
        IExpr sumExpr = null;
        for (int i = 0; i < variables.size(); ++i) {
            var sym = variables.get(i);
            var errSym = errors.get(i);
            var partialDeriv = F.Sqr(F.Times(errSym, F.D(result, sym)));
            sumExpr = sumExpr == null ? partialDeriv : F.Plus(sumExpr, partialDeriv);
        }
        final var sumOfSquaresSimple = sumExpr;

        // And evaluate the partials
        sumExpr = null;
        for (int i = 0; i < variables.size(); ++i) {
            var sym = variables.get(i);
            var errSym = errors.get(i);
            var partialDeriv = F.Sqr(F.Times(errSym, evaluator.eval(F.D(equation, sym))));
            sumExpr = sumExpr == null ? partialDeriv : F.Plus(sumExpr, partialDeriv);
        }
        final var sumOfSquaresEquations = sumExpr;

        final var frist = F.Set(resultError, F.Sqrt(sumOfSquaresSimple));
        final var snecod = F.Sqrt(sumOfSquaresEquations);

        LOGGER.info("First line: {}", frist);
        LOGGER.info("Second line: {}", snecod);

        // Third line fills in variables
        final var latexFactory = new TeXFormFactory();
        var buffer = new StringBuilder();
        latexFactory.convert(buffer, frist);
        final var tex1 = buffer.toString(); buffer = new StringBuilder();
        latexFactory.convert(buffer, snecod);
        final var tex2 = buffer.toString();
        TeXHelper.writeTex(appendTex(tex1, tex2), varPath.replaceFirst(".csv", ".tex"), false);
    }
}
