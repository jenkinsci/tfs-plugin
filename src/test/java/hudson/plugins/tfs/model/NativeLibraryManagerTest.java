package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.persistence.PersistenceStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NativeLibraryManagerTest {

    @Test public void extractFile() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PersistenceStore store = mock(PersistenceStore.class);
        when(store.containsItem(isA(String.class))).thenReturn(false);
        when(store.getItemOutputStream(isA(String.class))).thenReturn(baos);
        final NativeLibraryManager manager = new NativeLibraryManager(store);

        manager.extractFile("win32", "x86", "native_auth.dll");

        Assert.assertEquals(67240, baos.size());
    }

    @Test public void extractFile_noArch() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PersistenceStore store = mock(PersistenceStore.class);
        when(store.containsItem(isA(String.class))).thenReturn(false);
        when(store.getItemOutputStream(isA(String.class))).thenReturn(baos);
        final NativeLibraryManager manager = new NativeLibraryManager(store);

        manager.extractFile("macosx", null, "libnative_auth.jnilib");

        Assert.assertEquals(118960, baos.size());
    }

    @Test public void extractFiles() throws Exception {
        final NativeLibraryExtractor extractor = mock(NativeLibraryExtractor.class);

        NativeLibraryManager.extractFiles(extractor);

        verify(extractor, times(82)).extractFile(isA(String.class), Matchers.<String>anyObject(), isA(String.class));
    }
}
