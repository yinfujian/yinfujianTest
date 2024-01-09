package org.springframework.web.servlet.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;

/**
 * Implementation of ThemeResolver that uses a theme attribute in the user's
 * session in case of a custom setting, with a fallback to the fixed default theme.
 * This is most appropriate if the application needs user sessions anyway.
 *
 * <p>Custom controllers can override the user's theme by calling setTheme,
 * e.g. responding to a theme change request.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 */
public class SessionThemeResolver extends AbstractThemeResolver {

	/**
	 * Name of the session attribute that holds the theme name. Only used
	 * internally by this implementation. Use RequestContext.getTheme()
	 * to retrieve the current theme in controllers or views.
	 * @see org.springframework.web.servlet.support.RequestContext#getTheme
	 */
	public static final String THEME_SESSION_ATTRIBUTE_NAME = SessionThemeResolver.class.getName() + ".THEME";

	public String resolveThemeName(HttpServletRequest request) {
		String theme = (String) WebUtils.getSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME);
		// specific theme, or fallback to default?
		return (theme != null ? theme : getDefaultThemeName());
	}

	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
		WebUtils.setSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME, themeName);
	}

}
