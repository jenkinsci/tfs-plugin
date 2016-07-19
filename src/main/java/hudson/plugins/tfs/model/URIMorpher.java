package hudson.plugins.tfs.model;

import net.sf.ezmorph.MorphException;
import net.sf.ezmorph.ObjectMorpher;

import java.net.URI;
import java.net.URISyntaxException;

public class URIMorpher implements ObjectMorpher {

    public static final URIMorpher INSTANCE = new URIMorpher();

    private URIMorpher() {

    }

    @Override
    public Object morph(final Object value) {
        if (value == null) {
            return null;
        }

        if (URI.class.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (!supports(value.getClass())) {
            throw new MorphException(value.getClass() + " is not supported");
        }

        final String s = value.toString();
        try {
            final URI result = new URI(s);
            return result;
        }
        catch (final URISyntaxException ignored) {
            throw new MorphException("'" + s + "' is not a valid URI");
        }
    }

    @Override
    public Class morphsTo() {
        return URI.class;
    }

    @Override
    public boolean supports(final Class clazz) {
        return String.class.isAssignableFrom(clazz);
    }
}
