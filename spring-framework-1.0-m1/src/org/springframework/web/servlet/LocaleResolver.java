package org.springframework.web.servlet;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for web-based locale resolution strategies that allows for
 * both locale resolution via the request and locale modification via
 * request and response.
 *
 * <p>This interface allows for implementations based on request, session,
 * cookies, etc. The default implementation is AcceptHeaderLocaleResolver,
 * simply using the request's locale provided by the respective HTTP header.
 *
 * <p>Use RequestContext.getLocale() to retrieve the current locale in
 * controllers or views, independent of the actual resolution strategy.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
 * @see org.springframework.web.servlet.support.RequestContext#getLocale
 */
public interface LocaleResolver {

  /**
   * Resolve the current locale via the given request.
   * Should return a default locale as fallback in any case.
   * @param request request to be used for resolution
   * @return the current locale
   */
	Locale resolveLocale(HttpServletRequest request);

  /**
   * Set the current locale to the given one.
   * @param request request to be used for locale modification
   * @param response response to be used for locale modification
   * @param locale the new locale
   */
	void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);

}
