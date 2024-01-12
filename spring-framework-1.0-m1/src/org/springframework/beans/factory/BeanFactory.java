/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by objects that hold a number of bean definitions,
 * each uniquely identified by a String name. An independent instance of any of
 * these objects can be obtained (the Prototype design pattern), or a single
 * shared instance can be obtained (a superior alternative to the Singleton
 * design pattern). Which type of instance will be returned depends on the bean
 * factory configuration - the API is the same. The Singleton approach is much
 * more useful and more common in practice.
 * 接口被实现存储bean的定义信息，每一个bean信息拥有一个唯一性String 名字。
 * 可以获得这些对象中任何一个的独立实例（原型设计模式），或者可以获得单个共享实例（Singleton设计模式的高级替代方案）。
 * 返回哪种类型的实例取决于bean工厂配置——API是相同的。Singleton方法更有用，在实践中也更常见。
 *
 * <p>The point of this approach is that the BeanFactory is a central registry
 * of application components, and centralizes the configuring of application
 * components (no more do individual objects need to read properties files,
 * for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and
 * Development" for a discussion of the benefits of this approach.
 * 这种方法的要点是BeanFactory是应用程序组件的中心注册表，并集中配置应用程序组件（例如，单个对象不再需要读取属性文件）。
 * 请参阅“Expert One-on-One J2EE Design and Development”的第4章和第11章，以讨论这种方法的好处。
 *
 * <p>Normally a BeanFactory will load bean definitions stored in a configuration
 * source (such as an XML document), and use the org.springframework.beans package
 * to configure the beans. However, an implementation could simply return Java
 * objects it creates as necessary directly in Java code. There are no constraints
 * on how the definitions could be stored: LDAP, RDBMS, XML, properties file etc.
 * Implementations are encouraged to support references amongst beans, to either
 * Singletons or Prototypes.
 * 通常，BeanFactory将加载存储在配置源（如XML文档）中的bean定义，并使用org.springframework.beans包来配置bean。
 * 然而，实现可以简单地返回它在必要时直接在Java代码中创建的Java对象。
 * 定义的存储方式没有任何限制：LDAP、RDBMS、XML、属性文件等。鼓励实现支持Bean之间的引用，无论是对Singleton还是Prototype。
 *
 * @author Rod Johnson
 * @since 13 April 2001
 * @version $Revision: 1.1.1.1 $
 */
public interface BeanFactory {

	/**
	 * Return an instance (possibly shared or independent) of the given bean name.
	 * This method allows a bean factory to be used as a replacement for
	 * the Singleton or Prototype design pattern.
	 * 返回给定bean名称的实例（可能是共享的或独立的）。
	 * 这种方法允许使用bean工厂作为Singleton或Prototype设计模式的替代品。
	 *
	 * <p>Note that callers should retain references to returned objects. There is
	 * no guarantee that this method will be implemented to be efficient. For example,
	 * it may be synchronized, or may need to run an RDBMS query.
	 * 请注意，调用方应保留对返回对象的引用。不能保证这种方法将被有效地实施。
	 * 例如，它可能是同步的，或者可能需要运行RDBMS查询。
	 * @param name name of the bean to return
	 * @return the instance of the bean
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the given bean name.
	 * Provides a measure of type safety by throwing an exception if the bean is not
	 * of the required type.
	 * This method allows a bean factory to be used as a replacement for
	 * the Singleton or Prototype design pattern.
	 * 返回给定bean名称的实例（可能是共享的或独立的）。如果bean不是所需的类型，则通过抛出异常来提供类型安全性度量。
	 *
	 * 这种方法允许使用bean工厂作为Singleton或Prototype设计模式的替代品。
	 * <p>Note that callers should retain references to returned objects. There is
	 * no guarantee that this method will be implemented to be efficient. For example,
	 * it may be synchronized, or may need to run an RDBMS query.
	 * 请注意，调用方应保留对返回对象的引用。不能保证这种方法将被有效地实施。
	 * 例如，它可能是同步的，或者可能需要运行RDBMS查询。
	 * @param name name of the bean to return
	 * @param requiredType type the bean may match. Can be an interface or superclass
	 * of the actual class. For example, if the value is Object.class, this method will
	 * succeed whatever the class of the returned instance.
	 * @return the instance of the bean
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
	Object getBean(String name, Class requiredType) throws BeansException;

	/**
	 * Is this bean a singleton? That is, will getBean() always return the same object?
	 * 这个bean是singleton吗？也就是说，getBean（）是否总是返回相同的对象？
	 * @param name name of the bean to query
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @return is this bean a singleton
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Return the aliases for the given bean name, if defined.
	 * 如果已定义，则返回给定bean名称的别名。
	 * @param name the bean name to check for aliases
	 * @return the aliases, or an empty array if none
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
	String[] getAliases(String name) throws NoSuchBeanDefinitionException;

}
