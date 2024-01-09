package org.springframework.web.servlet.tags;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.ui.context.Theme;

/**
 * Custom tag to look up a theme message in the scope of this page.
 * Messages are looked up using the ApplicationContext's ThemeSource,
 * and thus should support internationalization.
 *
 * <p>Regards a HTML escaping setting, either on this tag instance,
 * the page level, or the web.xml level.
 *
 * <p>If "code" isn't set or cannot be resolved, "text" will be used
 * as default message.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see org.springframework.ui.context.Theme
 * @see org.springframework.ui.context.ThemeSource
 * @see #setCode
 * @see #setText
 * @see #setHtmlEscape
 * @see HtmlEscapeTag#setDefaultHtmlEscape
 * @see HtmlEscapeTag#HTML_ESCAPE_CONTEXT_PARAM
 */
public class ThemeTag extends MessageTag {

	/**
	 * Use the theme MessageSource for theme message resolution.
	 */
	protected MessageSource getMessageSource() {
		Theme theme = getRequestContext().getTheme();
		return (theme != null) ? theme.getMessageSource() : null;
	}

	/**
	 * Return exception message that indicates the current theme.
	 */
	protected String getNoSuchMessageExceptionDescription(NoSuchMessageException ex) {
		return "Theme '" + getRequestContext().getTheme().getName() + "': " + ex.getMessage();
	}

}
