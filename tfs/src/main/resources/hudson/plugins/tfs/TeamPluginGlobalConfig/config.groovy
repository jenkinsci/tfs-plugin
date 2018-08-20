package hudson.plugins.tfs.TeamPluginGlobalConfig;

def f = namespace(lib.FormTagLib);

f.section(title: descriptor.displayName) {
    f.entry(title: _("Team Project Collections"),
            field: "collectionConfigurations",
            help: descriptor.getHelpFile()) {

        f.repeatableProperty(field: "collectionConfigurations")
    }
    f.entry(title: _("Enable Push Trigger for all jobs"),
            field: "enableTeamPushTriggerForAllJobs",
            description: "Turning this on is equivalent to adding the 'Build when a change is pushed to TFS/Team Services' trigger to all jobs.") {
        f.checkbox (default: false)
    }
    f.entry(title: _("Enable Team Status for all jobs"),
            field: "enableTeamStatusForAllJobs",
            description: "Turning this on is equivalent to adding the 'Set build pending status in TFS/Team Services' build step and the 'Set build completion status in TFS/Team Services' post-build action to all jobs.") {
        f.checkbox (default: false)
    }
    f.entry() {
        f.dropdownDescriptorSelector(
                title: _("User account name mapping strategy"),
                field: "userAccountMapper",
                descriptors: descriptor.getUserAccountMapperDescriptors()
        )
    }
    f.advanced() {
        f.entry(title: _("Store TFVC configuration in computer-specific folders"),
                field: "configFolderPerNode",
                description: "Warning: don't turn this on unless you know what you are doing!") {
            f.checkbox (default: false)
        }
    }
    f.entry(title: _("Release WebHooks"),
            field: "releaseWebHookConfigurations") {

        f.repeatableProperty(field: "releaseWebHookConfigurations")
    }
}
