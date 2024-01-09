/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;

import org.aopalliance.intercept.AttributeRegistry;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * MethodPointcut implementation wrapping an
 * Interceptor that should always run.
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @version $Id: AlwaysInvoked.java,v 1.1.1.1 2003/08/14 16:20:12 trisberg Exp $
 */
public class AlwaysInvoked extends AbstractMethodPointcut implements StaticMethodPointcut {
	
	public AlwaysInvoked(MethodInterceptor interceptor) {
		super(interceptor);
	}

	/**
	 * @see org.springframework.aop.framework.StaticMethodPointcut#applies(java.lang.reflect.Method, AttributeRegistry)
	 */
	public boolean applies(Method m, AttributeRegistry ar) {
		return true;
	}

}
