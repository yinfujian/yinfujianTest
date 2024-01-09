package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Implementation of SmartDataSource that wraps a single connection which is not
 * closed after use. Obviously, this is not multi-threading capable.
 *
 * <p>Note that at shutdown, someone should close the underlying connection via the
 * close() method. Client code will never call close on the connection handle if it
 * is SmartDataSource-aware (e.g. uses DataSourceUtils.closeConnectionIfNecessary).
 *
 * <p>If client code will call close in the assumption of a pooled connection, like
 * when using persistence toolkits, set suppressClose to true. This will return a
 * close-suppressing proxy instead of the physical connection. Be aware that you will
 * not be able to cast this to an OracleConnection anymore, for example.
 *
 * <p>This is primarily a test class. For example, it enables easy testing of code
 * outside of an application server, in conjunction with a simple JNDI environment.
 * In contrast to DriverManagerDataSource, it reuses the same connection all the time,
 * avoiding excessive creation of physical connections.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jndi.support.SimpleNamingContextBuilder
 */
public class SingleConnectionDataSource extends DriverManagerDataSource implements DisposableBean {

	private boolean suppressClose;

	/** wrapped connection */
	private Connection connection;

	/**
	 * Constructor for bean-style configuration.
	 */
	public SingleConnectionDataSource() {
	}

	/**
	 * Create a new SingleConnectionDataSource with the given standard
	 * DriverManager parameters.
	 * @param suppressClose if the returned connection will be a close-suppressing
	 * proxy or the physical connection.
	 */
	public SingleConnectionDataSource(String driverClassName, String url, String username, String password,
	                                  boolean suppressClose) throws CannotGetJdbcConnectionException {
		super(driverClassName, url, username, password);
		this.suppressClose = suppressClose;
	}

	/**
	 * Create a new SingleConnectionDataSource with a given connection.
	 * @param source underlying source connection
	 * @param suppressClose if the connection should be wrapped with a* connection that
	 * suppresses close() calls (to allow for normal close() usage in applications that
	 * expect a pooled connection but do not know our SmartDataSource interface).
	 */
	public SingleConnectionDataSource(Connection source, boolean suppressClose)
	    throws CannotGetJdbcConnectionException, InvalidDataAccessApiUsageException {
		if (source == null) {
			throw new InvalidDataAccessApiUsageException("Connection is null in SingleConnectionDataSource");
		}
		this.suppressClose = suppressClose;
		init(source);
	}

	/**
	 * Set if the returned connection will be a close-suppressing proxy or
	 * the physical connection.
	 */
	public void setSuppressClose(boolean suppressClose) {
		this.suppressClose = suppressClose;
	}

	/**
	 * Return if the returned connection will be a close-suppressing proxy or
	 * the physical connection.
	 */
	public boolean isSuppressClose() {
		return suppressClose;
	}

	/**
	 * This is a single connection: Do not close it when returning to the "pool".
	 */
	public boolean shouldClose(Connection conn) {
		return false;
	}

	/**
	 * Initialize the underlying connection via DriverManager.
	 */
	protected void init() throws CannotGetJdbcConnectionException {
		try {
			init(getConnectionFromDriverManager());
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Could not create connection", ex);
		}
	}

	/**
	 * Initialize the underlying connection.
	 * Wraps the connection with a close-suppressing proxy if necessary.
	 * @param source the JDBC Connection to use
	 */
	protected void init(Connection source) throws CannotGetJdbcConnectionException {
		// wrap connection?
		this.connection = this.suppressClose ? DataSourceUtils.getCloseSuppressingConnectionProxy(source) : source;
	}

	public Connection getConnection() throws SQLException {
		synchronized (this) {
			if (this.connection == null) {
				// no underlying connection -> lazy init via DriverManager
				init();
			}
		}
		if (this.connection.isClosed()) {
			throw new SQLException("Connection was closed in SingleConnectionDataSource. " +
			                       "Check that user code checks shouldClose() before closing connections, " +
			                       "or set suppressClose to true");
		}
		logger.debug("Returning single connection: " + this.connection);
		return this.connection;
	}

	/**
	 * Specifying a custom username and password doesn't make sense with a single connection.
	 * Returns the single connection if given the same username and password, though.
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		if (username != null && password != null && username.equals(getUsername()) && password.equals(getPassword())) {
			return getConnection();
		}
		else {
			throw new SQLException("SingleConnectionDataSource does not support custom username and password");
		}
	}

	/**
	 * Close the underlying connection.
	 * The provider of this DataSource needs to care for proper shutdown.
	 * <p>As this bean implements DisposableBean, a bean factory will
	 * automatically invoke this on destruction of its cached singletons.
	 */
	public void destroy() throws SQLException {
		if (this.connection != null) {
			this.connection.close();
		}
	}

}
