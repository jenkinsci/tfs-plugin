package hudson.plugins.tfs.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentitySearchFactor;
import com.microsoft.tfs.core.clients.webservices.MembershipQuery;
import com.microsoft.tfs.core.clients.webservices.ReadIdentityOptions;
import com.microsoft.tfs.core.clients.webservices.TeamFoundationIdentity;

import hudson.model.User;
import hudson.tasks.Mailer;

/**
 * Finds user information via the UserAccountMappers that it knows about.
 */
public class TfsUserLookup implements UserLookup {

    private static final Logger logger = Logger.getLogger(TfsUserLookup.class.getName());

    private final IIdentityManagementService ims;
    private final UserAccountMapper userAccountMapper;

    public TfsUserLookup(final IIdentityManagementService ims, final UserAccountMapper userAccountMapper) {
        this.ims = ims;
        this.userAccountMapper = userAccountMapper;
    }

    /**
     * Finds the User instance for the account name provided.
     * @param accountName Windows NT account name: domain\alias.
     */
    public User find(final String accountName) {
        final String mappedAccountName = userAccountMapper.mapUserAccount(accountName);
        logger.log(Level.FINE, String.format("Looking up Jenkins user for account '%s'.", mappedAccountName));
        final User jenkinsUser = User.get(mappedAccountName);
        Mailer.UserProperty mailerProperty = jenkinsUser.getProperty(Mailer.UserProperty.class);
        if (mailerProperty == null || mailerProperty.getAddress() == null || mailerProperty.getAddress().length() == 0) {
            logger.log(Level.FINE, String.format("No Mailer.UserProperty defined for '%s', looking in TFS", mappedAccountName));
            final TeamFoundationIdentity tfsUser = ims.readIdentity(
                IdentitySearchFactor.ACCOUNT_NAME,
                accountName,
                MembershipQuery.NONE,
                ReadIdentityOptions.NONE
            );
            if (tfsUser != null) {
                final String displayName = tfsUser.getDisplayName();
                jenkinsUser.setFullName(displayName);
                final String emailAddress = (String) tfsUser.getProperty("Mail");
                if (emailAddress != null) {
                    mailerProperty = new Mailer.UserProperty(emailAddress);
                    try {
                        jenkinsUser.addProperty(mailerProperty);
                    } catch (IOException e) {
                        logger.warning(String.format("Unable to save Jenkins account for  user '%s'.", accountName));
                    }
                } else {
                    logger.info(String.format("User '%s' did not have an e-mail address configured.", accountName));
                }
            } else {
                logger.warning(String.format("Unable to find user '%s'.", accountName));
            }
        }
        return jenkinsUser;
    }

}
