package ro.linic.cloud.resource.server.starter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import ro.linic.cloud.resource.server.starter.components.I18n;

@AutoConfiguration
public class DefaultBeanConfig {
	@Bean
	@ConditionalOnMissingBean
    public I18n i18n(final MessageSource messageSource) {
        return new I18n(messageSource);
    }
	
	@Bean
	@ConditionalOnMissingBean
	public WebClient webClient(final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager auth2AuthorizedClientManager) {
        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
            new ServerOAuth2AuthorizedClientExchangeFilterFunction(auth2AuthorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("authorities-reader");
        return WebClient.builder()
            .filter(oauth2Client)
            .build();
    }
	
	@Bean
	@ConditionalOnMissingBean
    public AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager(
        final ReactiveClientRegistrationRepository clientRegistrationRepository) {
        final InMemoryReactiveOAuth2AuthorizedClientService clientService =
            new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
        final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
            ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, clientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }
	
	@Bean
	@ConditionalOnMissingBean
	public InMemoryReactiveClientRegistrationRepository reactiveClientRegistrationRepository(final OAuth2ClientProperties properties) {
		final List<ClientRegistration> registrations = new ArrayList<>(
				new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());
		return new InMemoryReactiveClientRegistrationRepository(registrations);
	}
}
