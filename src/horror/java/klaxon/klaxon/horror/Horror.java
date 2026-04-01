package klaxon.klaxon.horror;

import static java.util.Arrays.asList;
import static klaxon.klaxon.horror.Files.readString;
import static klaxon.klaxon.horror.FormatHelper.formatError;
import static klaxon.klaxon.horror.FormatHelper.getNextSafeName;
import static klaxon.klaxon.horror.FormatHelper.resetSafeNames;
import static klaxon.klaxon.horror.FormatHelper.subSymbols;
import static klaxon.klaxon.horror.FormatHelper.unsubSymbols;
import static klaxon.klaxon.horror.TeXHelper.makeSplitEq;
import static klaxon.klaxon.horror.TeXHelper.makeTex;
import static org.matheclipse.core.expression.F.NIL;

import com.google.common.collect.HashBiMap;
import java.io.IOException;
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
    private static final ExprEvaluator EVAL = new ExprEvaluator();

    static void main(String[] args) {

        // Load input equation and variables
        if (args.length == 0) args = new String[]{"./inputs.csv"};
        for (var arg : args) parseEquationFile(arg);
    }

    private static void parseEquationFile(String eqFilePath) {
        EVAL.clearVariables();
        resetSafeNames();

        /*----------------------------------------- LOAD FROM FILE ---------------------------------------------------*/

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

        final var rawValues = Arrays.copyOfRange(values, 2, values.length);

        if (rawValues.length < rawVariableNames.length) {
            LOGGER.error("Not enough values! Expected {} values, found {}.", rawVariableNames.length, rawValues.length);
            return;
        } else if (rawValues.length > rawVariableNames.length) {
            LOGGER.warn("Too many values! Expected {} values, found {}.", rawVariableNames.length, rawValues.length);
        }

        // symja doesn't handle some characters properly, we gotta fix that
        //for (int i = 0; i < rawVariableNames.length; ++i) {
        //    rawVariableNames[i] = escapeSymbol(rawVariableNames[i]);
        //}

        // Load variable-error pairs
        final HashBiMap<ISymbol, SciValue> mappings = HashBiMap.create();
        final var variables = new ArrayList<ISymbol>();
        final var errors = new ArrayList<ISymbol>();
        final var constants = new ArrayList<ISymbol>();
        loadVariables(rawVariableNames, rawValues, errors, mappings, variables, constants);

        final var resultSym = EVAL.defineVariable(getNextSafeName());
        mappings.put(resultSym, new SciValue(values[0]));
        mappings.forEach((sym, val) -> {
            var str = Double.isNaN(val.value) ? "NaN" : val.getSymJaString();
            LOGGER.info("{}: {} with {}E{} = {} = {}, {}", sym, val.latexName, val.exactValue, val.power, val.value, str, val.isExact);
        });

        final var resultString = subSymbols(values[0], mappings);
        var equationString = subSymbols(values[1], mappings);


        /*--------------------------------------- END LOAD FROM FILE -------------------------------------------------*/
        /*-------------------------------------------- EVALUATE ------------------------------------------------------*/

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

        final var sumSquareLine1 = F.Sqrt(sumOfSquaresSimple);
        final var subbedPartialsLine2 = F.Sqrt(sumOfSquaresEquations);

        // In order to make the third line work, we need to...
        // 1) Continue calculations with exact variable values
        // 2) Print out the third line with *rounded* values
        // In order to achieve this, we can modify the generated TeX into thrid
        // Step 2: Copy it and add a tag var, something like `vOOOp`, multiplied by each number. (vOOOpe for errors)
        // Step 3: Replace all vOOOp*num with the number, but with OOO many sig figs
        IExpr finalEquation = subbedPartialsLine2.copy();
        for (var symPair : mappings.entrySet()) {
            var sym = symPair.getKey();
            var value = symPair.getValue();
            if (resultSym.equals(sym)) continue;

            var replacedEquation = finalEquation.replaceAll(F.Rule(sym, value.getSymJaValue()));
            if (replacedEquation != NIL) {
                finalEquation = replacedEquation;
            }
        }

        // Do it again, but only visually - round the values
        IExpr subbedValuesLine3 = subbedPartialsLine2.copy();
        for (var symPair : mappings.entrySet()) {
            var sym = symPair.getKey();
            var val = symPair.getValue();
            if (Double.isNaN(val.value)) continue;

            var r = subbedValuesLine3.replaceAll(F.Rule(sym, F.symjify(val.getSymJaString())));
            if (r != NIL) {
                subbedValuesLine3 = r;
            }
        }

        // Finally, compute (and pretty-print) the answer
        var ans = EVAL.eval(finalEquation);

        // Raw values
        LOGGER.info("First line: {}", sumSquareLine1);
        LOGGER.info("Second line: {}", subbedPartialsLine2);
        LOGGER.info("Third line: {}", finalEquation);

        // Convert to LaTeX
        final var tex1 = makeTex(sumSquareLine1);
        final var tex2 = makeTex(subbedPartialsLine2);
        final var tex3 = makeTex(subbedValuesLine3);

        // Pretty-print the answer, making sure trailing 0's are preserved if necessary
        var fans = EVAL.evalf(ans);
        LOGGER.info("Answer: {}", fans);
        final var tex4 = Double.isNaN(fans) ? makeTex(ans) : formatError(fans);

        var outDir = Path.of("./out");
        try { java.nio.file.Files.createDirectories(outDir); } catch (IOException e) { throw new RuntimeException(e); }
        final var combinedTex = makeSplitEq(makeTex(resultError), "eq1", tex1, tex2, tex3, tex4);
        TeXHelper.writeTex(
                unsubSymbols(combinedTex, mappings),
                outDir.resolve(eqFilePath.replaceFirst("\\.csv", ".tex")),
                false);
    }

    private static void loadVariables(String[] varNames, String[] varVals, ArrayList<ISymbol> errors, HashBiMap<ISymbol, SciValue> mappings, ArrayList<ISymbol> variables, ArrayList<ISymbol> constants) {
        final var symbols = new HashSet<>(asList(varNames));

        // Variables are always followed by their errors
        for (int varI = 0; varI < varNames.length; ++varI) {
            final var name = varNames[varI];
            final var value = varVals[varI];

            // Create a safe name and map it to the real one
            var sym = EVAL.defineVariable(getNextSafeName());
            final SciValue sciVal;
            if (symbols.contains("\\delta " + name)) {
                var errVal = varVals[varI + 1];
                sciVal = new SciValue(name, value, errVal);
                variables.add(sym);
            } else {
                sciVal = new SciValue(name, value);
                (name.startsWith("\\delta") ? errors : constants).add(sym);
            }

            mappings.put(sym, sciVal);
        }
    }

}
