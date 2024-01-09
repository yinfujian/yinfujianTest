/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Interface used by TransactionInterceptor. Implementations know
 * how to source transaction attributes, whether from configuration,
 * metadata attributes at source level, or anywhere else.
 * @author Rod Johnson
 * @since 15-Apr-2003
 * @version $Id: TransactionAttributeSource.java,v 1.1.1.1 2003/08/14 16:20:40 trisberg Exp $
 */
public interface TransactionAttributeSource {

	/**
	 * Return the transaction attribute for this method.
	 * Return null if the method is non-transactional.
	 * @param invocation method invocation descriptor
	 * @return TransactionAttribute transaction attribute or null.
	 */
	TransactionAttribute getTransactionAttribute(MethodInvocation invocation);

}
