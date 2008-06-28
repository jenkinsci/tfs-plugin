package hudson.plugins.tfs.model;

public class TeamFoundationCredentials {

    private final String username;
    private final String password;
    private final String domain;

    public TeamFoundationCredentials(String username, String password, String domain) {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }

    public String getLoginStr() {
        return username + "@" + domain + "," + password;
    }

}
