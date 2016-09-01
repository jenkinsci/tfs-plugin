package hudson.plugins.tfs.model.TeamRequestedResult;

def f = namespace(lib.FormTagLib);

f.entry(title: _("Type"), field: "teamResultType") {
    f.select()
}

f.entry(title: _("Glob patterns"), field: "patterns") {
    f.textarea()
}
