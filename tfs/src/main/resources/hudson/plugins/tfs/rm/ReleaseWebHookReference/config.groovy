package hudson.plugins.tfs.rm.ReleaseWebHookAction;

def f = namespace(lib.FormTagLib);
 
f.entry(title: _("Release Webhook"), field: "webHookName") {
    f.select();
}

f.entry {
    div(align: "right") {
        f.repeatableDeleteButton()
    }
}
