/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.jdbc.datasource;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for Spring's DataSource implementations,
 * taking care of the "uninteresting" glue.
 * @author Juergen Hoeller
 * @since 07.05.2003
 * @see DriverManagerDataSource
 * @version $Id: AbstractDataSource.java,v 1.1.1.1 2003/08/14 16:20:31 trisberg Exp $
 */
public abstract class AbstractDataSource implements DataSource {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Returns 0: means use default system timeout.
	 */
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public void setLoginTimeout(int timeout) throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout");
	}

	/**
	 * LogWriter methods are unsupported.
	 */
	public PrintWriter getLogWriter() {
		throw new UnsupportedOperationException("getLogWriter");
	}

	/**
	 * LogWriter methods are unsupported.
	 */
	public void setLogWriter(PrintWriter pw) throws SQLException {
		throw new UnsupportedOperationException("setLogWriter");
	}

}
