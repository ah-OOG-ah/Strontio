package klaxon.klaxon.horror;

import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
import static klaxon.klaxon.horror.Files.readString;
import static klaxon.klaxon.horror.TeXHelper.appendTexs;
import static klaxon.klaxon.horror.TeXHelper.makeTex;
import static org.matheclipse.core.expression.F.NIL;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import java.nio.file.Path;
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
        final var mappings = new Object2DoubleArrayMap<ISymbol>();
        final var symbols = new HashSet<>(asList(varNames));
        final var variables = new ArrayList<ISymbol>();
        final var errors = new ArrayList<ISymbol>();
        final var constants = new ArrayList<ISymbol>();

        // We assume variables and errors appear in the same order
        for (int i = 0; i < varNames.length; ++i) {
            final var value = varVals[i];
            switch (varNames[i]) {
                case null -> {} // ??? but handle anyway
                case String sym when sym.startsWith("\\delta ") -> { // error
                    var err = evaluator.defineVariable(sym);
                    errors.add(err);
                    mappings.put(err, parseDouble(value));
                }
                case String sym when symbols.contains("\\delta " + sym) -> { // variable
                    var var = evaluator.defineVariable(sym);
                    variables.add(var);
                    mappings.put(var, parseDouble(value));
                }
                case String sym -> { // must be a constant then, it has no error
                    var conzt = evaluator.defineVariable(sym);
                    constants.add(conzt);
                    mappings.put(conzt, parseDouble(value));
                }
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

        // Convert to LaTeX
        final var tex1 = makeTex(frist);
        final var tex2 = makeTex(snecod);

        // Now substitute variables
        IExpr thrid = snecod.copy();
        var i = mappings.object2DoubleEntrySet().fastIterator();
        while (i.hasNext()) {
            var e = i.next();
            var r = thrid.replaceAll(F.Rule(e.getKey(), F.symjify(e.getDoubleValue())));
            if (r != NIL) {
                thrid = r;
            }
        }
        final var tex3 = makeTex(thrid);

        TeXHelper.writeTex(
                appendTexs(tex1, tex2, tex3, makeTex(evaluator.eval(thrid))),
                varPath.replaceFirst(".csv", ".tex"),
                false);
    }
}
