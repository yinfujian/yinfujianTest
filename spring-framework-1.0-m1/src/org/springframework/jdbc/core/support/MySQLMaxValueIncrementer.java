package org.springframework.jdbc.core.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.InternalErrorException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Class to increment maximum value of a given MySQL table with the equivalent
 * of an auto-increment column. Note: if you use this class, your MySQL key
 * column should <i>NOT</i> be auto-increment, as the sequence table does the job.
 *
 * <p>The sequence is kept in a table; there should be one sequence table per
 * table that needs an auto-generated key. The table type of the sequence table
 * should be MyISAM so the sequences are allocated without regard to any
 * transactions that might be in progress.
 *
 * <p>Example:
 * <p><code>
 * &nbsp;&nbsp;create table tab (id int unsigned not null primary key, text varchar(100));<br>
 * &nbsp;&nbsp;create table tab_sequence (value int not null) type=MYISAM;<br>
 * &nbsp;&nbsp;insert into tab_sequence values(0);<br>
 * </code>
 *
 * <p>If cacheSize is set, the intermediate values are served without querying the
 * database. If the server or your application is stopped or crashes or a transaction
 * is rolled back, the unused values will never be served. The maximum hole size in
 * numbering is consequently the value of cacheSize.
 *
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @author Thomas Risberg
 * @version $Id: MySQLMaxValueIncrementer.java,v 1.2 2003/08/17 20:37:14 jhoeller Exp $
 */

public class MySQLMaxValueIncrementer extends AbstractDataFieldMaxValueIncrementer {

	protected final Log logger = LogFactory.getLog(getClass());

	private NextMaxValueProvider nextMaxValueProvider;

	/**
	 * Default constructor.
	 **/
	public MySQLMaxValueIncrementer() {
		this.nextMaxValueProvider = new NextMaxValueProvider();
	}

	/**
	 * Alternative constructor.
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 **/
	public MySQLMaxValueIncrementer(DataSource ds, String incrementerName, String columnName) {
		super(ds, incrementerName, columnName);
		this.nextMaxValueProvider = new NextMaxValueProvider();
	}

	/**
	 * Alternative constructor.
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 * @param cacheSize the number of buffered keys
	 **/
	public MySQLMaxValueIncrementer(DataSource ds, String incrementerName, String columnName, int cacheSize) {
		super(ds, incrementerName, columnName, cacheSize);
		this.nextMaxValueProvider = new NextMaxValueProvider();
	}

	/**
	 * Alternative constructor.
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 * @param prefixWithZero in case of a String return value, should the string be prefixed with zeroes
	 * @param padding the length to which the string return value should be padded with zeroes
	 **/
	public MySQLMaxValueIncrementer(DataSource ds, String incrementerName, String columnName, boolean prefixWithZero, int padding) {
		super(ds, incrementerName, columnName);
		this.nextMaxValueProvider = new NextMaxValueProvider();
		this.nextMaxValueProvider.setPrefixWithZero(prefixWithZero, padding);
	}

	/**
	 * Alternative constructor.
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 * @param prefixWithZero in case of a String return value, should the string be prefixed with zeroes
	 * @param padding the length to which the string return value should be padded with zeroes
	 * @param cacheSize the number of buffered keys
	 **/
	public MySQLMaxValueIncrementer(DataSource ds, String incrementerName, String columnName, boolean prefixWithZero, int padding, int cacheSize) {
		super(ds, incrementerName, columnName, cacheSize);
		this.nextMaxValueProvider = new NextMaxValueProvider();
		this.nextMaxValueProvider.setPrefixWithZero(prefixWithZero, padding);
	}

	/**
	 * Set whether to prefix with zero.
	 */
	public void setPrefixWithZero(boolean prefixWithZero, int length) {
		this.nextMaxValueProvider.setPrefixWithZero(prefixWithZero, length);
	}

	/**
	 * @see org.springframework.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementIntValue()
	 */
	protected int incrementIntValue() {
		return nextMaxValueProvider.getNextIntValue();
	}

	/**
	 * @see org.springframework.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementLongValue()
	 */
	protected long incrementLongValue() {
		return nextMaxValueProvider.getNextLongValue();
	}

	/**
	 * @see org.springframework.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementDoubleValue()
	 */
	protected double incrementDoubleValue() {
		return nextMaxValueProvider.getNextDoubleValue();
	}

	/**
	 * @see org.springframework.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementStringValue()
	 */
	protected String incrementStringValue() {
		return nextMaxValueProvider.getNextStringValue();
	}

	// Private class that does the actual
	// job of getting the sequence.nextVal value
	private class NextMaxValueProvider extends AbstractNextMaxValueProvider {

		/** The Sql string for updating the sequence value */
		private String insertSql;

		/** The Sql string for retrieving the new sequence value */
		private String updateSql = "select last_insert_id()";

		/** The next id to serve */
		private long nextId = 0;

		/** The max id to serve */
		private long maxId = 0;

		synchronized protected long getNextKey(int type) throws DataAccessException {
			if (isDirty()) {
				initPrepare();
			}
			if (maxId == nextId) {
				/*
				* Need to use straight JDBC code because we need to make sure that the insert and select
				* are performed on the same connection (otherwise we can't be sure that last_insert_id()
				* returned the correct value)
				*/
				Connection con = null;
				Statement stmt = null;
				ResultSet rs = null;
				try {
					con = DataSourceUtils.getConnection(getDataSource());
					stmt = con.createStatement();
					DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
					// Increment the sequence column
					stmt.executeUpdate(insertSql);
					// Retrieve the new max of the sequence column
					rs = stmt.executeQuery(updateSql);
					if (rs.next()) {
						maxId = rs.getLong(1);
						if (logger.isInfoEnabled())
							logger.info("new maxId is : " + maxId);
					}
					else
						throw new InternalErrorException("last_insert_id() failed after executing an update");
					nextId = maxId - getCacheSize();
					nextId++;
					if (logger.isInfoEnabled())
						logger.info("nextId is : " + nextId);
				}
				catch (SQLException ex) {
					throw new DataAccessResourceFailureException("Could not obtain last_insert_id", ex);
				}
				finally {
					if (null != rs) {
						try {
							rs.close();
						}
						catch (SQLException e) {
						}
					}
					if (null != stmt) {
						try {
							stmt.close();
						}
						catch (SQLException e) {
						}
						DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
					}
				}
			}
			else
				nextId++;
			return nextId;
		}

		private void initPrepare() throws InvalidMaxValueIncrementerApiUsageException {
			afterPropertiesSet();
			if (getIncrementerName() == null)
				throw new InvalidMaxValueIncrementerApiUsageException("IncrementerName property must be set on " + getClass().getDeclaringClass().getName());
			if (getColumnName() == null)
				throw new InvalidMaxValueIncrementerApiUsageException("ColumnName property must be set on " + getClass().getDeclaringClass().getName());
			StringBuffer buf = new StringBuffer();
			buf.append("update ");
			buf.append(getIncrementerName());
			buf.append(" set ");
			buf.append(getColumnName());
			buf.append(" = last_insert_id(");
			buf.append(getColumnName());
			buf.append(" + ");
			buf.append(getCacheSize());
			buf.append(")");
			insertSql = buf.toString();
			if (logger.isInfoEnabled())
				logger.info("insertSql = " + insertSql);
			setDirty(false);
		}
	}

}

