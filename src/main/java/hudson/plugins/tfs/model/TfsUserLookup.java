package hudson.plugins.tfs.model;

import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentitySearchFactor;
import com.microsoft.tfs.core.clients.webservices.MembershipQuery;
import com.microsoft.tfs.core.clients.webservices.ReadIdentityOptions;
import com.microsoft.tfs.core.clients.webservices.TeamFoundationIdentity;

import hudson.model.User;
import hudson.tasks.Mailer;

public class TfsUserLookup implements UserLookup {

    private static final Logger LOGGER = Logger.getLogger(TfsUserLookup.class.getName());

    private final IIdentityManagementService ims;
    
    public TfsUserLookup(IIdentityManagementService ims) {
        this.ims = ims;
    }

    /**
     * @param accountName Windows NT account name: domain\alias.
     */
    public User find(String accountName) {
        LOGGER.fine("Looking up Jenkins user for TFS account '%s'.");
        final User jenkinsUser = User.get(accountName);
        Mailer.UserProperty mailerProperty = jenkinsUser.getProperty(Mailer.UserProperty.class);
        if (mailerProperty == null || mailerProperty.getAddress() == null || mailerProperty.getAddress().length() == 0) {
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
                        LOGGER.warning(String.format("Unable to save Jenkins account for TFS user '%s'.", accountName));
                    }
                }
                else {
                    LOGGER.info(String.format("TFS user '%s' did not have an e-mail address configured.", accountName));
                }
            }
            else {
                LOGGER.warning(String.format("Unable to find TFS user '%s'.", accountName));
            }
        }
        return jenkinsUser;
    }

}
