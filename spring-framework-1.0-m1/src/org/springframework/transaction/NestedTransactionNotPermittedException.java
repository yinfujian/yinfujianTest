/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction;

/**
 * Exception thrown when an attempt is made to begin a transaction
 * but this would amount to a nested transaction, which is not
 * supporting by the underlying transaction implementation.
 * @author Rod Johnson
 * @since 17-Mar-2003
 * @version $Revision: 1.1.1.1 $
 */
public class NestedTransactionNotPermittedException extends CannotCreateTransactionException {

	public NestedTransactionNotPermittedException(String msg) {
		super(msg);
	}

	public NestedTransactionNotPermittedException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
