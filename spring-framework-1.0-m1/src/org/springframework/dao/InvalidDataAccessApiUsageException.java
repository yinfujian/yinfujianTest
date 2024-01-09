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
 * Exception thrown on incorrect usage of the API, such as failing to
 * "compile" a query object that needed compilation before execution.
 *
 * <p>This represents a problem in our Java data access framework,
 * not the underlying data access infrastructure.
 *
 * @author Rod Johnson
 */
public class InvalidDataAccessApiUsageException extends DataAccessException {

	/**
	 * Constructor for InvalidDataAccessApiUsageException.
	 * @param msg message
	 */
	public InvalidDataAccessApiUsageException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for InvalidDataAccessApiUsageException.
	 * @param msg message
	 * @param ex root cause, from an underlying API such as JDBC
	 */
	public InvalidDataAccessApiUsageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
