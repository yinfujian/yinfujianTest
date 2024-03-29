/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Abstract superclass that makes implementing a BeanFactory very easy.
 *
 * <p>This class uses the <b>Template Method</b> design pattern.
 * Subclasses must implement only the
 * <code>
 * getBeanDefinition(name)
 * </code>
 * method.
 *
 * <p>This class handles resolution of runtime bean references,
 * FactoryBean dereferencing, and management of collection properties.
 * It also allows for management of a bean factory hierarchy, 
 * implementing the HierarchicalBeanFactory interface.
 *
 * @author Rod Johnson
 * @since 15 April 2001
 * @version $Id: AbstractBeanFactory.java,v 1.1.1.1 2003/08/14 16:20:19 trisberg Exp $
 */
public abstract class AbstractBeanFactory implements HierarchicalBeanFactory {

	/**
	 * Used to dereference a FactoryBean and distinguish it from
	 * beans <i>created</i> by the factory. For example,
	 * if the bean named <code>myEjb</code> is a factory, getting
	 * <code>&myEjb</code> will return the factory, not the instance
	 * returned by the factory.
	 * 用于取消引用FactoryBean，并将其与工厂</i>创建的bean区分开来。
	 * 例如，如果名为<code>myEjb</code>的bean是一个工厂，那么获取<code>&myEjb</code>将返回工厂，而不是工厂返回的实例。
	 */
	public static final String FACTORY_BEAN_PREFIX = "&";


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent bean factory, for bean inheritance support */
	private BeanFactory parentBeanFactory;

	/** Cache of singletons: bean name --> bean instance */
	private Map singletonCache = new HashMap();

	/** Map from alias to canonical bean name */
	private Map aliasMap = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new AbstractBeanFactory.
	 */
	public AbstractBeanFactory() {
	}

