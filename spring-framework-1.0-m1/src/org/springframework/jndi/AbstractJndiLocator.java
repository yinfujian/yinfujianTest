/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jndi;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Convenient superclass for JNDI-based Service Locators. Subclasses are
 * JavaBeans, exposing a jndiName property. This may or may not include
 * the "java:comp/env/" prefix expected by J2EE applications. If it doesn't,
 * the container prefix will be prepended if the "inContainer" property is
 * true (the default) and no other scheme like "java:" is given.
 *
 * <p>Subclasses must implement the located() method to cache the results
 * of the JNDI lookup. They don't need to worry about error handling.
 *
 * <p><b>Assumptions: </b>The resource obtained from JNDI can be cached.
 *
 * @author Rod Johnson
 * @version $Id: AbstractJndiLocator.java,v 1.2 2003/08/22 12:20:36 jhoeller Exp $
 * @see #setInContainer
 */
public abstract class AbstractJndiLocator implements InitializingBean {

	/** JNDI prefix used in a J2EE container */
	public static String CONTAINER_PREFIX = "java:comp/env/";

	protected final Log logger = LogFactory.getLog(getClass());

	private JndiTemplate jndiTemplate = new JndiTemplate();

	private String jndiName;

	private boolean inContainer = true;

	/**
	 * Create a new JNDI locator. The jndiName property must be set,
	 * and afterPropertiesSet be called to perform the JNDI lookup.
	 * <p>Obviously, this class is typically used via a BeanFactory.
	 */
	public AbstractJndiLocator() {
	}

	/**
	 * Create a new JNDI locator, specifying the JNDI name. If the name
	 * doesn't include a java:comp/env/ prefix, it will be prepended.
	 * <p>As this is a shortcut, it calls afterPropertiesSet to perform
	 * the JNDI lookup immediately.
	 * @param jndiName JNDI name.
	 */
	public AbstractJndiLocator(String jndiName) throws NamingException, IllegalArgumentException {
		setJndiName(jndiName);
		afterPropertiesSet();
	}

	/**
	 * Set the JNDI template to use for the JNDI lookup.
	 */
	public final void setJndiTemplate(JndiTemplate template) {
		jndiTemplate = template;
	}

	/**
	 * Return the JNDI template to use for the JNDI lookup.
	 */
	public final JndiTemplate getJndiTemplate() {
		return jndiTemplate;
	}

	/**
	 * Set the JNDI name. If it doesn't begin "java:comp/env/"
	 * we add this prefix if we're running in a container.
	 * @param jndiName JNDI name of bean to look up
	 * @see #setInContainer
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * Return the JNDI name to look up.
	 */
	public String getJndiName() {
		return jndiName;
	}

	/**
	 * Set if the lookup occurs in a J2EE container, i.e. if the prefix
	 * "java:comp/env/" needs to be added if the JNDI name doesn't already
	 * contain it. Default is true.
	 * <p>Note: Will only get applied if no other scheme like "java:" is given.
	 */
	public void setInContainer(boolean inContainer) {
		this.inContainer = inContainer;
	}

	/**
	 * Return if the lookup occurs in a J2EE container.
	 */
	public boolean isInContainer() {
		return inContainer;
	}

	public final void afterPropertiesSet() throws NamingException, IllegalArgumentException {
		if (this.jndiName == null || this.jndiName.equals("")) {
			throw new IllegalArgumentException("Property 'jndiName' must be set on " + getClass().getName());
		}
		// prepend container prefix if not already specified and no other scheme given
		if (this.inContainer && !this.jndiName.startsWith(CONTAINER_PREFIX) && this.jndiName.indexOf(':') == -1) {
			this.jndiName = CONTAINER_PREFIX + this.jndiName;
		}
		Object o = lookup(this.jndiName);
		located(o);
	}

	private Object lookup(String jndiName) throws NamingException {
		Object o = this.jndiTemplate.lookup(jndiName);
		logger.debug("Successfully looked up object with jndiName '" + jndiName + "': value=[" + o + "]");
		return o;
	}

	/**
	 * Subclasses must implement this to cache the object this class has obtained
	 * from JNDI.
	 * @param o object successfully retrieved from JNDI
	 */
	protected abstract void located(Object o);

}
