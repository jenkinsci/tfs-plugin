package hudson.plugins.tfs.TeamCollectResultsPostBuildAction;

def f = namespace(lib.FormTagLib);

// this would look/feel nicer if the Add button was a drop-down list, like for build steps
f.entry(title: _("Build results to collect"),
        help: descriptor.getHelpFile()) {
    f.repeatableProperty(field: "requestedResults") {
        f.entry {
            div(align: "right") {
                f.repeatableDeleteButton()
            }
        }
    }
}
