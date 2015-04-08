package hudson.plugins.tfs.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class GetFilesToWorkFolderCommand extends AbstractCommand implements ParseableCommand<List<String>> {
    private static final Pattern ITEM_PATTERN = Pattern.compile("\\w+\\s+(.*)");
    private static final Pattern PATH_PATTERN = Pattern.compile("(.+):");
    
    private final String workFolder;
    private final boolean preview;
    private final String versionSpec;
    private final boolean useOverwrite;

    public GetFilesToWorkFolderCommand(ServerConfigurationProvider configurationProvider, String workFolder, boolean preview, String versionSpec, boolean useOverwrite) {
        super(configurationProvider);
        this.workFolder = workFolder;
        this.preview = preview;
        this.versionSpec = versionSpec;
        this.useOverwrite = useOverwrite;
    }

    public GetFilesToWorkFolderCommand(ServerConfigurationProvider configurationProvider, String workFolder, String versionSpec, boolean useOverwrite) {
        this(configurationProvider, workFolder, false, versionSpec, useOverwrite);
    }

    public GetFilesToWorkFolderCommand(ServerConfigurationProvider provider, String workFolder, boolean preview, boolean useOverwrite) {
        this(provider, workFolder, preview, null, useOverwrite);
    }

    public GetFilesToWorkFolderCommand(ServerConfigurationProvider provider, String workFolder, boolean useOverwrite) {
        this(provider, workFolder, false, null, useOverwrite);
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();
        arguments.add("get");
        arguments.add(workFolder);
        arguments.add("-recursive");
        if (preview) {
            arguments.add("-preview");
        }
        if (versionSpec != null) {
            arguments.add("-version:" + versionSpec);
        }
        arguments.add("-noprompt");
        if (useOverwrite) {
            arguments.add( "-overwrite");
        }
        addLoginArgument(arguments);
        return arguments;
    }

    public List<String> parse(Reader r) throws ParseException, IOException {
        BufferedReader reader = new BufferedReader(r);
        List<String> list = new ArrayList<String>();
        
        String line = reader.readLine();
        String lastPath = null;
        while (line != null) {
            Matcher matcher = PATH_PATTERN.matcher(line);
            if (matcher.matches()) {
                lastPath = matcher.group(1);
            } else {                
                if (lastPath != null) {
                    matcher = ITEM_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        list.add(lastPath + "\\" + matcher.group(1));
                    }
                }
            }            
            line = reader.readLine();
        }
        return list;
    }
}
