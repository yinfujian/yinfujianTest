/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

/**
 * Interface that defines contract of incrementing
 * any data store field's maximum value. Works much like
 * sequence number generator. Typical implementations could use
 * RDBMS SQL and/or Stored Procedures to do the job.
 * <br><br><b>History:</b> 
 * <li>17/04/2003 : donated to Spring by Dmitriy Kopylenko
 * <li>19/04/2003 : modified by Isabelle Muszynski, added nextDoubleValue
 * <li>09/05/2003 : modified by JPP, added nextLongValue
 * @author Dmitriy Kopylenko
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @version $Id: DataFieldMaxValueIncrementer.java,v 1.1.1.1 2003/08/14 16:20:26 trisberg Exp $
 */
public interface DataFieldMaxValueIncrementer {
	
    /**
     * Increments data store field's max value as int
     * @return int next data store value such as <b>max + 1</b>
     * @throws DataAccessException
     */
    int nextIntValue() throws DataAccessException;

	/**
	 * Increments data store field's max value as long
	 * @return int next data store value such as <b>max + 1</b>
	 * @throws DataAccessException
	 */
	long nextLongValue() throws DataAccessException;
	
    /**
     * Increments data store field's max value as double
     * @return next data store value such as <b>max + 1</b>
     * @throws DataAccessException
     */
    double nextDoubleValue() throws DataAccessException;
	
    /**
     * Increments data store field's max value as String
     * @return next data store value such as <b>max + 1</b>
     * @throws DataAccessException
     */
    String nextStringValue() throws DataAccessException;

    /**
     * Generic method to retrieve the next value.
     * Legal values for keyClass are int.class, double.class, String.class,
     * Integer.class and Double.class
     * @throws IllegalArgumentException if keyClass is not one of 
     * the expected types
     * @throws DataAccessException
     **/
    Object nextValue(Class keyClass) throws DataAccessException;
}
