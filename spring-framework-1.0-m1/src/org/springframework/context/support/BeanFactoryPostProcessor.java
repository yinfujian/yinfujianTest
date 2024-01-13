package org.springframework.context.support;

import org.springframework.beans.factory.support.ListableBeanFactoryImpl;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;

/**
 * 允许自定义修改应用程序上下文的bean。
 * 对于针对覆盖应用程序上下文中配置的bean属性的系统管理员的自定义配置文件非常有用。
 *
 * <p>For reading "beanName.property=value" configuration from a
 * properties file, consider using PropertyResourceConfigurer.
 *
 * @author Juergen Hoeller
 * @since 06.07.2003
 * @see PropertyResourceConfigurer
 */
public interface BeanFactoryPostProcessor extends ApplicationContextAware {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * 在标准初始化后修改应用程序上下文的内部bean工厂。所有bean定义都将被加载，但还没有任何bean被实例化。
	 * 这允许重写或添加属性，甚至可以添加到急于初始化的bean中。
	 * @param beanFactory the bean factory used by the application context
	 * @throws ApplicationContextException in case of initialization errors
	 */
	void postProcessBeanFactory(ListableBeanFactoryImpl beanFactory) throws ApplicationContextException;

}
