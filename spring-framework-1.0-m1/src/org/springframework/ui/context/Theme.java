package org.springframework.ui.context;

import org.springframework.context.MessageSource;

/**
 * A Theme can resolve theme-specific messages, codes, file paths, etc
 * (e.g. CSS and image files in a web environment).
 * The MessageSource supports parameterization and internationalization.
 * @author Juergen Hoeller
 * @since 17.06.2003
 * @see ThemeSource
 * @see org.springframework.web.servlet.theme
 */
public interface Theme {

	/**
	 * Return the name of the theme.
	 * @return the name of the theme
	 */
	String getName();

	/**
	 * Return the specific MessageSource that resolves messages
	 * with respect to this theme.
	 * @return the theme-specific MessageSource
	 */
	MessageSource getMessageSource();

}
