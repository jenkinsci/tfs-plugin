package hudson.plugins.tfs.model;

import hudson.model.User;

/**
 * An interface to define the UserLookup contract.
 */
public interface UserLookup {
    /**
     * @param accountName Windows NT account name: domain\alias.
     *
     * @return the Jenkins {@link User} object associated with the account name
     */
    User find(String accountName);
}
