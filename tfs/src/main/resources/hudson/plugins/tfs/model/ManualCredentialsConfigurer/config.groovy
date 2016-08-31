package hudson.plugins.tfs.model.ManualCredentialsConfigurer;

def f = namespace(lib.FormTagLib);

f.entry(title: "User name", field:"userName",
        description:"Domain alias, e-mail address or alternate credentials") {
    f.textbox()
}

f.entry(title: "User password", field:"password") {
    f.password()
}

