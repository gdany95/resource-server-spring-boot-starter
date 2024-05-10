package ro.linic.cloud.resource.server.starter.components;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class I18n {
	private MessageSource messageSource;

	public I18n(final MessageSource messageSource) {
		super();
		this.messageSource = messageSource;
	}

	public String msg(final String code) {
		// Attention LocaleContextHolder.getLocale() is thread based,
		// maybe you need some fallback locale
		return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
	
	public String msg(final String code, final Object... args) {
		// Attention LocaleContextHolder.getLocale() is thread based,
		// maybe you need some fallback locale
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}
}