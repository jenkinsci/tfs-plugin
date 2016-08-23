package hudson.plugins.tfs.TeamCollectionConfiguration;

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib);

f.entry(title: _("Collection URL"), field: "collectionUrl") {
    f.textbox()
}

f.entry(title: _("Credentials"), field: "credentialsId",
        description: "Depending on the integration features used, the user account or personal access token may need code_read, code_status and/or work_write permissions") {
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
