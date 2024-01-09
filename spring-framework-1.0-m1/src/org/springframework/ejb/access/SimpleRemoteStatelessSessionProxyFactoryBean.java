/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.FactoryBean;

/**
 * Convenient factory for local or remote SLSB proxies.
 * If you want control over interceptor chaining, use an AOP ProxyFactoryBean
 * rather than rely on this class.
 * @author Rod Johnson
 * @since 09-May-2003
 * @version $Id: SimpleRemoteStatelessSessionProxyFactoryBean.java,v 1.1.1.1 2003/08/14 16:20:25 trisberg Exp $
 */
public class SimpleRemoteStatelessSessionProxyFactoryBean extends SimpleRemoteSlsbInvokerInterceptor
    implements FactoryBean {
	
	/*
	 * Instead of a separate subclass for each type of SLSBInvoker, we could have added
	 * this functionality to AbstractSlsbInvokerInterceptor. However, the avoiding of
	 * code duplication would be outweighed by the confusion this would produce over the
	 * purpose of AbstractSlsbInvokerInterceptor.
	 */
	
	/** EJBObject */
	private Object proxy;

	/**
	 * The business interface of the EJB we're proxying.
	 */
	private Class businessInterface;

	/**
	 * @return the business interface of the EJB. Note that this
	 * will normally be the superinterface of the EJB remote component interface.
	 * Using a business methods interface is a best practice
	 * when implementing EJBs.
	 */
	public Class getBusinessInterface() {
		return businessInterface;
	}

	/**
	 * Set the business interface of the EJB we're proxying
	 * @param class1 set the business interface of the EJB
	 */
	public void setBusinessInterface(Class class1) {
		this.businessInterface = class1;
	}

	public void afterLocated() {
		if (this.businessInterface == null) {
			throw new IllegalArgumentException("businessInterface property must be set in SimpleRemoteStatelessSessionProxyFactoryBean");
		}
		ProxyFactory pf = new ProxyFactory(new Class[] { this.businessInterface });
		pf.addInterceptor(this);
		this.proxy = pf.getProxy();
	}

	public Object getObject() throws BeansException {
		return this.proxy;
	}

	public boolean isSingleton() {
		return true;
	}

	public PropertyValues getPropertyValues() {
		return null;
	}

}
