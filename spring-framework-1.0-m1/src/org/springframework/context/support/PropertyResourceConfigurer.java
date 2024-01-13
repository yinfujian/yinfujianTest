package org.springframework.context.support;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;
import org.springframework.context.ApplicationContextException;

/**
 * Allows for configuration of individual bean properties from a property resource,
 * i.e. a properties file. Useful for custom config files targetted at system
 * administrators that override bean properties configured in the application context.
 * 允许从属性资源（即属性文件）配置单个bean属性。对于针对覆盖应用程序上下文中配置的bean属性的系统管理员的自定义配置文件非常有用。
 *
 * <p>Expects configuration lines of the following form:<br>
 * // 读的这个形式的
 * beanName.property=value
 *
 * @author Juergen Hoeller
 * @since 12.03.2003
 */
public class PropertyResourceConfigurer extends ApplicationObjectSupport implements BeanFactoryPostProcessor {

	private final Log logger = LogFactory.getLog(getClass());

	private String location;

	/**
	 * Set the location of the properties file. Allows for both a URL
	 * and a (file) path, according to the respective ApplicationContext.
	 * 设置属性文件的位置。根据各自的ApplicationContext，同时允许URL和（文件）路径。
	 * @see org.springframework.context.ApplicationContext#getResourceAsStream
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	public void postProcessBeanFactory(ListableBeanFactoryImpl beanFactory) throws ApplicationContextException {
		if (this.location != null) {
			logger.info("Loading properties '" + this.location + "'");
			Properties prop = new Properties();
			try {
				// 获取输出流
				prop.load(getApplicationContext().getResourceAsStream(this.location));
				for (Iterator it = prop.keySet().iterator(); it.hasNext();) {
					String key = (String) it.next();
					processKey(beanFactory, key, prop.getProperty(key));
				}
			} catch (IOException ex) {
				logger.warn("Could not load properties '" + this.location + "': " + ex.getMessage());
			}
		} else {
			logger.warn("No property resource location specified");
		}
	}

	protected void processKey(ListableBeanFactoryImpl factory, String key, String value) throws ApplicationContextException {
		try {
			int dotIndex = key.indexOf('.');
			if (dotIndex == -1) {
				throw new FatalBeanException("Invalid key (expected 'beanName.property')");
			}
			String beanName = key.substring(0, dotIndex);
			String beanProperty = key.substring(dotIndex+1);
			factory.registerAdditionalPropertyValue(beanName, new PropertyValue(beanProperty, value));
			logger.debug("Property " + key + " set to " + value);
		}
		catch (BeansException ex) {
			throw new ApplicationContextException("Could not set property '" + key + "' to value '" + value + "': " + ex.getMessage());
		}
	}

}
