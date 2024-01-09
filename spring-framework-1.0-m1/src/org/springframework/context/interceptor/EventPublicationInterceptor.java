package org.springframework.context.interceptor;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ApplicationObjectSupport;


/**
 * Interceptor that knows how to publish {@link ApplicationEvent}s to all 
 * <code>ApplicationListener</code>s registered with <code>ApplicationContext</code> 
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptor.java,v 1.3 2003/08/29 00:20:49 pawlakjp Exp $
 */
public class EventPublicationInterceptor extends ApplicationObjectSupport  implements MethodInterceptor, InitializingBean {

	private String applicationEventClassName;
	private Class applicationEventClass;
	
	
	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		ApplicationEvent applicationEvent = null;
		Constructor constructor = this.applicationEventClass.getConstructor(new Class[] {Object.class});
		applicationEvent = (ApplicationEvent)constructor.newInstance(new Object[] {invocation.getThis()});
		
		getApplicationContext().publishEvent(applicationEvent);
		
		return retVal;
	}

	/**
	 * Set applicationEventClassName
	 * @param className
	 */
	public void setApplicationEventClassName(String className) {
		this.applicationEventClassName = className;
	}
	
	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if(this.applicationEventClassName == null)
			//??? What Exception to throw ???
			throw new Exception("applicationEventClassName property must be set on " + getClass().getName());
		
		this.applicationEventClass = Class.forName(this.applicationEventClassName);
		if(!ApplicationEvent.class.isAssignableFrom(this.applicationEventClass))
			throw new IllegalArgumentException(getClass().getName() + " does not support '" + this.applicationEventClassName + "'" );
	}
}
