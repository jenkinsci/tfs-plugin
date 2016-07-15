package hudson.plugins.tfs.VstsPluginGlobalConfig;

def f = namespace(lib.FormTagLib);

f.section(title: descriptor.displayName) {
    f.entry(title: _("Team Project Collections"),
            field: "collectionConfigurations",
            help: descriptor.getHelpFile()) {

        f.repeatableProperty(field: "collectionConfigurations")
    }
}
