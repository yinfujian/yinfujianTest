/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.dao;

/**
 * Generic exception thrown when the current process was
 * a deadlock loser, and its transaction rolled back.
 * @author Rod Johnson
 * @version $Id: DeadlockLoserDataAccessException.java,v 1.1.1.1 2003/08/14 16:20:24 trisberg Exp $
 */
public class DeadlockLoserDataAccessException extends DataAccessException {

	/**
	 * Constructor for DeadlockLoserDataAccessException.
	 * @param msg mesg
	 * @param ex root cause
	 */
	public DeadlockLoserDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
