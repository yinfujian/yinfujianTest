package org.springframework.context.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple implementation of NestingMessageSource that allows messages
 * to be held in a Java object, and added programmatically.
 * This class now supports internationalization.
 * NestingMessageSource的简单实现，它允许消息保存在Java对象中，并以编程方式添加。这个类现在支持国际化。
 *
 * <p>Intended for testing, rather than use production systems.
 *
 * @author Rod Johnson
 */
public class StaticMessageSource extends AbstractNestingMessageSource {

	private final Log logger = LogFactory.getLog(getClass());

	// message 容器
	private Map messages = new HashMap();

	/**
	 * @see AbstractNestingMessageSource#messageKey(Locale, String)
	 */
	protected String resolve(String code, Locale locale) {
		return (String) this.messages.get(messageKey(locale, code));
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
   * @param locale locale message should be found within
	 * @param message message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String message) {
		this.messages.put(messageKey(locale, code), message);
		logger.info("Added message [" + message + " for code [" + code + "] and Locale [" + locale + "]");
	}

}

