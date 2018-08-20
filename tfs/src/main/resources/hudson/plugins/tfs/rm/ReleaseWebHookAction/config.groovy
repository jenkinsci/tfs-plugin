package hudson.plugins.tfs.rm.ReleaseWebHookAction;

def f = namespace(lib.FormTagLib);

f.entry(title: _("Release Webhooks"),
            field: "webHookReferences") {

        f.repeatableProperty(field: "webHookReferences")
    }
