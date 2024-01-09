package org.springframework.remoting.caucho;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.AuthorizableRemoteProxyFactoryBean;

import com.caucho.burlap.client.BurlapProxyFactory;
import com.caucho.burlap.client.BurlapRuntimeException;

/**
 * Factory bean for Burlap proxies. Behaves like the proxied service when
 * used as bean reference, exposing the specified service interface.
 * The service URL must be an HTTP URL exposing a Burlap service.
 * Supports authentication via username and password.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>Note: Burlap services accessed with this proxy factory do not have to be
 * exported via BurlapServiceExporter, as there isn't any special handling involved. 
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see BurlapServiceExporter
 */
public class BurlapProxyFactoryBean extends AuthorizableRemoteProxyFactoryBean {

	protected Object createProxy() throws MalformedURLException {
		BurlapProxyFactory proxyFactory = new BurlapProxyFactory();
		proxyFactory.setUser(getUsername());
		proxyFactory.setPassword(getPassword());
		Object source = proxyFactory.create(getServiceInterface(), getServiceUrl());

		// Create AOP interceptor wrapping source
		ProxyFactory pf = new ProxyFactory(source);
		pf.addInterceptor(0, new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				try {
					return invocation.proceed();
				}
				catch (BurlapRuntimeException ex) {
					Throwable rootCause = (ex.getRootCause() != null) ? ex.getRootCause() : ex;
					throw new RemoteAccessException("Error on remote access", rootCause);
				}
				catch (UndeclaredThrowableException ex) {
					throw new RemoteAccessException("Error on remote access", ex.getUndeclaredThrowable());
				}
			}
		});
		return pf.getProxy();
	}

}