	/**
	 * Create a new AbstractBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 * @see #getBean
	 */
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}

	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	/**
	 * Return the bean name, stripping out the factory deference prefix if necessary,
	 * and resolving aliases to canonical names.
	 * 返回bean名称，必要时去掉工厂尊重前缀，并将别名解析为规范名称。
	 */
	private String transformedBeanName(String name) {
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			name = name.substring(FACTORY_BEAN_PREFIX.length());
		}
		// Handle aliasing
		// 优先别名
		String canonicalName = (String) this.aliasMap.get(name);
		return canonicalName != null ? canonicalName : name;
	}

	/**
	 * Return whether this name is a factory dereference (beginning
	 * with the factory dereference prefix)
	 */
	private boolean isFactoryDereference(String name) {
		return name.startsWith(FACTORY_BEAN_PREFIX);
	}

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * 返回具有给定名称的bean，如果没有找到，则检查父bean工厂。
	 * @param name name of the bean to retrieve
	 */
	public final Object getBean(String name) {
		return getBeanInternal(name, null);
	}

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * 返回具有给定名称的bean，如果没有找到，则检查父bean工厂。
	 * @param name name of the bean to retrieve 要检索的bean的名称
	 * @param newlyCreatedBeans cache with newly created beans (name, instance) 使用新创建的bean（名称、实例）缓存
	 * if triggered by the creation of another bean, or null else
	 * (necessary to resolve circular references)
	 * 如果由另一个bean的创建触发，或者为null else（解析循环引用所必需）
	 */
	private Object getBeanInternal(String name, Map newlyCreatedBeans) {
		if (name == null)
			throw new NoSuchBeanDefinitionException(null, "Cannot get bean with null name");
		try {
			// 通过beanName获取AbstractBeanDefinition
			AbstractBeanDefinition bd = getBeanDefinition(transformedBeanName(name));
			if (bd.isSingleton()) {
				// Check for bean instance created in the current call,
				// to be able to resolve circular references
				// 检查在当前调用中创建的bean实例，以便能够解析循环引用 这个存的是半成品对象，解决循环依赖
				if (newlyCreatedBeans != null && newlyCreatedBeans.containsKey(name)) {
					return newlyCreatedBeans.get(name);
				}
				// 可能从缓存中已经创建的对象获取
				return getSharedInstance(name, newlyCreatedBeans);
			}
			else {
				return createBean(name, newlyCreatedBeans);
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null)
				return this.parentBeanFactory.getBean(name);
			throw ex;
		}
	}

	public final Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);
		Class clazz = bean.getClass();
		if (!requiredType.isAssignableFrom(clazz)) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
		}
		return bean;
	}

	public boolean isSingleton(String pname) throws NoSuchBeanDefinitionException {
		String name = transformedBeanName(pname);
		try {
			return getBeanDefinition(name).isSingleton();
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Not found -> check parent
			if (this.parentBeanFactory != null)
				return this.parentBeanFactory.isSingleton(name);
			throw ex;
		}
	}

	public final String[] getAliases(String pname) {
		String name = transformedBeanName(pname);
		List aliases = new ArrayList();
		for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			if (entry.getValue().equals(name)) {
				aliases.add(entry.getKey());
			}
		}
		return (String[]) aliases.toArray(new String[aliases.size()]);
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Get a singleton instance of this bean name. Note that this method shouldn't
	 * be called too often: Callers should keep hold of instances. Hence, the whole
	 * method is synchronized here.
	 * 获取此bean名称的一个singleton实例。请注意，不应经常调用此方法：调用方应保留实例。因此，整个方法在这里是同步的。
	 * TODO: There probably isn't any need for this to be synchronized, at least not if we pre-instantiate singletons.
	 * TODO:这可能没有任何同步的必要，至少如果我们预先实例化singleton的话就没有了。
	 * @param pname name that may include factory dereference prefix 可能包括工厂取消引用前缀的名称
	 * @param newlyCreatedBeans cache with newly created beans (name, instance) 使用新创建的bean（名称、实例）缓存
	 * if triggered by the creation of another bean, or null else
	 * (necessary to resolve circular references)
	 *  如果由另一个bean的创建触发，或者为null else（解析循环引用所必需）
	 */
	private final synchronized Object getSharedInstance(String pname, Map newlyCreatedBeans) throws BeansException {
		// Get rid of the dereference prefix if there is one
		// 获取标准beanName
		String name = transformedBeanName(pname);

		// bean 缓存 singletonCache 中获取单例bean
		Object beanInstance = this.singletonCache.get(name);
		if (beanInstance == null) {
			logger.info("Creating shared instance of singleton bean '" + name + "'");
			// 没有的话建一个bean
			beanInstance = createBean(name, newlyCreatedBeans);
			// 放进缓存里
			this.singletonCache.put(name, beanInstance);
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("Returning cached instance of singleton bean '" + name + "'");
		}

		// Don't let calling code try to dereference the
		// bean factory if the bean isn't a factory
		// 如果bean不是工厂，不要让调用代码试图取消引用bean工厂
		// 也就是说以&开头对象名，必须是FactoryBean， 不是的会抛出异常
		if (isFactoryDereference(pname) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(name, beanInstance);
		}

		// Now we have the beanInstance, which may be a normal bean
		// or a FactoryBean. If it's a FactoryBean, we use it to
		// create a bean instance, unless the caller actually wants
		// a reference to the factory.
		// 现在我们有了beanInstance，它可能是一个普通的bean或FactoryBean。
		// 如果它是FactoryBean，我们将使用它来创建一个bean实例，除非调用者实际上想要对工厂的引用。
		if (beanInstance instanceof FactoryBean) {
			if (!isFactoryDereference(pname)) {
				// Configure and return new bean instance from factory
				// 从工厂里拿一个新的bean出来
				FactoryBean factory = (FactoryBean) beanInstance;
				logger.debug("Bean with name '" + name + "' is a factory bean");
				beanInstance = factory.getObject();

				// Set pass-through properties 传递的参数
				if (factory.getPropertyValues() != null) {
					logger.debug("Applying pass-through properties to bean with name '" + name + "'");
					new BeanWrapperImpl(beanInstance).setPropertyValues(factory.getPropertyValues());
				}
				// Initialization is really up to factory
				//invokeInitializerIfNecessary(beanInstance);
			}
			else {
				// The user wants the factory itself
				logger.debug("Calling code asked for BeanFactory instance for name '" + name + "'");
			}
		}	// if we're dealing with a factory bean

		return beanInstance;
	}

	/**
	 * All the other methods in this class invoke this method
	 * although beans may be cached after being instantiated by this method.
	 * All bean instantiation within this class is performed by this method.
	 * Return a BeanWrapper object for a new instance of this bean.
	 * First look up BeanDefinition for the given bean name.
	 * Uses recursion to support instance "inheritance".
	 * 这个类中的所有其他方法都调用这个方法，尽管bean在被这个方法实例化后可能会被缓存。
	 * 这个类中的所有bean实例化都是由这个方法执行的。
	 * 为这个bean的一个新实例返回一个BeanWrapper对象。
	 * 首先查找给定bean名称的BeanDefinition。
	 * 使用递归来支持实例“继承”。
	 * @param name name of the bean. Must be unique in the BeanFactory
	 * @param newlyCreatedBeans cache with newly created beans (name, instance)
	 * if triggered by the creation of another bean, or null else
	 * (necessary to resolve circular references)
	 * @return a new instance of this bean
	 */
	private Object createBean(String name, Map newlyCreatedBeans) throws BeansException {
		RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(name); // 获取BeanDefinition
		logger.debug("Creating instance of bean '" + name + "' with merged definition [" + mergedBeanDefinition + "]");
		BeanWrapper instanceWrapper = new BeanWrapperImpl(mergedBeanDefinition.getBeanClass()); // 创建对象并设置给BeanWrapperImpl的object
		Object bean = instanceWrapper.getWrappedInstance(); // 获取该对象

		// Cache new instance to be able resolve circular references, but ignore
		// FactoryBean instances as they can't create objects if not initialized
		// 缓存新实例以能够解析循环引用，但忽略FactoryBean实例，因为它们在未初始化的情况下无法创建对象
		if (!(bean instanceof FactoryBean)) {
			if (newlyCreatedBeans == null) {
				newlyCreatedBeans = new HashMap();
			}
			newlyCreatedBeans.put(name, bean);
		}

		PropertyValues pvs = mergedBeanDefinition.getPropertyValues(); // 配置的属性值
		applyPropertyValues(instanceWrapper, pvs, name, newlyCreatedBeans); // 通过反射method设置配置值
		callLifecycleMethodsIfNecessary(bean, name, mergedBeanDefinition, instanceWrapper); // 生命周期方法，InitializingBean getInitMethodName BeanFactoryAware
		return bean;
	}

	/**
	 * Apply the given property values, resolving any runtime references
	 * to other beans in this bean factory.
	 * Must use deep copy, so we don't permanently modify this property
	 * @param bw BeanWrapper wrapping the target object
	 * @param pvs new property values
	 * @param name bean name passed for better exception information
	 * @param newlyCreatedBeans cache with newly created beans (name, instance)
	 * if triggered by the creation of another bean, or null else
	 * (necessary to resolve circular references)
	 */
	private void applyPropertyValues(BeanWrapper bw, PropertyValues pvs, String name, Map newlyCreatedBeans) throws BeansException {
		if (pvs == null)
			return;

		MutablePropertyValues deepCopy = new MutablePropertyValues(pvs);
		PropertyValue[] pvals = deepCopy.getPropertyValues();
		
		for (int i = 0; i < pvals.length; i++) {
			PropertyValue pv = new PropertyValue(pvals[i].getName(), resolveValueIfNecessary(bw, newlyCreatedBeans, pvals[i]));
			// Update mutable copy
			deepCopy.setPropertyValueAt(pv, i);
		}
		
		// Set our (possibly massaged) deepCopy
		try {
			bw.setPropertyValues(deepCopy);
		}
		catch (FatalBeanException ex) {
			// Improve the message by showing the context
			throw new FatalBeanException("Error setting property on bean [" + name + "]", ex);
		}
	}

	/**
	 * Given a PropertyValue, return a value, resolving any references to other
	 * beans in the factory if necessary. The value could be:
	 * <li>An ordinary object or null, in which case it's left alone
	 * <li>A RuntimeBeanReference, which must be resolved
	 * <li>A ManagedList. This is a special collection that may contain
	 * RuntimeBeanReferences that will need to be resolved.
	 * <li>A ManagedMap. In this case the value may be a reference that
	 * must be resolved.
	 * If the value is a simple object, but the property takes a Collection type,
	 * the value must be placed in a list.
	 */
	private Object resolveValueIfNecessary(BeanWrapper bw, Map newlyCreatedBeans, PropertyValue pv)
	    throws BeansException {
		Object val;
		
		// Now we must check each PropertyValue to see whether it
		// requires a runtime reference to another bean to be resolved.
		// If it does, we'll attempt to instantiate the bean and set the reference.
		if (pv.getValue() != null && (pv.getValue() instanceof RuntimeBeanReference)) {
			// 注入属性的时候，如果，注入的是bean类型，会去取引用的bean，如果引用的bean还没有对象，那么要createBean
			// 这里可能出现循环依赖
			RuntimeBeanReference ref = (RuntimeBeanReference) pv.getValue();
			val = resolveReference(pv.getName(), ref, newlyCreatedBeans);
		}	
		else if (pv.getValue() != null && (pv.getValue() instanceof ManagedList)) {
			// Convert from managed list. This is a special container that
			// may contain runtime bean references.
			// May need to resolve references
			val = resolveManagedList(pv.getName(), (ManagedList) pv.getValue(), newlyCreatedBeans);
		}
		else if (pv.getValue() != null && (pv.getValue() instanceof ManagedMap)) {
			// Convert from managed map. This is a special container that
			// may contain runtime bean references as values.
			// May need to resolve references
			ManagedMap mm = (ManagedMap) pv.getValue();
			val = resolveManagedMap(pv.getName(), mm, newlyCreatedBeans);	
		}
		else {
			// It's an ordinary property. Just copy it.
			val = pv.getValue();
		}
		
		 // If it's an array type, we may have to massage type
		 // of collection. We'll start with ManagedList.
		 // We may also have to convert array elements from Strings
		 // TODO consider refactoring into BeanWrapperImpl? 考虑重构到BeanWrapperImpl？
		 if (val != null && val instanceof ManagedList && bw.getPropertyDescriptor(pv.getName()).getPropertyType().isArray()) {
			 // It's an array
			 Class arrayClass = bw.getPropertyDescriptor(pv.getName()).getPropertyType();
			 Class componentType = arrayClass.getComponentType();
			 List l = (List) val;
		
			val = managedListToArray(bw, pv, val, componentType, l);
		 }
		
		return val;
	}
	
	/**
	 * Resolve a reference to another bean in the factory.
	 * @param name included for diagnostics
	 */
	private Object resolveReference(String name, RuntimeBeanReference ref, Map newlyCreatedBeans) {
		try {
			// Try to resolve bean reference
			logger.debug("Resolving reference from bean [" + name + "] to bean [" + ref.getBeanName() + "]");
			Object bean = getBeanInternal(ref.getBeanName(), newlyCreatedBeans);
			// Create a new PropertyValue object holding the bean reference
			return bean;
		}
		catch (BeansException ex) {
			throw new FatalBeanException("Can't resolve reference to bean [" + ref.getBeanName() + "] while setting properties on bean [" + name + "]", ex);
		}
	}

	/**
	 * For each element in the ManagedMap, resolve references if necessary.
	 * Allow ManagedLists as map entries.
	 */
	private ManagedMap resolveManagedMap(String name, ManagedMap mm, Map newlyCreatedBeans) {
		Iterator keys = mm.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			Object value = mm.get(key);
			if (value instanceof RuntimeBeanReference) {
				mm.put(key, resolveReference(name, (RuntimeBeanReference) value, newlyCreatedBeans));
			}
			else if (value instanceof ManagedList) {
				// An entry may be a ManagedList, in which case we may need to
				// resolve references
				mm.put(key, resolveManagedList(name, (ManagedList) value, newlyCreatedBeans));
			}
		}	// for each key in the managed map
		return mm;
	}

	/**
	 * For each element in the ManagedList, resolve reference if necessary.
	 */
	private ManagedList resolveManagedList(String name, ManagedList l, Map newlyCreatedBeans) {
		for (int j = 0; j < l.size(); j++) {
			if (l.get(j) instanceof RuntimeBeanReference) {
				l.set(j, resolveReference(name, (RuntimeBeanReference) l.get(j), newlyCreatedBeans));
			}
		}
		return l;
	}
	
	private Object managedListToArray(BeanWrapper bw, PropertyValue pv, Object val, Class componentType, List l)
	    throws NegativeArraySizeException, BeansException, BeanDefinitionStoreException {
		try {
			Object[] arr = (Object[]) Array.newInstance(componentType, l.size());
			for (int i = 0; i < l.size(); i++) {
				// TODO hack: BWI cast
				Object newval = ((BeanWrapperImpl) bw).doTypeConversionIfNecessary(bw.getWrappedInstance(), pv.getName(), null, l.get(i), componentType);
				arr[i] = newval;
			}
			val = arr;
		}
		catch (ArrayStoreException ex) {
			throw new BeanDefinitionStoreException("Cannot convert array element from String to " + componentType, ex);
		}
		return val;
	}
	
	/**
	 * Give a bean a chance to react now all its properties are set,
	 * and a chance to know about its owning bean factory (this object).
	 * This means checking whether the bean implements InitializingBean
	 * 生命周期的方法回调
	 * and/or BeanFactoryAware, and invoking the necessary callback(s) if it does.
	 * @param bean new bean instance we may need to initialize
	 * @param name the bean has in the factory. Used for debug output.
	 */
	private void callLifecycleMethodsIfNecessary(Object bean, String name, RootBeanDefinition rbd, BeanWrapper bw)
	    throws BeansException {

		// 是否实现了InitializingBean 接口，是的话，在创建完bean的时候会回调
		if (bean instanceof InitializingBean) {
			logger.debug("Calling afterPropertiesSet() on bean with name '" + name + "'");
			try {
				((InitializingBean) bean).afterPropertiesSet();
			}
			catch (Exception ex) {
				throw new FatalBeanException("afterPropertiesSet() on bean with name '" + name + "' threw an exception", ex);
			}
		}

		// 初始化方法
		if (rbd.getInitMethodName() != null) {
			logger.debug("Calling custom init method '" + rbd.getInitMethodName() + "' on bean with name '" + name + "'");
			bw.invoke(rbd.getInitMethodName(), null);
			// Can throw MethodInvocationException
		}

		// 工厂增强方法
		if (bean instanceof BeanFactoryAware) {
			logger.debug("Calling setBeanFactory() on BeanFactoryAware bean with name '" + name + "'");
			try {
				((BeanFactoryAware) bean).setBeanFactory(this);
			}
			catch (BeansException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new FatalBeanException("setBeanFactory() on bean with name '" + name + "' threw an exception", ex);
			}
		}
	}

	/**
	 * Make a RootBeanDefinition, even by traversing parent if the parameter is a child definition.
	 * 如果参数是子定义，即使通过遍历父定义也可以进行RootBeanDefinition。
	 * @return a merged RootBeanDefinition with overriden properties
	 */
	protected final RootBeanDefinition getMergedBeanDefinition(String name) throws NoSuchBeanDefinitionException {
		try {
			AbstractBeanDefinition bd = getBeanDefinition(name);
			if (bd instanceof RootBeanDefinition) {
				// Remember to take a deep copy
				return new RootBeanDefinition((RootBeanDefinition) bd);
			}
			else if (bd instanceof ChildBeanDefinition) {
				ChildBeanDefinition cbd = (ChildBeanDefinition) bd;
				// Deep copy
				RootBeanDefinition rbd = new RootBeanDefinition(getMergedBeanDefinition(cbd.getParentName()));
				// Override properties
				rbd.setPropertyValues(merge(rbd.getPropertyValues(), cbd.getPropertyValues()));
				return rbd;
			}			
		}
		catch (NoSuchBeanDefinitionException ex) {
			if (this.parentBeanFactory != null) {
				if (!(this.parentBeanFactory instanceof AbstractBeanFactory))
					throw new BeanDefinitionStoreException("Parent bean factory must be of type AbstractBeanFactory to support inheritance from a parent bean definition: " +
							"offending bean name is '" + name + "'", null);
				return ((AbstractBeanFactory) this.parentBeanFactory).getMergedBeanDefinition(name);
			}
			else {
				throw ex;
			}
		}
		throw new FatalBeanException("Shouldn't happen: BeanDefinition for '" + name + "' is neither a RootBeanDefinition or ChildBeanDefinition");
	}
	
	/**
	 * Incorporate changes from overrides param into pv base param.
	 */
	private PropertyValues merge(PropertyValues pv, PropertyValues overrides) {
		MutablePropertyValues parent = new MutablePropertyValues(pv);
		for (int i = 0; i < overrides.getPropertyValues().length; i++) {
			parent.addOrOverridePropertyValue(overrides.getPropertyValues()[i]);
		}
		return parent;
	}
	
	/**
	 * Given a bean name, create an alias. This must respect prototype/
	 * singleton behaviour. We typically use this method to support
	 * names that are illegal within XML ids (used for bean names).
	 * @param name name of the bean
	 * @param alias alias that will behave the same as the bean names
	 */
	public final void registerAlias(String name, String alias) {
		logger.debug("Creating alias '" + alias + "' for bean with name '" + name + "'");
		this.aliasMap.put(alias, name);
	}

	/**
	 * Destroy all cached singletons in this factory.
	 * To be called on shutdown of a factory.
	 */
	public final void destroySingletons() {
		logger.info("Destroying singletons in factory {" + this + "}");

		// 遍历单例对象
		for (Iterator it = this.singletonCache.keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			Object bean = this.singletonCache.get(name);
			RootBeanDefinition bd = getMergedBeanDefinition(name);

			if (bean instanceof DisposableBean) {
				logger.debug("Calling destroy() on bean with name '" + name + "'");
				try {
					((DisposableBean) bean).destroy();
				}
				catch (Exception ex) {
					logger.error("destroy() on bean with name '" + name + "' threw an exception", ex);
				}
			}

			if (bd.getDestroyMethodName() != null) {
				logger.debug("Calling custom destroy method '" + bd.getDestroyMethodName() + "' on bean with name '" + name + "'");
				BeanWrapper bw = new BeanWrapperImpl(bean);
				try {
					bw.invoke(bd.getDestroyMethodName(), null);
				}
				catch (MethodInvocationException ex) {
					logger.error(ex.getMessage(), ex.getRootCause());
				}
			}
		}
		
		this.singletonCache.clear();
	}


	//---------------------------------------------------------------------
	// Abstract method to be implemented by concrete subclasses
	//---------------------------------------------------------------------

	/**
	 * This method must be defined by concrete subclasses to implement the
	 * <b>Template Method</b> GoF design pattern.
	 * <br>Subclasses should normally implement caching, as this method is invoked
	 * by this class every time a bean is requested.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name. Must never return null.
	 * @throws NoSuchBeanDefinitionException if the bean definition cannot be resolved
	 */
	protected abstract AbstractBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

}
