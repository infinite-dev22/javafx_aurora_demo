module aurora_demo {
    requires java.logging;
    requires java.prefs;
    requires java.net.http;
    requires java.compiler;
    requires java.naming;
    requires javafx.graphics;

    opens inc.nomard.aurora_demo;
    exports inc.nomard.aurora_demo;
}
