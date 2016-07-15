package hudson.plugins.tfs.VstsCollectionConfiguration;

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib);

f.entry(title: _("Collection URL"), field: "collectionUrl") {
    f.textbox()
}

f.entry(title: _("Credentials"), field: "credentialsId") {
    c.select()
}

f.block() {
    f.validateButton(
            title: _("Test connection"),
            progress: _("Testing..."),
            method: "testCredentials",
            with: "collectionUrl,credentialsId"
    )
}

f.entry {
    div(align: "right") {
        f.repeatableDeleteButton()
    }
}
