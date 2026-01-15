package klaxon.klaxon.horror;

import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
import static klaxon.klaxon.horror.Files.readString;
import static klaxon.klaxon.horror.TeXHelper.makeSplitEq;
import static klaxon.klaxon.horror.TeXHelper.makeTex;
import static org.matheclipse.core.expression.F.NIL;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;
import java.util.regex.Pattern;
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

        LOGGER.info("First line: {}", frist);
        LOGGER.info("Second line: {}", snecod);

        // Convert to LaTeX
        final var tex1 = makeTex(frist);
        final var tex2 = makeTex(snecod);
        final var tex3 = makeTex(thrid);

        var outDir = Path.of("./out");
        try { java.nio.file.Files.createDirectories(outDir); } catch (IOException e) { throw new RuntimeException(e); }
        final var combinedTex = makeSplitEq(makeTex(resultError), "eq1", tex1, tex2, tex3, makeTex(EVAL.eval(thrid)));
        TeXHelper.writeTex(
                unescapeSymbol(combinedTex),
                outDir.resolve(eqFilePath.replaceFirst(".csv", ".tex")),
                false);
    }

    /// symja doesn't properly handle several characters I want to use. This converts them into characters I *don't*
    /// want to use, but symja is fine with.
    /// - `_` -> `uuu`
    /// - `capital letters` -> `zzlowercase letterszz`
    private static final ArrayList<Function<String, String>> ESCAPES_FWD = new ArrayList<>();
    private static final ArrayList<Function<String, String>> ESCAPES_BCKWD = new ArrayList<>();
    static {
        ESCAPES_FWD.add(s -> s.replaceAll("([A-Z]+)", "zz$1zz").toLowerCase());
        final var pattern = Pattern.compile("zz([a-z]+)zz");
        ESCAPES_BCKWD.add(s -> {
            var m = pattern.matcher(s);
            var sb = new StringBuilder(s.length());
            while (m.find()) {
                m.appendReplacement(sb, m.group(1).toUpperCase());
            }
            m.appendTail(sb);
            return sb.toString();
        });
        // needs to be second, to avoid ruining the first one on reverse
        ESCAPES_FWD.add(s -> s.replaceAll("_", "uuu"));
        ESCAPES_BCKWD.add(s -> s.replaceAll("uuu", "_"));
    }

    /// See [#ESCAPES_FWD] for the list of escaped symbols
    private static String escapeSymbol(String sym) {
        var ret = sym;
        for (var escaper : ESCAPES_FWD) {
            ret = escaper.apply(ret);
        }
        return ret;
    }

    /// See [#ESCAPES_FWD] for the list of escaped symbols
    private static String unescapeSymbol(String sym) {
        var ret = sym;
        for (var unescaper : ESCAPES_BCKWD) {
            ret = unescaper.apply(ret);
        }
        return ret;
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
