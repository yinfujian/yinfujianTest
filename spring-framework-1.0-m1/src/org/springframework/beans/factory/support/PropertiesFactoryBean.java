package org.springframework.beans.factory.support;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassLoaderUtils;

/**
 * Allows for making a properties file from a classpath location available
 * as Properties instance in a bean factory. Can be used for to populate
 * any bean property of type Properties via a bean reference.
 * @author Juergen Hoeller
 * @see java.util.Properties
 */
public class PropertiesFactoryBean implements FactoryBean {

	private final Log logger = LogFactory.getLog(getClass());

	private Properties properties = new Properties();

	/**
	 * Set the location of the properties file as classpath resource,
	 * e.g. "/myprops.properties".
	 */
	public void setLocation(String location) throws IOException {
		logger.info("Loading properties [" + location + "]");
		this.properties = new Properties();
		this.properties.load(ClassLoaderUtils.getResourceAsStream(getClass(), location));
	}

	public Object getObject() {
		return this.properties;
	}

	public boolean isSingleton() {
		return true;
	}

	public PropertyValues getPropertyValues() {
		return null;
	}

}
