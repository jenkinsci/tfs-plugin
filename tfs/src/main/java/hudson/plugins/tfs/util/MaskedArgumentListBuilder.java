package hudson.plugins.tfs.util;

import java.util.Collection;
import java.util.HashSet;

import hudson.util.ArgumentListBuilder;

/**
 * ArgumentListBuilder that supports marking arguments as masked.
 * 
 * @author Erik Ramfelt
 */
public class MaskedArgumentListBuilder extends ArgumentListBuilder{

    private static final long serialVersionUID = 1L;
    
    private Collection<Integer> maskedArgumentIndex;

    @Override
    public ArgumentListBuilder prepend(String... args) {
        if (maskedArgumentIndex != null) {
            Collection<Integer> newMaskedArgumentIndex = new HashSet<Integer>();
            for (Integer argIndex : maskedArgumentIndex) {
                newMaskedArgumentIndex.add(argIndex + args.length);
            }
            maskedArgumentIndex = newMaskedArgumentIndex;
        }
        return super.prepend(args);        
    }
    
    /**
     * Returns true if there are any masked arguments.
     * @return true if there are any masked arguments; false otherwise
     */
    public boolean hasMaskedArguments() {
        return (maskedArgumentIndex != null);
    }

    /**
     * Returns an array of booleans where the masked arguments are marked as true
     * @return an array of booleans.
     */
    public boolean[] toMaskArray() {
        String[] commands = toCommandArray();
        boolean[] mask = new boolean[commands.length];
        if (maskedArgumentIndex != null) { 
            for (Integer argIndex : maskedArgumentIndex) {
                mask[argIndex] = true;
            }
        }
        return mask;
    }

    /**
     * Add a masked argument
     * @param string the argument
     */
    public void addMasked(String string) {
        if (maskedArgumentIndex == null) {
            maskedArgumentIndex = new HashSet<Integer>();
        }
        maskedArgumentIndex.add(toCommandArray().length);
        add(string);
    }

}
