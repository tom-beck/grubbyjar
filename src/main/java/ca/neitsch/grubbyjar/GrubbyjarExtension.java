package ca.neitsch.grubbyjar;

public class GrubbyjarExtension {
    private String _script;

    public void script(String s) {
        _script = s;
    }

    public String getScript() {
        return _script;
    }
}
