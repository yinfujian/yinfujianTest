/**
 * The Spring framework is distributed under the Apache
 * Software License.
 */

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

/**
 * Interface to be implemented by objects used within a BeanFactory
 * that are themselves factories. If a bean implements this interface,
 * it is used as a factory, not directly as a bean.
 *
 * <p><b>NB: A bean that implements this interface cannot be used
 * as a normal bean.</b>
 *
 * <p>FactoryBeans can support singletons and prototypes.
 *
 * @author Rod Johnson
 * @since March 08, 2003
 * @see org.springframework.beans.factory.BeanFactory
 * @version $Id: FactoryBean.java,v 1.2 2003/08/19 16:25:02 jhoeller Exp $
 */
public interface FactoryBean {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory. As with a BeanFactory, this allows
	 * support for both the Singleton and Prototype design pattern.
	 * @return an instance of the bean
	 */
	Object getObject() throws BeansException;

	/**
	 * Is the bean managed by this factory a singleton or a prototype?
	 * That is, will getBean() always return the same object?
	 * <p>The singleton status of a FactoryBean will generally
	 * be provided by the owning BeanFactory.
	 * @return if this bean is a singleton
	 */
	boolean isSingleton();

	/**
	 * Property values to pass to new bean instances created
	 * by this factory. Mapped directly onto the bean instance using
	 * reflection. This occurs <i>after</i> any configuration of the
	 * instance performed by the factory itself, and is an optional
	 * step within the control of the owning BeanFactory.
	 * @return PropertyValues to pass to each new instance,
	 * or null (the default) if there are no properties to
	 * pass to the instance
	 */
	PropertyValues getPropertyValues();

}
