/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanFactoryUtils;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventMulticaster;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ContextOptions;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NestingMessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.StringUtils;


/**
 * Partial implementation of ApplicationContext. Doesn't mandate the
 * type of storage used for configuration, but implements common functionality.
 *
 * <p>This class uses the Template Method design pattern, requiring
 * concrete subclasses to implement protected abstract methods.
 * 这个类用到了模版方法设计模式，需要子类实现抽象方法
 *
 * <p>The context options may be supplied as a bean in the default bean factory,
 * with the name "contextOptions".
 *
 * <p>A message source may be supplied as a bean in the default bean factory,
 * with the name "messageSource". Else, message resolution is delegated to the
 * parent context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 21, 2001
 * @version $Revision: 1.3 $
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see #OPTIONS_BEAN_NAME
 * @see #MESSAGE_SOURCE_BEAN_NAME
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

	/**
	 * Name of options bean in the factory.
	 * If none is supplied, a default ContextOptions instance will be used.
	 * @see ContextOptions
	 */
	public static final String OPTIONS_BEAN_NAME = "contextOptions";

	/**
	 * Name of the MessageSource bean in the factory.
	 * If none is supplied, message resolution is delegated to the parent.
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Log4j logger used by this class. Available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent context */
	private ApplicationContext parent;

	/** Display name */
	private String displayName = getClass().getName() + ";hashCode=" + hashCode();

	/** System time in milliseconds when this context started */
	private long startupTime;

	/** Special bean to handle configuration */
	private ContextOptions contextOptions;

	/** MessageSource helper we delegate our implementation of this interface to */
	private MessageSource messageSource;

	/**
	 * Helper class used in event publishing.
	 * TODO: This could be parameterized as a JavaBean (with a distinguished name
	 * specified), enabling a different thread usage policy for event publication.
	 * 事件发布中使用的帮助程序类。
	 *
	 * TODO:这可以参数化为JavaBean（指定了可分辨名称），从而为事件发布启用不同的线程使用策略。
	 */
	private ApplicationEventMulticaster eventMulticaster = new ApplicationEventMulticasterImpl();

	/**
	 * Set of ApplicationContextAware objects that have already received the context
	 * reference, to be able to avoid double initialization of managed objects.
	 * 已接收上下文引用的ApplicationContextAware对象集，以避免托管对象的双重初始化。
	 */
	private Set managedSingletons = new HashSet();

	/** Map of shared objects, keyed by String */
	private Map sharedObjects = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent parent context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this.parent = parent;
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext
	//---------------------------------------------------------------------

	/**
	 * Subclasses may call this to set parent after constructor.
	 * Note that parent shouldn't be changed: it should only be
	 * set later if it isn't available when an object of this
	 * class is created.
	 * @param ac parent context
	 */
	protected void setParent(ApplicationContext ac) {
		this.parent = ac;
	}

	/**
	 * Return the parent context, or null if there is no parent,
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or null if there is no parent
	 */
	public ApplicationContext getParent() {
		return parent;
	}

	/**
	 * To avoid endless constructor chaining, only concrete classes
	 * take this in their constructor, and then invoke this method
	 */
	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Return a friendly name for context
	 * @return a display name for the context
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Return the timestamp when this context was first loaded
	 * @return the timestamp (ms) when this context was first loaded
	 */
	public final long getStartupDate() {
		return startupTime;
	}

	/**
	 * Return context options. These control reloading etc.
	 * @return context options
	 */
	public final ContextOptions getOptions() {
		return this.contextOptions;
	}

	/**
	 * Load or reload configuration. 加载或者重新加载配置信息
	 * @throws ApplicationContextException if the configuration was invalid or couldn't
	 * be found, or if configuration has already been loaded and reloading is forbidden
	 * @throws BeansException if the bean factory could not be initialized
	 */
	public final void refresh() throws ApplicationContextException, BeansException {
		// 是够可以重新加载， 是在loadOptions中给初始化的，也就是可以重复进入的该初始化方法的。
		if (this.contextOptions != null && !this.contextOptions.isReloadable())
			throw new ApplicationContextException("Forbidden to reload config");

		this.startupTime = System.currentTimeMillis();

		// 刷新bean工厂，实际加载配置文件
		refreshBeanFactory();

		if (getBeanDefinitionCount() == 0)
			logger.warn("No beans defined in ApplicationContext [" + getDisplayName() + "]");
		else
			logger.info(getBeanDefinitionCount() + " beans defined in ApplicationContext [" + getDisplayName() + "]");

		// 调用配置信息来覆盖/修改bean的definitions定义信息
		invokeContextConfigurers();

		// load options bean for this context 加载bean是否可重新加载
		// 初始化 contextOptions 参数
		loadOptions();

		// initialize message source for this context
		// 为容器初始化事件源, 好像是为日志做的准备 更多是在web mvc中用的
		// messagekey Local + "." + code
		initMessageSource();

		// initialize other special beans in specific context subclasses
		// 初始化其他特殊的beans,在特殊的上下文子类中，
		// 高版本整合tomcat。
		onRefresh();

		// 校验监听器对象并注册他们，
		// 放到了一个容器里面。遍历容器，挨个执行
		// 观察者模式
		refreshListeners();

		// 这么晚才实例化singleton，以允许它们访问消息源
		preInstantiateSingletons();

		// last step: publish respective event 发布消息
		publishEvent(new ContextRefreshedEvent(this));
	}

	/**
	 * Callback method which can be overridden to add context-specific refresh work.
	 * 是一个回调的方法， 高版本使用这个整合的tomcat
	 * @throws ApplicationContextException in case of errors during refresh
	 */
	protected void onRefresh() throws ApplicationContextException {
		// For subclasses: do nothing by default.
		// 是给子类实现的
	}

	/**
	 * 实例化并调用所有注册的BeanFactoryPostProcessor bean。
	 * 必须在单例实例化之前调用。
	 */
	private void invokeContextConfigurers() {
		// 获取所有实现了BeanFactoryPostProcessor这个接口的定义信息
		String[] beanNames = getBeanDefinitionNames(BeanFactoryPostProcessor.class);
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			// 创建了bean,初始化了bean
			BeanFactoryPostProcessor configurer = (BeanFactoryPostProcessor) getBean(beanName);
			configurer.postProcessBeanFactory(getBeanFactory());
		}
	}

	/**
	 * 加载options
	 * beanFactory 必须在这个方法被调用前加载
	 */
	private void loadOptions() {
		try {
			this.contextOptions = (ContextOptions) getBean(OPTIONS_BEAN_NAME);
		} catch (NoSuchBeanDefinitionException ex) {
			// 异常处理后交付默认值
			logger.info("No options bean (\"" + OPTIONS_BEAN_NAME + "\") found: using default");
			this.contextOptions = new ContextOptions();
		}
	}

	/**
	 * Initialize the message source.
	 * 初始化消息源
	 * Use parent's if none defined in this context.
	 * 如果在当前上下文没有定义，就使用父类的
	 */
	private void initMessageSource() {
		try {
			this.messageSource = (MessageSource) getBean(MESSAGE_SOURCE_BEAN_NAME);
			// set parent message source if applicable,
			// and if the message source is defined in this context, not in a parent
			if (this.parent != null && (this.messageSource instanceof NestingMessageSource) &&
			    Arrays.asList(getBeanDefinitionNames()).contains(MESSAGE_SOURCE_BEAN_NAME)) {
				((NestingMessageSource) this.messageSource).setParent(this.parent);
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// 异常后赋默认值
			logger.info("No MessageSource found for [" + getDisplayName() + "]: using empty StaticMessageSource");
			// use empty message source to be able to accept getMessage calls
			this.messageSource = new StaticMessageSource();
		}
	}

	/**
	 * Invoke the setApplicationContext() callback on all objects
	 * in the context. This involves instantiating the objects.
	 * Only singletons will be instantiated eagerly.
	 * 对上下文中的所有对象调用setApplicationContext（）回调。
	 * 这涉及到实例化对象。
	 * 只有singleton才会被急切地实例化。
	 */
	private void preInstantiateSingletons() {
//		logger.info("Configuring singleton beans in context");
		// 从beanDefinitionMap中获取所有beanName的集合
		String[] beanNames = getBeanDefinitionNames();
//		logger.debug("Found " + beanNames.length + " listeners in bean factory: names=[" +
//		             StringUtils.arrayToDelimitedString(beanNames, ",") + "]");
		// 遍历beansNames
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			// 他的bean定义信息是不是单例的
			if (isSingleton(beanName)) {
				getBean(beanName);
			}
		}
	}

	/**
	 * If the object is context-aware, give it a reference to this object.
	 * Does not reinitialize singletons that have already received the context.
	 * @param bean object to invoke the setApplicationContext() method on,
	 * if it implements the ApplicationContextAware interface
	 */
	private void configureManagedObject(String name, Object bean) {
		// ApplicationContextAware 方法的处理
		if (bean instanceof ApplicationContextAware &&
				(!isSingleton(name) || !this.managedSingletons.contains(bean))) {
			logger.debug("Setting application context on ApplicationContextAware object [" + bean + "]");
			ApplicationContextAware aca = (ApplicationContextAware) bean;
			aca.setApplicationContext(this);
			this.managedSingletons.add(bean);
		}
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 * 添加实现ApplicationListener作为侦听器的bean。
	 * 不会影响其他侦听器，这些侦听器可以在不使用bean的情况下添加
	 */
	private void refreshListeners() {
		logger.info("Refreshing listeners");
		//所有监听器类型的集合
		List listeners = BeanFactoryUtils.beansOfType(ApplicationListener.class, this);
		logger.debug("Found " + listeners.size() + " listeners in bean factory");
		for (int i = 0; i < listeners.size(); i++) {
			ApplicationListener listener = (ApplicationListener) listeners.get(i);
			addListener(listener);
			logger.info("Bean listener added: [" + listener + "]");
		}
	}

	/**
	 * Destroy the singletons in the bean factory of this application context.
	 * 销毁在上下文中单例对象
	 */
	public void close() {
		logger.info("Closing application context [" + getDisplayName() + "]");

		// destroy all cached singletons in this context,
		// invoking DisposableBean.destroy and/or "destroy-method"
		getBeanFactory().destroySingletons();

		// publish respective event
		publishEvent(new ContextClosedEvent(this));
	}

	/**
	 * Publish the given event to all listeners.
	 * 给所有的监听器发布消息
	 * <p>Note: Listeners get initialized after the message source, to be able
	 * to access it within listener implementations. Thus, message source
	 * implementation cannot publish events.
	 * @param event event to publish. The event may be application-specific,
	 * or a standard framework event.
	 */
	public final void publishEvent(ApplicationEvent event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event in context [" + getDisplayName() + "]: " + event.toString());
		}
		this.eventMulticaster.onApplicationEvent(event);
		if (this.parent != null) {
			parent.publishEvent(event);
		}
	}

	/**
	 * Add a listener. Any beans that are listeners are automatically added.
	 * 添加一个侦听器。将自动添加作为侦听器的任何bean。
	 */
	protected void addListener(ApplicationListener l) {
		this.eventMulticaster.addApplicationListener(l);
	}

	/**
	 * This implementation supports fully qualified URLs and appropriate
	 * (file) paths, via getResourceByPath.
	 * 支持通过完整的url中获取，或者从恰当的路径
	 * Throws a FileNotFoundException if getResourceByPath returns null.
	 * @see #getResourceByPath
	 */
	public final InputStream getResourceAsStream(String location) throws IOException {
		try {
			// try URL，
			URL url = new URL(location);
			logger.debug("Opening as URL: " + location);
			return url.openStream();
		} catch (MalformedURLException ex) {
			// no URL -> try (file) path
			// 没有url。尝试从文件输入流
			InputStream in = getResourceByPath(location);
			if (in == null) {
				throw new FileNotFoundException("Location '" + location + "' isn't a URL and cannot be interpreted as (file) path");
			}
			return in;
		}
	}

	/**
	 * Return input stream to the resource at the given (file) path.
	 * <p>Default implementation supports file paths, either absolute or
	 * relative to the application's working directory. This should be
	 * appropriate for standalone implementations but can be overridden,
	 * e.g. for implementations targetted at a container.
	 * @param path path to the resource
	 * @return InputStream for the specified resource, can be null if
	 * not found (instead of throwing an exception)
	 * @throws IOException exception when opening the specified resource
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		return new FileInputStream(path);
	}

	/**
	 * This implementation returns the working directory of the Java VM.
	 * This should be appropriate for standalone implementations but can
	 * be overridden for implementations targetted at a container.
	 */
	public String getResourceBasePath() {
		return (new File("")).getAbsolutePath() + File.separatorChar;
	}

	public synchronized Object sharedObject(String key) {
		Object sharedObject = this.sharedObjects.get(key);
		if (sharedObject == null && getParent() != null) {
			return getParent().sharedObject(key);
		}
		else {
			return sharedObject;
		}
	}

	public synchronized void shareObject(String key, Object o) {
		logger.info("Set shared object '" + key + "'");
		this.sharedObjects.put(key, o);
	}

	public synchronized Object removeSharedObject(String key) {
		logger.info("Removing shared object '" + key + "'");
		Object o = this.sharedObjects.remove(key);
		if (o == null) {
			logger.warn("Shared object '" + key + "' not present; could not be removed");
		} else {
			logger.info("Removed shared object '" + key + "'");
		}
		return o;
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource
	//---------------------------------------------------------------------

	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return this.messageSource.getMessage(code, args, defaultMessage, locale);
	}

	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, args, locale);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(resolvable, locale);
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory bean工厂的实现
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		// 创建了bean
		Object bean = getBeanFactory().getBean(name);
		configureManagedObject(name, bean);
		return bean;
	}

	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBeanFactory().getBean(name, requiredType);
		configureManagedObject(name, bean);
		return bean;
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getAliases(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public String[] getBeanDefinitionNames(Class type) {
		return getBeanFactory().getBeanDefinitionNames(type);
	}

	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	/** Show information about this context */
	public String toString() {
		StringBuffer sb = new StringBuffer("ApplicationContext: displayName=[" + this.displayName + "]; ");
		sb.append("class=[" + getClass().getName() + "]; ");
		sb.append("BeanFactory={" + getBeanFactory() + "}; ");
		sb.append("} MessageSource={" + this.messageSource + "}; ");
		sb.append("ContextOptions={" + this.contextOptions + "}; ");
		sb.append("Startup date=" + new Date(this.startupTime) + "; ");
		if (this.parent == null)
			sb.append("Root of ApplicationContext hierarchy");
		else
			sb.append("Parent={" + this.parent + "}");
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by refresh before any other initialization work.
	 * 子类必须实现该方法去实际加载配置信息
	 * 这个方法需要在refresh方法的任务初始化工作之前完成
	 * @see #refresh
	 */
	protected abstract void refreshBeanFactory() throws ApplicationContextException, BeansException;

	/**
	 * Subclasses must return their internal bean factory here.
	 * They should implement the lookup efficiently, so that it can be called
	 * repeatedly without a performance penalty.
	 * @return this application context's internal bean factory
	 */
	protected abstract ListableBeanFactoryImpl getBeanFactory();

}
