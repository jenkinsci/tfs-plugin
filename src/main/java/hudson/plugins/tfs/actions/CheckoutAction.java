package hudson.plugins.tfs.actions;

import hudson.plugins.tfs.model.ChangeSet;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface CheckoutAction  {

    List<ChangeSet> checkout() throws IOException, InterruptedException, ParseException;
	
}
