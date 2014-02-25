package hudson.plugins.tfs.commands;

import java.util.Calendar;

import hudson.Util;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.util.ArgumentListBuilder;

public abstract class AbstractCommand implements Command {

    protected static String getRangeSpecification(Calendar timestamp, int changeset) {
        String result;
        if(timestamp != null)
        {
            result = String.format("D%s", DateUtil.TFS_DATETIME_FORMATTER.get().format(timestamp.getTime()));
        }
        else
        {
            result = String.format("C%d", changeset); 
        }
        return result;
    }

    protected static Calendar getExclusiveToTimestamp(Calendar toTimestamp) {
        // The to timestamp is exclusive, ie it will only show history before the to timestamp.
        // This command should be inclusive.
        Calendar result = (Calendar) toTimestamp.clone();
        result.add(Calendar.SECOND, 1);
        return result;
    }

    private final ServerConfigurationProvider config;
    
    public AbstractCommand(ServerConfigurationProvider configurationProvider) {
        this.config = configurationProvider;
    }

    protected void addServerArgument(ArgumentListBuilder arguments) {
        arguments.add(String.format("-server:%s", config.getUrl()));
    }
    
    protected void addLoginArgument(MaskedArgumentListBuilder arguments) {
        if ((Util.fixEmpty(config.getUserName()) != null) && (config.getUserPassword()!= null)) {
            arguments.addMasked(String.format("-login:%s,%s", 
                    config.getUserName(), 
                    config.getUserPassword()));
        }
    }
}
