/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.ClassLoaderUtils;

/**
 * Factory for creating SQLExceptionTranslator based on the
 * DatabaseProductName taken from the DatabaseMetaData.
 *
 * <p>Returns a SQLExceptionTranslator populated with vendor codes
 * defined in a configuration file named "sql-error-codes.xml".
 * Reads the default file in this package if not overridden by a file
 * in the root of the classpath (e.g. in the WEB-INF/classes directory).
 *
 * @author Thomas Risberg
   @version $Id: SQLExceptionTranslatorFactory.java,v 1.1 2003/08/22 08:18:33 jhoeller Exp $
 */
public class SQLExceptionTranslatorFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Name of custom SQL error codes file, loading from the root
	 * of the classpath (e.g. in the WEB-INF/classes directory).
	 */
	public static final String SQL_ERROR_CODE_OVERRIDE_PATH = "/sql-error-codes.xml";

	/**
	 * Name of SQL error code files, loading from the classpath.
	 * Will look in the package of this class (no leading /).
	 */
	public static final String SQL_ERROR_CODE_DEFAULT_PATH = "sql-error-codes.xml";

	/**
	* Keep track of this instance so we can return it to classes that request it.
	*/
	private static final SQLExceptionTranslatorFactory instance;

	static {
		instance = new SQLExceptionTranslatorFactory();
	}

	/**
	 * Return singleton instance.
	 */
	public static SQLExceptionTranslatorFactory getInstance() {
		return instance;
	}


	/**
	* Create a Map to hold error codes for all databases defined in the config file.
	*/
	private Map rdbmsErrorCodes;

	/**
	 * Not public to enforce Singleton design pattern.
	 */
	private SQLExceptionTranslatorFactory() {
		try {
			InputStream is = ClassLoaderUtils.getResourceAsStream(getClass(), SQL_ERROR_CODE_OVERRIDE_PATH);
			if (is == null) {
				is = ClassLoaderUtils.getResourceAsStream(getClass(), SQL_ERROR_CODE_DEFAULT_PATH);
				if (is == null)
					throw new BeanDefinitionStoreException("Unable to locate file [" + SQL_ERROR_CODE_DEFAULT_PATH +"]",null);
			}
			ListableBeanFactory bf = new XmlBeanFactory(is);
			String[] rdbmsNames = bf.getBeanDefinitionNames(org.springframework.jdbc.core.SQLErrorCodes.class);
			rdbmsErrorCodes = new HashMap(rdbmsNames.length);

			for (int i = 0; i < rdbmsNames.length; i++) {
				SQLErrorCodes ec = (SQLErrorCodes) bf.getBean(rdbmsNames[i]);
				if (ec.getBadSqlGrammarCodes() == null)
					ec.setBadSqlGrammarCodes(new String[0]);
				else
					Arrays.sort(ec.getBadSqlGrammarCodes());
				if (ec.getDataIntegrityViolationCodes() == null)
					ec.setDataIntegrityViolationCodes(new String[0]);
				else
					Arrays.sort(ec.getDataIntegrityViolationCodes());
				rdbmsErrorCodes.put(rdbmsNames[i], ec);
			}
		}
		catch (BeanDefinitionStoreException be) {
			logger.warn("Error loading error codes from config file.  Message = " + be.getMessage());
			rdbmsErrorCodes = new HashMap(0);
		}
	}

	/**
	 * Return SQLExceptionTranslator for the given DataSource,
	 * evaluating DatabaseProductName from DatabaseMetaData.
	 */
	public SQLExceptionTranslator getDefaultTranslator(DataSource ds) {
		logger.info("Initializing default SQL exception translator");
		Connection con = DataSourceUtils.getConnection(ds);
		if (con != null) {
			// should always be the case outside of test environments
			try {
				DatabaseMetaData dbmd = con.getMetaData();
				if (dbmd != null) {
					String dbName = dbmd.getDatabaseProductName();
					// special check for DB2
					if (dbName != null && dbName.startsWith("DB2/"))
						dbName = "DB2";
					if (dbName != null) {
						SQLErrorCodes sec = (SQLErrorCodes) rdbmsErrorCodes.get(dbName);
						if (sec != null)
							return new SQLErrorCodeSQLExceptionTranslator(sec);
					}
				}
				// could not find the database among the defined ones
			}
			catch (SQLException se) {
				// this is bad - we probably lost the connection
				logger.warn("Could not read database meta data for exception translator", se);
			}
			finally {
				DataSourceUtils.closeConnectionIfNecessary(con, ds);
			}
		}
		return new SQLStateSQLExceptionTranslator();
	}

	/**
	 * Return plain SQLErrorCodes instance for the given database.
	 */
	public SQLErrorCodes getErrorCodes(String dbName) {
		SQLErrorCodes sec = (SQLErrorCodes) rdbmsErrorCodes.get(dbName);
		if (sec == null) {
		// could not find the database among the defined ones
			sec = new SQLErrorCodes();
		}
		return sec;
	}

}
