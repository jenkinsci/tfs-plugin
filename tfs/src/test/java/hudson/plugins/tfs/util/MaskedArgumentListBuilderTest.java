package hudson.plugins.tfs.util;

import static org.junit.Assert.*;
import hudson.plugins.tfs.Util;

import org.junit.Test;


public class MaskedArgumentListBuilderTest {

    @Test
    public void assertEmptyMask() {
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        builder.add("arg");
        builder.add("other", "arguments");
        
        assertFalse("There shouldnt be any masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new Boolean[]{false,false,false}, Util.toBoxedArray(array));
    }
    
    @Test
    public void assertLastArgumentIsMasked() {
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        builder.add("arg");
        builder.addMasked("ismasked");
        
        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new Boolean[]{false,true}, Util.toBoxedArray(array));
    }
    
    @Test
    public void assertSeveralMaskedArguments() {
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        builder.add("arg");
        builder.addMasked("ismasked");
        builder.add("non masked arg");
        builder.addMasked("ismasked2");
        
        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new Boolean[]{false,true, false, true}, Util.toBoxedArray(array));
    }
    
    @Test
    public void assertPrependAfterAddingMasked() {
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        builder.addMasked("ismasked");
        builder.add("arg");
        builder.prepend("first", "second");
        
        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new Boolean[]{false,false,true,false}, Util.toBoxedArray(array));
    }
    
    @Test
    public void assertPrependBeforeAddingMasked() {
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        builder.prepend("first", "second");
        builder.addMasked("ismasked");
        builder.add("arg");
        
        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new Boolean[]{false,false,true,false}, Util.toBoxedArray(array));
    }
}
