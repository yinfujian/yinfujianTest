/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.support;

import javax.jms.MessageListener;

/** 
 * Convenient superclass for JMS MDBs.
 * Requires subclasses to implement the JMS interface MessageListener.
 * @author Rod Johnson
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public abstract class AbstractJmsMessageDrivenBean 
	extends AbstractMessageDrivenBean
	implements MessageListener {
	
	// Empty: the purpose of this class is to ensure
	// that subclasses implement javax.jms.MessageListener
	
} 
