/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlParameter;

/**
 * Root of the JDBC object hierarchy, as described in Chapter 9 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">
 * Expert One-On-One J2EE Design and Development</a> by Rod Johnson (Wrox, 2002).
 *
 * <p>An "RDBMS operation" is a multithreaded, reusable object representing
 * a query, update or stored procedure. An RDBMS operation is <b>not</b> a command,
 * as a command isn't reusable. However, execute methods may take commands as
 * arguments. Subclasses should be Java beans, allowing easy configuration.
 *
 * <p>This class and subclasses throw runtime exceptions, defined in the
 * org.springframework.dao package (and as thrown by the org.springframework.jdbc.core
 * package, which the classes in this package use to perform raw JDBC actions).
 *
 * <p>Subclasses should set SQL and add parameters before invoking the
 * compile() method. The order in which parameters are added is significant.
 * The appropriate execute or update method can then be invoked.
 *
 * @author Rod Johnson
 * @version $Id: RdbmsOperation.java,v 1.3 2003/08/28 17:26:36 jhoeller Exp $
 * @see org.springframework.dao
 * @see org.springframework.jdbc.core
 */
public abstract class RdbmsOperation implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** List of SqlParameter objects */
	private List declaredParameters = new LinkedList();

	/** SQL statement */
	private String sql;

	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled;

	/**
	 * Add anonymous parameters, specifying only their SQL types
	 * as defined in the java.sql.Types class.
	 * <p>Parameter ordering is significant. This method is an alternative
	 * to the declareParameter() method, which should normally be preferred.
	 * @param types array of SQL types as defined in the
	 * java.sql.Types class
	 * @throws InvalidDataAccessApiUsageException if the operation is already compiled
	 */
	public void setTypes(int[] types) throws InvalidDataAccessApiUsageException {
		if (compiled)
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				declareParameter(new SqlParameter(types[i]));
			}
		}
	}

	/**
	 * Declare a parameter. The order in which this method is called is significant.
	 * @param param SqlParameter to add. This will specify SQL type and (optionally)
	 * the parameter's name.
	 * @throws InvalidDataAccessApiUsageException if the operation is already compiled,
	 * and hence cannot be configured further
	 */
	public void declareParameter(SqlParameter param) throws InvalidDataAccessApiUsageException {
		if (compiled)
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		declaredParameters.add(param);
	}

	/**
	 * Return a list of the declared SqlParameter objects.
	 * @return a list of the declared SqlParameter objects
	 */
	protected List getDeclaredParameters() {
		return declaredParameters;
	}

	/**
	 * Set the SQL executed by this operation.
	 * @param sql the SQL executed by this operation
	 */
	public final void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * Subclasses can override this to supply dynamic SQL if they wish,
	 * but SQL is normally set by calling the setSql() method
	 * or in a subclass constructor.
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Ensures compilation if used in a bean factory.
	 */
	public void afterPropertiesSet() {
		compile();
	}

	/**
	 * Is this operation "compiled"? Compilation, as in JDO,
	 * means that the operation is fully configured, and ready to use.
	 * The exact meaning of compilation will vary between subclasses.
	 * @return whether this operation is compiled, and ready to use.
	 */
	public final boolean isCompiled() {
		return compiled;
	}

	/**
	 * Compile this query.
	 * Ignore subsequent attempts to compile
	 * @throws InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided.
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (this.sql == null)
				throw new InvalidDataAccessApiUsageException("Sql must be set in class " + getClass().getName());
			// Invoke subclass compile
			compileInternal();
			this.compiled = true;
			logger.info("RdbmsOperation with SQL [" + getSql() + "] compiled");
		}
	}

	/**
	 * Subclasses must implement to perform their own compilation.
	 * Invoked after this class's compilation is complete.
	 * Subclasses can assume that sql has been supplied and that
	 * a DataSource has been supplied.
	 * @throws InvalidDataAccessApiUsageException if the subclass
	 * hasn't been properly configured.
	 */
	protected abstract void compileInternal() throws InvalidDataAccessApiUsageException;

	/**
	 * Validate the parameters passed to an execute method based on declared parameters.
	 * Subclasses should invoke this method before every execute() or update() method.
	 * @param parameters parameters supplied. May be null.
	 * @throws InvalidDataAccessApiUsageException if the parameters are invalid
	 */
	protected final void validateParameters(Object[] parameters) throws InvalidDataAccessApiUsageException {
		if (!compiled)
			throw new InvalidDataAccessApiUsageException("SQL operation must be compiled before execution");

		if (parameters != null) {
			if (declaredParameters == null)
				throw new InvalidDataAccessApiUsageException("Didn't expect any parameters: none was declared");
			if (parameters.length != declaredParameters.size())
				throw new InvalidDataAccessApiUsageException(parameters.length + " parameters were supplied, but none was declared in class " + getClass().getName());
		}
		else {
			// No parameters were supplied
			if (!declaredParameters.isEmpty())
				throw new InvalidDataAccessApiUsageException(declaredParameters.size() + " parameters must be supplied");
		}
	}

}
