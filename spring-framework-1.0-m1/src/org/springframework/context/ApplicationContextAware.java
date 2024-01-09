/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the application context it runs in.
 *
 * <p>Implementing this interface is typical when an object requires
 * access to file resources, i.e. wants to call getResourceAsStream or
 * getResourceBasePath, or to the message source. Configuration via bean
 * references should make it unnecessary to implement this interface for
 * bean lookup purposes.
 *
 * <p>ApplicationObjectSupport is a convenience base class for
 * application objects, implementing this interface.
 *
 * @author Rod Johnson
 * @see org.springframework.context.support.ApplicationObjectSupport
 */
public interface ApplicationContextAware {
	
	/** 
	 * Set the application context used by this object.
	 * Normally this call will be used to initialize the object.
	 * <p>Note that this call can occur multiple times if the context
	 * is reloadable. The implementation must check itself if it is
	 * already initialized resp. if it wants to perform reinitialization.
	 * @param context ApplicationContext object to be used by this object
	 * @throws ApplicationContextException if initialization attempted
	 * by this object fails
	 */
	void setApplicationContext(ApplicationContext context) throws ApplicationContextException;

}
