package hudson.plugins.tfs.rm.ReleaseWebHook;

def f = namespace(lib.FormTagLib);

f.entry(title: _("WebHook Name"), field: "webHookName") {
    f.textbox();
}

f.entry(title: _("Payload URL"), field: "payloadUrl") {
    f.textbox();
}

f.entry(title: _("Secret"), field: "secret") {
    f.password();
}

f.entry {
    div(align: "right") {
        f.repeatableDeleteButton()
    }
}