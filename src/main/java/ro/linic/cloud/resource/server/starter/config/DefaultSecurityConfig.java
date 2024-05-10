package ro.linic.cloud.resource.server.starter.config;

import static ro.linic.util.commons.PresentationUtils.safeString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.web.reactive.function.client.WebClient;

import ro.linic.cloud.resource.server.starter.components.TenantContext;
import ro.linic.cloud.resource.server.starter.components.TenantFilter;

@AutoConfiguration(before = {OAuth2ResourceServerAutoConfiguration.class, OAuth2ClientAutoConfiguration.class})
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(ResourceServerProperties.class)
public class DefaultSecurityConfig {
	private static final Logger log = Logger.getLogger(DefaultSecurityConfig.class.getName());
	private static final String[] AUTH_WHITELIST = {
			"/actuator/**", "help/**", "swagger-ui/**", "swagger-resources/**", "v3/**", "webjars/**"};
			  
	@Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
		http.addFilterBefore(new TenantFilter(), DisableEncodeUrlFilter.class)
		.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers(AUTH_WHITELIST).permitAll()
				.anyRequest().authenticated())
		.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
		.csrf(csrf -> csrf.disable());
        return http.build();
    }
	
	@Bean
	@ConditionalOnMissingBean
	public JwtAuthenticationConverter jwtAuthenticationConverter(final ResourceServerProperties resourceServerProperties,
			final WebClient webClient) {
	    final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
	    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new CustomAuthoritiesConverter(webClient,
	    		safeString(resourceServerProperties.getMasterAuthorizerUrl(), "http://localhost:9000")));
	    return jwtAuthenticationConverter;
	}
	
	static class CustomAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
		private final WebClient webClient;
		private final String masterAuthorizerUrl;
		
		public CustomAuthoritiesConverter(final WebClient webClient, final String masterAuthorizerUrl) {
			this.webClient = webClient;
			this.masterAuthorizerUrl = Objects.requireNonNull(masterAuthorizerUrl);
		}

		@Override
		public Collection<GrantedAuthority> convert(final Jwt jwt) {
			final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
			for (final String authority : getAuthorities(jwt))
				grantedAuthorities.add(new SimpleGrantedAuthority(authority));
			
			if (grantedAuthorities.isEmpty())
				throw new AccessDeniedException("User "+jwt.getSubject()+" has no authorities for tenant: "+TenantContext.getCurrentTenantId());
			
			return grantedAuthorities;
		}

		private Set<String> getAuthorities(final Jwt jwt) {
			try {
				return this.webClient
				          .get()
				          .uri(masterAuthorizerUrl+"/user/"+jwt.getSubject()+"/authorities")
				          .header("X-TenantID", TenantContext.getCurrentTenantId().toString())
				          .retrieve()
				          .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
				          .block();
			} catch (final Exception e) {
				log.log(Level.SEVERE, e.getMessage(), e);
				return Set.of();
			}
		}
	}
}
