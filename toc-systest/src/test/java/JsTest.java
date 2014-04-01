import java.io.IOException;

import org.junit.BeforeClass;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

public class JsTest {
    @BeforeClass
    public static void setupJs() throws IOException {
        Context cx = ContextFactory.getGlobal().enterContext();
        cx.setOptimizationLevel(-1);
        cx.setLanguageVersion(Context.VERSION_1_5);
        Global global = Main.getGlobal();
        global.init(cx);
        Main.processSource(cx, "src/test/resources/jssetup.js");
    }

    @org.junit.Test
    public void test() {
    }
}