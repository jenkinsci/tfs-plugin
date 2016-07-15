package hudson.plugins.tfs.VstsCollectionConfiguration;

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib);

f.entry(title: _("Collection URL"), field: "collectionUrl") {
    f.textbox()
}

f.entry(title: _("Credentials"), field: "credentialsId") {
    c.select()
}

f.entry {
    div(align: "right") {
        f.repeatableDeleteButton()
    }
}
