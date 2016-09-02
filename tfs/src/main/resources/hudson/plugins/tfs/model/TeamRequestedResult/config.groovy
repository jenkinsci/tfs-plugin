package hudson.plugins.tfs.model.TeamRequestedResult;

def f = namespace(lib.FormTagLib);

f.entry(title: _("Type"), field: "teamResultType") {
    f.select()
}

f.entry(title: _("Files to include"), field: "includes",
        description: "<a href='http://ant.apache.org/manual/Types/fileset.html'>Fileset 'includes'</a> setting that specifies the files to collect. Basedir of the fileset is <a href='ws/'>the workspace root</a>.") {
    f.textbox()
}
