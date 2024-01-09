/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ResultReader;

/**
 * Reusable threadsafe object to represent a SQL query. Subclasses must
 * implement the newResultReader() method to provide an object that can
 * save the results of iterating over the ResultSet.
 *
 * <p>This class provides a number of public final execute() methods that are
 * analogous to the different convenient JDO query execute() methods. Subclasses
 * can either rely on one of these inherited methods, or can add their own
 * custom execution methods, with meaningful names and typed parameters. Each
 * custom query method will invoke one of this class's untype query methods.
 *
 * @author Rod Johnson
 * @author Jean-Pierre Pawlak
 * @version $Id: SqlQuery.java,v 1.2 2003/08/26 17:24:44 jhoeller Exp $
 */
public abstract class SqlQuery extends SqlOperation {

	/**
	 * Number of rows to expect. If 0, unknown.
	 */
	private int rowsExpected;


	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------

	/**
	 * Allow use as a bean.
	 */
	public SqlQuery() {
	}

	/**
	 * Convenient constructor with DataSource and SQL string.
	 * @param ds DataSource to use to get connections
	 * @param sql to execute. SQL can also be supplied at runtime
	 * by overriding the getSql() method.
	 */
	public SqlQuery(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}


	//-------------------------------------------------------------------------
	// Bean properties
	//-------------------------------------------------------------------------

	/**
	 * Set the number of rows expected. This can be used to ensure
	 * efficient storage of results. The default behavior is not to
	 * expect any specific number of rows.
	 */
	public void setRowsExpected(int rowsExpected) {
		this.rowsExpected = rowsExpected;
	}

	/**
	 * Get the number of rows expected.
	 */
	public int getRowsExpected() {
		return rowsExpected;
	}


	//-------------------------------------------------------------------------
	// Execute methods
	//-------------------------------------------------------------------------

	/**
	 * All execution goes through this method
	 * @param parameters parameters, as to JDO queries. Primitive parameters must
	 * be represented by their Object wrapper type. The ordering of parameters is
	 * significant.
	 * @param context contextual information passed to the callback mapRow method.
	 * This parameter don't rely on the Jdbc request itself, but can be useful for
	 * creating the objects of the list.
	 * @return a list of objects, one per row of the ResultSet. Normally all these
	 * will be of the same class, although it is possible to use different types.
	 */
	public final List execute(final Object[] parameters, Map context) throws DataAccessException {
		validateParameters(parameters);
		ResultReader rr = newResultReader(this.rowsExpected, parameters, context);
		getJdbcTemplate().query(newPreparedStatementCreator(parameters), rr);
		return rr.getResults();
	}

	/**
	 * Convenient method to execute without context
	 * @param parameters parameters, as to JDO queries. Primitive parameters must
	 * be represented by their Object wrapper type. The ordering of parameters is
	 * significant.
	 */
	public final List execute(final Object[] parameters) throws DataAccessException {
		validateParameters(parameters);
		return execute(parameters, null);
	}

	/**
	 * Convenient method to execute without parameters
	 * @param context The contextual information for object creation
	 */
	public final List execute(Map context) throws DataAccessException {
		return execute((Object[]) null, context);
	}

	/**
	 * Convenient method to execute without parameters nor context
	 */
	public final List execute() throws DataAccessException {
		return execute((Object[]) null);
	}

	/**
	 * Convenient method to execute with a single int parameter and context
	 * @param p1 single int parameter
	 * @param context The contextual information for object creation
	 */
	public final List execute(int p1, Map context) throws DataAccessException {
		return execute(new Object[]{new Integer(p1)}, context);
	}

	/**
	 * Convenient method to execute with a single int parameter
	 * @param p1 single int parameter
	 */
	public final List execute(int p1) throws DataAccessException {
		return execute(p1, null);
	}

	/**
	 * Convenient method to execute with two int parameters and context
	 * @param p1 first int parameter
	 * @param p2 second int parameter
	 * @param context The contextual information for object creation
	 */
	public final List execute(int p1, int p2, Map context) throws DataAccessException {
		return execute(new Object[]{new Integer(p1), new Integer(p2)}, context);
	}

