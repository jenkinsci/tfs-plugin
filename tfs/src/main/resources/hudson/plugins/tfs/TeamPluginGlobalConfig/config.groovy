package hudson.plugins.tfs.TeamPluginGlobalConfig;

def f = namespace(lib.FormTagLib);

f.section(title: descriptor.displayName) {
    f.entry(title: _("Team Project Collections"),
            field: "collectionConfigurations",
            help: descriptor.getHelpFile()) {

        f.repeatableProperty(field: "collectionConfigurations")
    }
    f.advanced() {
        f.entry(title: _("Store TFVC configuration in computer-specific folders"),
                field: "configFolderPerNode",
                description: "Warning: don't turn this on unless you know what you are doing!") {
            f.checkbox (default: false)
        }
    }
}
