/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction;

/**
 * Exception thrown when a transaction can't be created using an
 * underlying transaction API such as JTA.
 * @author Rod Johnson
 * @since 17-Mar-2003
 * @version $Revision: 1.1.1.1 $
 */
public class CannotCreateTransactionException extends TransactionException {

	public CannotCreateTransactionException(String msg) {
		super(msg);
	}

	public CannotCreateTransactionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
