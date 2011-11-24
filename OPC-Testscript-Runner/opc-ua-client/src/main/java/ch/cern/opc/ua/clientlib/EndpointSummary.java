package ch.cern.opc.ua.clientlib;

import java.util.ArrayList;
import java.util.List;

import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.UserTokenPolicy;

public class EndpointSummary 
{
	final String URL;
	final String securityMode;
	final String securityPolicy;
	final String userTokenPolicies;
	final String transportProfile;
	final String securityLevel;
	
	public static EndpointSummary[] toEndpointSummaries(final EndpointDescription[] endpoints)
	{
		List<EndpointSummary> result = new ArrayList<EndpointSummary>();
		
		for(EndpointDescription endpoint : endpoints)
		{
			result.add(new EndpointSummary(endpoint));
		}
		
		return result.toArray(new EndpointSummary[0]);
	}
	
	public static EndpointDescription matchEndpoint(final EndpointSummary summary, final EndpointDescription[] endpoints)
	{
		for(EndpointDescription endpoint : endpoints)
		{
			if(summary.isMatch(endpoint)) return endpoint;
		}
		
		return null;
	}
	
	private EndpointSummary(final EndpointDescription endpoint)
	{
		URL = endpoint.getEndpointUrl();
		securityMode = endpoint.getSecurityMode().toString();
		securityPolicy = endpoint.getSecurityPolicyUri();
		userTokenPolicies = getTransportPoliciesAsSingleString(endpoint);
		transportProfile = endpoint.getTransportProfileUri();
		securityLevel = endpoint.getSecurityLevel().toString();
	}

	private String getTransportPoliciesAsSingleString(final EndpointDescription endpoint) 
	{
		StringBuffer result = new StringBuffer();
		
		for(UserTokenPolicy token : endpoint.getUserIdentityTokens())
		{
			result.append(token+" ");
		}
		
		return result.toString();
	}
	
	public boolean isMatch(final EndpointDescription endpoint)
	{
		return this.equals(new EndpointSummary(endpoint));
	}
	
	@Override
	public String toString() 
	{
		return new StringBuffer().
		append("Endpoint Summary\n").
		append("\tURL: "+URL+"\n").
		append("\tSecurityMode: "+securityMode+"\n").
		append("\tSecurityPolicy: "+securityPolicy+"\n").
		append("\tUserTokenPolicies: "+userTokenPolicies+"\n").
		append("\tTransportProfile: "+transportProfile+"\n").
		append("\tSecurity Level: "+securityLevel+"\n").
		toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((URL == null) ? 0 : URL.hashCode());
		result = prime * result
				+ ((securityLevel == null) ? 0 : securityLevel.hashCode());
		result = prime * result
				+ ((securityMode == null) ? 0 : securityMode.hashCode());
		result = prime * result
				+ ((securityPolicy == null) ? 0 : securityPolicy.hashCode());
		result = prime
				* result
				+ ((transportProfile == null) ? 0 : transportProfile.hashCode());
		result = prime
				* result
				+ ((userTokenPolicies == null) ? 0 : userTokenPolicies
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EndpointSummary other = (EndpointSummary) obj;
		if (URL == null) {
			if (other.URL != null)
				return false;
		} else if (!URL.equals(other.URL))
			return false;
		if (securityLevel == null) {
			if (other.securityLevel != null)
				return false;
		} else if (!securityLevel.equals(other.securityLevel))
			return false;
		if (securityMode == null) {
			if (other.securityMode != null)
				return false;
		} else if (!securityMode.equals(other.securityMode))
			return false;
		if (securityPolicy == null) {
			if (other.securityPolicy != null)
				return false;
		} else if (!securityPolicy.equals(other.securityPolicy))
			return false;
		if (transportProfile == null) {
			if (other.transportProfile != null)
				return false;
		} else if (!transportProfile.equals(other.transportProfile))
			return false;
		if (userTokenPolicies == null) {
			if (other.userTokenPolicies != null)
				return false;
		} else if (!userTokenPolicies.equals(other.userTokenPolicies))
			return false;
		return true;
	}
}
