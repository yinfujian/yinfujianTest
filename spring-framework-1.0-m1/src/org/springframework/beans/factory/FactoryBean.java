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
 * 要由BeanFactory中使用的对象实现的接口，这些对象本身就是工厂。如果bean实现了这个接口，那么它将被用作工厂，而不是直接作为bean。
 *
 * <p><b>NB: A bean that implements this interface cannot be used
 * as a normal bean.</b>
 * 实现此接口的bean不能用作普通bean。
 *
 * <p>FactoryBeans can support singletons and prototypes.
 * FactoryBeans可以支持singleton和prototype。
 * 支持单例和原型
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
	 * 返回此工厂管理的对象的实例（可能是共享的或独立的）。与BeanFactory一样，这允许同时支持Singleton和Prototype设计模式。
	 * @return an instance of the bean
	 */
	Object getObject() throws BeansException;

	/**
	 * Is the bean managed by this factory a singleton or a prototype?
	 * That is, will getBean() always return the same object?
	 * <p>The singleton status of a FactoryBean will generally
	 * be provided by the owning BeanFactory.
	 * 这个工厂管理的bean是单例还是原型？
	 * 也就是说，getBean()是否总是返回相同的对象？
	 * <p>FactoryBean的单例状态通常由拥有的BeanFactory提供。
	 * @return if this bean is a singleton
	 */
	boolean isSingleton();

	/**
	 * Property values to pass to new bean instances created
	 * by this factory. Mapped directly onto the bean instance using
	 * reflection. This occurs <i>after</i> any configuration of the
	 * instance performed by the factory itself, and is an optional
	 * step within the control of the owning BeanFactory.
	 * 属性值传递给该工厂创建的新bean实例。
	 * 使用反射直接映射到bean实例上。
	 * 这发生在工厂本身执行的实例的任何配置之后<i>，并且是所属BeanFactory控制范围内的可选步骤。
	 * @return PropertyValues to pass to each new instance,
	 * or null (the default) if there are no properties to
	 * pass to the instance
	 */
	PropertyValues getPropertyValues();

}
