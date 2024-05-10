package ro.linic.cloud.resource.server.starter.config;

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resource.server")
public class ResourceServerProperties {
	private String masterAuthorizerUrl;

	public String getMasterAuthorizerUrl() {
		return masterAuthorizerUrl;
	}

	public void setMasterAuthorizerUrl(final String masterAuthorizerUrl) {
		this.masterAuthorizerUrl = masterAuthorizerUrl;
	}

	@Override
	public int hashCode() {
		return Objects.hash(masterAuthorizerUrl);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ResourceServerProperties other = (ResourceServerProperties) obj;
		return Objects.equals(masterAuthorizerUrl, other.masterAuthorizerUrl);
	}
}
