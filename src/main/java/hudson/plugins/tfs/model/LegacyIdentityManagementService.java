package hudson.plugins.tfs.model;

import org.apache.commons.lang.NotImplementedException;

import com.microsoft.tfs.core.clients.webservices.GroupProperty;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentityDescriptor;
import com.microsoft.tfs.core.clients.webservices.IdentitySearchFactor;
import com.microsoft.tfs.core.clients.webservices.MembershipQuery;
import com.microsoft.tfs.core.clients.webservices.ReadIdentityOptions;
import com.microsoft.tfs.core.clients.webservices.TeamFoundationIdentity;
import com.microsoft.tfs.util.GUID;

/**
 * An {@link IIdentityManagementService} implementation to provide similar
 * functionality to that provided by TFS 2010 and up on TFS 2008.  
 *
 * Right now, the {@link TfsUserLookup} is the only consumer of
 * {@link IIdentityManagementService} implementations, for the purpose of
 * determining a TFS user's display name and e-mail address, given their
 * account name.
 */
public class LegacyIdentityManagementService implements IIdentityManagementService {

    public TeamFoundationIdentity[] readIdentities(IdentityDescriptor[] paramArrayOfIdentityDescriptor,
            MembershipQuery paramMembershipQuery, ReadIdentityOptions paramReadIdentityOptions) {
        throw new NotImplementedException();
    }

    public TeamFoundationIdentity readIdentity(IdentityDescriptor paramIdentityDescriptor, MembershipQuery paramMembershipQuery,
            ReadIdentityOptions paramReadIdentityOptions) {
        throw new NotImplementedException();
    }

    public TeamFoundationIdentity[] readIdentities(GUID[] paramArrayOfGUID, MembershipQuery paramMembershipQuery) {
        throw new NotImplementedException();
    }

    public TeamFoundationIdentity[][] readIdentities(IdentitySearchFactor paramIdentitySearchFactor, String[] paramArrayOfString,
            MembershipQuery paramMembershipQuery, ReadIdentityOptions paramReadIdentityOptions) {
        throw new NotImplementedException();
    }

    public TeamFoundationIdentity readIdentity(IdentitySearchFactor searchFactor,
            String accountName,
            MembershipQuery membershipQuery, ReadIdentityOptions readIdentityOptions) {
        return new TeamFoundationIdentity(new IdentityDescriptor("identityType", "identifier"), accountName, true, null, null);
    }

    public IdentityDescriptor createApplicationGroup(String paramString1, String paramString2, String paramString3) {
        throw new NotImplementedException();
    }

    public TeamFoundationIdentity[] listApplicationGroups(String paramString, ReadIdentityOptions paramReadIdentityOptions) {
        throw new NotImplementedException();
    }

    public void updateApplicationGroup(IdentityDescriptor paramIdentityDescriptor, GroupProperty paramGroupProperty,
            String paramString) {
        throw new NotImplementedException();
    }

    public void deleteApplicationGroup(IdentityDescriptor paramIdentityDescriptor) {
        throw new NotImplementedException();
    }

    public void addMemberToApplicationGroup(IdentityDescriptor paramIdentityDescriptor1, IdentityDescriptor paramIdentityDescriptor2) {
        throw new NotImplementedException();
    }

    public void removeMemberFromApplicationGroup(IdentityDescriptor paramIdentityDescriptor1,
            IdentityDescriptor paramIdentityDescriptor2) {
        throw new NotImplementedException();
    }

    public boolean isMember(IdentityDescriptor paramIdentityDescriptor1, IdentityDescriptor paramIdentityDescriptor2) {
        throw new NotImplementedException();
    }

    public boolean refreshIdentity(IdentityDescriptor paramIdentityDescriptor) {
        throw new NotImplementedException();
    }

    public String getScopeName(String paramString) {
        throw new NotImplementedException();
    }

    public boolean isOwner(IdentityDescriptor paramIdentityDescriptor) {
        throw new NotImplementedException();
    }

    public boolean isOwnedWellKnownGroup(IdentityDescriptor paramIdentityDescriptor) {
        throw new NotImplementedException();
    }

    public String getIdentityDomainScope() {
        throw new NotImplementedException();
    }

}