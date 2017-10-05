package ca.neitsch.grubyjar;

public class GrubyjarExtension {
    private String _script;

    public void script(String s) {
        _script = s;
    }

    public String getScript() {
        return _script;
    }
}
