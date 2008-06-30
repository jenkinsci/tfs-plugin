package hudson.plugins.tfs.action;

import hudson.plugins.tfs.TfTool;

/**
 * Get action to retrieve all files from a TeamFoundation repository.
 * <p/>
 * tf get itemspec [/version:versionspec] [/all] [/overwrite] [/force] [/preview] [/recursive] [/noprompt]
 * 
 * @author Erik Ramfelt
 */
public class DefaultGetAction {

    public void getFiles(TfTool tool, boolean cleanCopy) {
        
        // create workspace (if needed)
        
        // map workspace to local folder
        
        // get files to local folder
        
        /*
            First create a workspace:

            tf /server:https://tfs01.codeplex.com /login:snd\\UID,PWD
            workspace /new "MACHINENAME;UID"

            Then map "Turtle" project to a local folder:

            tf /server:https://tfs01.codeplex.com /login:snd\\UID,PWD
            workfold "$/Turtle" ~/Source/turtle

            Then pull the files:

            tf /server:https://tfs01.codeplex.com /login:snd\\UID,PWD get
            ~/Source/turtle /recursive
            */
    }

}
