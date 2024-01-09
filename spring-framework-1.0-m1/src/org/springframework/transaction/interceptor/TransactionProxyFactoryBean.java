package org.springframework.transaction.interceptor;

import java.util.Iterator;
import java.util.Properties;

import org.springframework.aop.framework.AbstractMethodPointcut;
import org.springframework.aop.framework.AlwaysInvoked;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultProxyConfig;
import org.springframework.aop.framework.InvokerInterceptor;
import org.springframework.aop.framework.MethodPointcut;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Proxy factory bean for simplified declarative transaction handling.
 * Alternative to the standard AOP ProxyFactoryBean with a TransactionInterceptor.
 *
 * <p>This class is intended to cover the <i>typical</i> case of declarative
 * transaction demarcation: wrapping a target class with a transactional proxy,
 * without any other interceptors. Internally, an own TransactionInterceptor
 * instance is used, but the user of this class does not have to care.
 * 
 * <p>Optionaly MethodPointcut property could be set to cause a conditional
 * invokation of the underlying TransactionInterceptor. AlwaysInvoked MethodPointcut
 * is the default pointcut wrapping TransactionInterceptor
 *
 * <p>In contrast to TransactionInterceptor, the transaction attributes are
 * specified as properties, with method names as keys and transaction attribute
 * descriptors as values. Method names are always applied to the target class.
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @since 21.08.2003
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see TransactionInterceptor
 * @see #setTransactionAttributes
 * @see MethodPointcut
 * @version $Id: TransactionProxyFactoryBean.java,v 1.3 2003/08/22 15:32:28 dkopylenko Exp $
 */
public class TransactionProxyFactoryBean extends DefaultProxyConfig implements FactoryBean, InitializingBean {

	private PlatformTransactionManager transactionManager;

	private Object target;

	private boolean proxyInterfacesOnly = true;

	private Properties transactionAttributes;

	private Object proxy;

	private MethodPointcut methodPointcut;

	/**
	 * Set the transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the target object, i.e. the bean to be wrapped with a
	 * transactional proxy.
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * Set MethodPointcut, i.e the bean that can cause
 	 * conditional invocation of this TransactionInterceptor depending on
 	 * the method, and attributes passed. The default MethodPointcut is {@link AlwaysInvoked}
	 */
	public void setMethodPointcut(MethodPointcut pointcut) {
		this.methodPointcut = pointcut;
	}

	/**
	 * Set if the proxy should only implement the interfaces of the target.
	 * If this is false, a dynamic runtime subclass of the target will be
	 * created via CGLIB, castable to the target class.
	 * Default is true.
	 */
	public void setProxyInterfacesOnly(boolean proxyInterfacesOnly) {
		this.proxyInterfacesOnly = proxyInterfacesOnly;
	}

	/**
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * @see TransactionAttributeEditor
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		this.transactionAttributes = transactionAttributes;
	}

	public void afterPropertiesSet() throws AopConfigException {
		if (this.target == null) {
			throw new AopConfigException("Target must be set");
		}

		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		TransactionAttributeEditor tae = new TransactionAttributeEditor();
		for (Iterator it = transactionAttributes.keySet().iterator(); it.hasNext();) {
			String methodName = (String)it.next();
			String value = transactionAttributes.getProperty(methodName);
			tae.setAsText(value);
			TransactionAttribute attr = (TransactionAttribute)tae.getValue();
			tas.addTransactionalMethod(methodName, attr);
		}

		TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
		transactionInterceptor.setTransactionManager(this.transactionManager);
		transactionInterceptor.setTransactionAttributeSource(tas);
		transactionInterceptor.afterPropertiesSet();
		
		if (this.methodPointcut == null)
			this.methodPointcut = new AlwaysInvoked(transactionInterceptor);
		else
			((AbstractMethodPointcut)this.methodPointcut).setInterceptor(transactionInterceptor);
		
		addMethodPointcut(this.methodPointcut);
		addInterceptor(new InvokerInterceptor(this.target));
		if (this.proxyInterfacesOnly) {
			setInterfaces(BeanUtils.getAllInterfaces(this.target));
		}

		AopProxy proxy = new AopProxy(this);
		this.proxy = proxy.getProxy();
	}

	public Object getObject() {
		return this.proxy;
	}

	public boolean isSingleton() {
		return true;
	}

	public PropertyValues getPropertyValues() {
		return null;
	}

}
