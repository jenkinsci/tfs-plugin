package hudson.plugins.tfs.model;

import java.io.IOException;

import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentitySearchFactor;
import com.microsoft.tfs.core.clients.webservices.MembershipQuery;
import com.microsoft.tfs.core.clients.webservices.ReadIdentityOptions;
import com.microsoft.tfs.core.clients.webservices.TeamFoundationIdentity;

import hudson.model.User;
import hudson.tasks.Mailer;

public class TfsUserLookup implements UserLookup {

    private final IIdentityManagementService ims;
    
    public TfsUserLookup(IIdentityManagementService ims) {
        this.ims = ims;
    }

    /**
     * @param accountName Windows NT account name: domain\alias.
     */
    public User find(String accountName) {
        final User jenkinsUser = User.get(accountName);
        Mailer.UserProperty mailerProperty = jenkinsUser.getProperty(Mailer.UserProperty.class);
        if (mailerProperty == null || mailerProperty.getAddress() == null) {
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
                // TODO: What should we do if an e-mail address is not configured?  Send to a default account??
                if (emailAddress != null) {
                    mailerProperty = new Mailer.UserProperty(emailAddress);
                    try {
                        jenkinsUser.addProperty(mailerProperty);
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return jenkinsUser;
    }

}
