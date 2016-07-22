package hudson.plugins.tfs.model;

import net.sf.ezmorph.MorphException;
import net.sf.ezmorph.ObjectMorpher;

public class GitStatusStateMorpher implements ObjectMorpher {

    public static final GitStatusStateMorpher INSTANCE = new GitStatusStateMorpher();

    private GitStatusStateMorpher() {

    }

    @Override
    public Object morph(final Object value) {
        if (value == null) {
            return null;
        }

        if (!supports(value.getClass())) {
            throw new MorphException(value.getClass() + " is not supported");
        }

        final String s = value.toString();
        return GitStatusState.caseInsensitiveValueOf(s);
    }

    @Override
    public Class morphsTo() {
        return GitStatusState.class;
    }

    @Override
    public boolean supports(Class clazz) {
        return String.class.isAssignableFrom(clazz);
    }
}
