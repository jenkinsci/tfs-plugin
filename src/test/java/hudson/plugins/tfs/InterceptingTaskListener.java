package hudson.plugins.tfs;

import hudson.console.ConsoleNote;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class InterceptingTaskListener implements TaskListener {

    private final TaskListener victim;
    private final List<String> errors = new ArrayList<String>();
    private final List<String> fatalErrors = new ArrayList<String>();

    public InterceptingTaskListener(final TaskListener victim) {
        this.victim = victim;
    }

    public PrintStream getLogger() {
        return victim.getLogger();
    }

    public void annotate(final ConsoleNote ann) throws IOException {
        victim.annotate(ann);
    }

    public void hyperlink(final String url, final String text) throws IOException {
        victim.hyperlink(url, text);
    }

    private static void add(final List<String> destination, final String format, final Object... args) {
        final Formatter formatter = new Formatter();
        formatter.format(format, args);
        final String message = formatter.toString();
        destination.add(message);
    }

    public PrintWriter error(final String msg) {
        errors.add(msg);
        return victim.error(msg);
    }

    public PrintWriter error(final String format, final Object... args) {
        add(errors, format, args);
        return victim.error(format, args);
    }

    public PrintWriter fatalError(final String msg) {
        fatalErrors.add(msg);
        return victim.fatalError(msg);
    }

    public PrintWriter fatalError(final String format, final Object... args) {
        add(fatalErrors, format, args);
        return victim.fatalError(format, args);
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getFatalErrors() {
        return fatalErrors;
    }
}
