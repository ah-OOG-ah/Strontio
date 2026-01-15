package klaxon.klaxon.horror;

import static java.lang.Double.NaN;
import static java.lang.Double.parseDouble;
import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.util.Arrays.asList;
import static klaxon.klaxon.horror.Files.readString;
import static klaxon.klaxon.horror.FormatHelper.escapeSymbol;
import static klaxon.klaxon.horror.FormatHelper.formatError;
import static klaxon.klaxon.horror.FormatHelper.makeDFormatter;
import static klaxon.klaxon.horror.FormatHelper.unescapeSymbol;
import static klaxon.klaxon.horror.TeXHelper.makeSplitEq;
import static klaxon.klaxon.horror.TeXHelper.makeTex;
import static org.matheclipse.core.expression.F.NIL;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.ISymbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Horror {
    private static final Logger LOGGER = LoggerFactory.getLogger("Horror");
    private static final ExprEvaluator EVAL = new ExprEvaluator();

    static void main(String[] args) {

        // Load input equation and variables
        if (args.length == 0) args = new String[]{"./inputs.csv"};
        for (var arg : args) parseEquationFile(arg);
    }

    private static void parseEquationFile(String eqFilePath) {
        EVAL.clearVariables();

        final var varOpt = readString(Path.of(eqFilePath), false);
        if (varOpt.isEmpty()) { LOGGER.error("Failed to read variables from {}!", eqFilePath);
            return;
        }

        final var varFile = varOpt.get().lines().filter(s -> !s.isBlank()).toList();
        if (varFile.size() < 2) { LOGGER.error("Missing data in input file!");
            return;
        }
        if (varFile.size() > 2) { LOGGER.warn("Line count mismatch in variable file... should only need two"); }

        final var headers = varFile.getFirst().split(",");
        final var values = varFile.get(1).split(",");
        final var rawVariableNames = Arrays.copyOfRange(headers, 2, headers.length);

        final var resultString = escapeSymbol(values[0]);
        var equationString = escapeSymbol(values[1]);
        final var rawValues = Arrays.copyOfRange(values, 2, values.length);

        if (rawValues.length < rawVariableNames.length) {
            LOGGER.error("Not enough values! Expected {} values, found {}.", rawVariableNames.length, rawValues.length);
            return;
        } else if (rawValues.length > rawVariableNames.length) {
            LOGGER.warn("Too many values! Expected {} values, found {}.", rawVariableNames.length, rawValues.length);
        }

        // symja doesn't handle some characters properly, we gotta fix that
        for (int i = 0; i < rawVariableNames.length; ++i) {
            rawVariableNames[i] = escapeSymbol(rawVariableNames[i]);
        }

        // Load variable-error pairs
        final var mappings = new Object2DoubleArrayMap<ISymbol>();
        final var variables = new ArrayList<ISymbol>();
        final var errors = new ArrayList<ISymbol>();
        final var constants = new ArrayList<ISymbol>();
        loadVariables(rawVariableNames, rawValues, errors, mappings, variables, constants);

        // Create a "display mapping", which rounds to even to only display significant figures.
        // Calculations are done using the normal mapping, but the LaTeX uses display-mapped numbers
        final var displayMapping = new Object2ObjectOpenHashMap<ISymbol, NumberFormat>(mappings.size());
        for (int i = 0; i < variables.size(); ++i) {
            final var err = errors.get(i);
            final var errVal = mappings.getDouble(err);

            var df = makeDFormatter(errVal);
            displayMapping.put(variables.get(i), df);
            displayMapping.put(err, df);
        }

        // Constants are always displayed as-is, they have infinite precision.

        final var result = EVAL.defineVariable(resultString);
        final var resultError = EVAL.defineVariable("\\delta " + resultString);
        final var equation = EVAL.eval(equationString);
        LOGGER.info("Evaluating: {}", equation);
        LOGGER.info("Using variables: {}", variables);
        LOGGER.info("Using errors: {}", errors);
        LOGGER.info("Using constants: {}", constants);

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
            var partialDeriv = F.Sqr(F.Times(errSym, EVAL.eval(F.D(equation, sym))));
            sumExpr = sumExpr == null ? partialDeriv : F.Plus(sumExpr, partialDeriv);
        }
        final var sumOfSquaresEquations = sumExpr;

        final var frist = F.Sqrt(sumOfSquaresSimple);
        final var snecod = F.Sqrt(sumOfSquaresEquations);

        // Third line has variables subbed in
        IExpr thrid = snecod.copy();
        var i = mappings.object2DoubleEntrySet().fastIterator();
        while (i.hasNext()) {
            var e = i.next();
            var r = thrid.replaceAll(F.Rule(e.getKey(), F.symjify(e.getDoubleValue())));
            if (r != NIL) {
                thrid = r;
            }
        }

        // Do it again, but rounding
        IExpr thridPretty = snecod.copy();
        i = mappings.object2DoubleEntrySet().fastIterator();
        while (i.hasNext()) {
            var e = i.next();
            var df = displayMapping.get(e.getKey());
            if (df == null) continue;

            var rounded = F.symjify(df.format(e.getDoubleValue()));
            var r = thridPretty.replaceAll(F.Rule(e.getKey(), F.symjify(rounded)));
            if (r != NIL) {
                thridPretty = r;
            }
        }

        // Finally, compute (and pretty-print) the answer
        var ans = EVAL.eval(thrid);

        // Raw values
        LOGGER.info("First line: {}", frist);
        LOGGER.info("Second line: {}", snecod);
        LOGGER.info("Third line: {}", thrid);

        // Convert to LaTeX
        final var tex1 = makeTex(frist);
        final var tex2 = makeTex(snecod);
        final var tex3 = makeTex(thridPretty);

        // Pretty-print the answer, making sure trailing 0's are preserved if necessary
        var fans = EVAL.evalf(ans);
        final var tex4 = Double.isNaN(fans) ? makeTex(ans) : formatError(fans);

        var outDir = Path.of("./out");
        try { java.nio.file.Files.createDirectories(outDir); } catch (IOException e) { throw new RuntimeException(e); }
        final var combinedTex = makeSplitEq(makeTex(resultError), "eq1", tex1, tex2, tex3, tex4);
        TeXHelper.writeTex(
                unescapeSymbol(combinedTex),
                outDir.resolve(eqFilePath.replaceFirst(".csv", ".tex")),
                false);
    }

    private static void loadVariables(String[] varNames, String[] varVals, ArrayList<ISymbol> errors, Object2DoubleArrayMap<ISymbol> mappings, ArrayList<ISymbol> variables, ArrayList<ISymbol> constants) {
        final var symbols = new HashSet<>(asList(varNames));

        // We assume variables and errors appear in the same order
        for (int i = 0; i < varNames.length; ++i) {
            final var value = varVals[i];
            switch (varNames[i]) {
                case null -> {} // ??? but handle anyway
                // is error
                case String s when s.startsWith("\\delta ") -> defineSymbol(mappings, errors, s, value);
                // has an error in the pool
                case String s when symbols.contains("\\delta " + s) -> defineSymbol(mappings, variables, s, value);
                // must be a constant then, it has no error
                case String s -> defineSymbol(mappings, constants, s, value);
            }
        }
    }

    private static void defineSymbol(Object2DoubleArrayMap<ISymbol> mappings, ArrayList<ISymbol> symbols, String s, String value) {
        var sym = EVAL.defineVariable(s);
        symbols.add(sym);
        mappings.put(sym, parseDouble(value));
    }
}