	/**
	 * Convenient method to execute with two int parameters
	 * @param p1 first int parameter
	 * @param p2 second int parameter
	 */
	public final List execute(int p1, int p2) throws DataAccessException {
		return execute(p1, p2, null);
	}

	/**
	 * Convenient method to execute with a single long parameter and context
	 * @param p1 single long parameter
	 * @param context The contextual information for object creation
	 */
	public final List execute(long p1, Map context) throws DataAccessException {
		return execute(new Object[]{new Long(p1)}, context);
	}

	/**
	 * Convenient method to execute with a single long parameter
	 * @param p1 single long parameter
	 */
	public final List execute(long p1) throws DataAccessException {
		return execute(p1, null);
	}

	/**
	 * Convenient method to execute with a single String parameter and context
	 * @param p1 single String parameter
	 * @param context The contextual information for object creation
	 */
	public final List execute(String p1, Map context) throws DataAccessException {
		return execute(new Object[]{p1}, context);
	}

	/**
	 * Convenient method to execute with a single String parameter
	 * @param p1 single String parameter
	 */
	public final List execute(String p1) throws DataAccessException {
		return execute(p1, null);
	}

	/**
	 * Generic findObject method, used by all other findObject() methods.
	 * findObject() methods are like EJB entity bean finders, in that it is
	 * considered an error if they return more than one result.
	 * @return null if not found. Subclasses may choose to treat this
	 * as an error and throw an exception.
	 */
	public final Object findObject(Object[] parameters, Map context) throws DataAccessException {
		List l = execute(parameters, context);
		if (l.size() == 0)
			return null;
		if (l.size() > 1)
			throw new InvalidDataAccessApiUsageException("Result is not unique. Found " + l.size());
		return l.get(0);
	}

	/**
	 * Convenience method to find a single object without context
	 */
	public final Object findObject(Object[] parameters) throws DataAccessException {
		return findObject(parameters, null);
	}

	/**
	 * Convenience method to find a single object given a single int parameter and a context
	 */
	public final Object findObject(int p1, Map context) throws DataAccessException {
		return findObject(new Object[]{new Integer(p1)}, context);
	}

	/**
	 * Convenience method to find a single object given a single int parameter
	 */
	public final Object findObject(int p1) throws DataAccessException {
		return findObject(p1, null);
	}

	/**
	 * Convenience method to find a single object given two int parameters and a context
	 */
	public final Object findObject(int p1, int p2, Map context) throws DataAccessException {
		return findObject(new Object[]{new Integer(p1), new Integer(p2)}, context);
	}

	/**
	 * Convenience method to find a single object given two int parameters
	 */
	public final Object findObject(int p1, int p2) throws DataAccessException {
		return findObject(p1, p2, null);
	}

	/**
	 * Convenience method to find a single object given a single String parameter and a context
	 */
	public final Object findObject(String p1, Map context) throws DataAccessException {
		return findObject(new Object[]{p1}, context);
	}

	/**
	 * Convenience method to find a single object given a single String parameter
	 */
	public final Object findObject(String p1) throws DataAccessException {
		return findObject(p1, null);
	}

	/**
	 * Convenience method to find a single object given a single long parameter and a context
	 */
	public final Object findObject(long p1, Map context) throws DataAccessException {
		return findObject(new Object[]{new Long(p1)}, context);
	}

	/**
	 * Convenience method to find a single object given a single long parameter
	 */
	public final Object findObject(long p1) throws DataAccessException {
		return findObject(p1, null);
	}


	//-------------------------------------------------------------------------
	// Methods to be implemented by subclasses
	//-------------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to save a List of objects
	 * returned by the execute() method.
	 * @param rowsExpected If 0, we don't know how many rows to expect.
	 * This parameter can be ignored, but may help some implementations
	 * choose the most efficient Collection type: e.g. ArrayList
	 * instead of LinkedList for large result sets.
	 * @param parameters parameters to the execute() method, in case subclass is interested.
	 * May be null if there were no parameters.
	 */
	protected abstract ResultReader newResultReader(int rowsExpected, Object[] parameters, Map context);

	/**
	 * Subclasses can override this method to implement custom behavior on
	 * compilation. This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

}
