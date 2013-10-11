package hudson.plugins.tfs.model;

import hudson.model.User;

public interface UserLookup {
    /**
     * @param accountName Windows NT account name: domain\alias.
     */
    User find(String accountName);
}
