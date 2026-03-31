package klaxon.klaxon.jbest.preproc;

public record PreToken(String val) {
    public PreToken(StringBuilder buf) {
        this(buf.toString());
        buf.setLength(0);
    }
}
