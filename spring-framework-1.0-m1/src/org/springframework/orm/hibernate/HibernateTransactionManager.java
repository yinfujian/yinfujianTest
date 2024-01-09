package org.springframework.orm.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * PlatformTransactionManager implementation for single Hibernate session
 * factories. Binds a Hibernate Session from the specified factory to the
 * thread, potentially allowing for one thread Session per factory.
 * SessionFactoryUtils and HibernateTemplate are aware of thread-bound
 * Sessions and take part in such transactions automatically. Using either
 * is required for proper Hibernate access code supporting this transaction
 * handling mechanism.
 *
 * <p>Supports custom isolation levels, and timeouts that get applied as
 * appropriate Hibernate query timeouts. To support the latter, application
 * code must either use HibernateTemplate.find or call SessionFactoryUtils'
 * applyTransactionTimeout method for each created Hibernate Query object.
 *
 * <p>This implementation is appropriate for applications that solely use
 * Hibernate for transactional data access, but it also supports direct
 * data source access within a transaction (i.e. plain JDBC code working
 * with the same DataSource). This allows for mixing services that access
 * Hibernate including proper transactional caching and services that use
 * plain JDBC without being aware of Hibernate! Application code needs to
 * stick to the same simple Connection lookup pattern as with
 * DataSourceTransactionManager (i.e. DataSourceUtils.getConnection).
 *
 * <p>Note that to be able to register a DataSource's Connection for plain
 * JDBC code, this instance needs to be aware of the DataSource (see
 * setDataSource). The given DataSource should obviously match the one used
 * by the given SessionFactory. To achieve this, either configure both to
 * the same JNDI DataSource, or create the SessionFactory by supplying the
 * same DataSource to LocalSessionFactoryBean. In the latter case, the
 * Hibernate settings do not have to define a connection provider at all,
 * avoiding duplicated configuration.
 *
 * <p>JTA resp. JtaTransactionManager is necessary for accessing multiple
 * transactional resources. The DataSource that Hibernate uses needs to be
 * JTA-enabled then (see container setup), alternatively the Hibernate JCA
 * connector can to be used for direct container integration. Normally,
 * Hibernate JTA setup is somewhat container-specific due to the JTA
 * TransactionManager lookup, required for proper transactional handling of
 * the JVM-level read-write cache. Using the JCA Connector can solve this but
 * involves classloading issues and container-specific connector deployment.
 *
 * <p>Fortunately, there is an easier way with Spring: SessionFactoryUtils'
 * close and thus HibernateTemplate register synchronizations with
 * JtaTransactionManager, for proper completion callbacks. Therefore,
 * as long as JtaTransactionManager demarcates the JTA transactions,
 * Hibernate does not require any special JTA configuration for proper JTA.
 *
 * <p>Note: This class, like all of Spring's Hibernate support, requires
 * Hibernate 2.0 (initially developed with RC1).
 * 
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see SessionFactoryUtils#getSession
 * @see SessionFactoryUtils#applyTransactionTimeout
 * @see SessionFactoryUtils#closeSessionIfNecessary
 * @see HibernateTemplate#execute
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
 * @see org.springframework.jdbc.datasource.DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 */
public class HibernateTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private SessionFactory sessionFactory;

	private DataSource dataSource;

	private Interceptor entityInterceptor;

	/**
	 * Create a new HibernateTransactionManager instance.
	 * A SessionFactory has to be set to be able to use it.
	 * @see #setSessionFactory
	 */
	public HibernateTransactionManager() {
	}

	/**
	 * Create a new HibernateTransactionManager instance.
	 * @param sessionFactory SessionFactory to manage transactions for
	 * @param dataSource DataSource to manage transactions for
	 */
	public HibernateTransactionManager(SessionFactory sessionFactory, DataSource dataSource) {
		this.sessionFactory = sessionFactory;
		this.dataSource = dataSource;
		afterPropertiesSet();
	}

	/**
	 * Set the SessionFactory that this instance should manage transactions for.
	 */
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the SessionFactory that this instance should manage transactions for.
	 */
	public final SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set the JDBC DataSource that this instance should manage transactions for.
   * The DataSource should match the one used by the Hibernate SessionFactory.
	 * <p>A transactional JDBC Connection for this DataSource will be provided to
	 * application code accessing this DataSource directly via DataSourceUtils
	 * or JdbcTemplate. The Connection will be taken from the Hibernate Session.
	 */
	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the JDBC DataSource that this instance manages transactions for.
	 */
	public final DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this transaction manager.
	 * <p>Such an interceptor can either be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean, or at the Session level, i.e. on
	 * HibernateTemplate, HibernateInterceptor, and HibernateTransactionManager.
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 */
	public final void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Return the current Hibernate entity interceptor, or null if none.
	 */
	public Interceptor getEntityInterceptor() {
		return entityInterceptor;
	}

	public void afterPropertiesSet() {
		if (this.sessionFactory == null) {
			throw new IllegalArgumentException("sessionFactory is required");
		}
	}

	protected Object doGetTransaction() throws CannotCreateTransactionException, TransactionException {
		if (SessionFactoryUtils.getThreadObjectManager().hasThreadObject(this.sessionFactory)) {
			logger.debug("Found thread-bound Session for Hibernate transaction");
			SessionHolder sessionHolder = (SessionHolder) SessionFactoryUtils.getThreadObjectManager().getThreadObject(this.sessionFactory);
			return new HibernateTransactionObject(sessionHolder, false);
		}
		else {
			logger.debug("Opening new Session for Hibernate transaction");
			Session session = SessionFactoryUtils.getSession(this.sessionFactory, this.entityInterceptor);
			return new HibernateTransactionObject(new SessionHolder(session), true);
		}
	}

	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
		return (txObject.getSessionHolder().getTransaction() != null);
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
		Session session = txObject.getSessionHolder().getSession();
		logger.debug("Beginning Hibernate transaction");
		try {
			// apply isolation level
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				logger.debug("Changing isolation level to " + definition.getIsolationLevel());
				txObject.setPreviousIsolationLevel(new Integer(session.connection().getTransactionIsolation()));
				session.connection().setTransactionIsolation(definition.getIsolationLevel());
			}

			// apply read-only
			if (definition.isReadOnly()) {
				session.setFlushMode(FlushMode.NEVER);
				session.connection().setReadOnly(true);
			}

			// add the Hibernate transaction to the session holder
			txObject.getSessionHolder().setTransaction(session.beginTransaction());

			// register transaction timeout
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getSessionHolder().setTimeoutInSeconds(definition.getTimeout());
			}

			// bind the session holder to the thread
			if (txObject.isNewSessionHolder()) {
				SessionFactoryUtils.getThreadObjectManager().bindThreadObject(this.sessionFactory, txObject.getSessionHolder());
			}

			// register the Hibernate Session's JDBC Connection for the DataSource, if set
			if (this.dataSource != null) {
				ConnectionHolder conHolder = new ConnectionHolder(session.connection());
				if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(definition.getTimeout());
				}
				DataSourceUtils.getThreadObjectManager().bindThreadObject(this.dataSource, conHolder);
			}
		}
		catch (SQLException ex) {
			throw new CannotCreateTransactionException("Cannot set transaction isolation", ex);
		}
		catch (net.sf.hibernate.TransactionException ex) {
			throw new CannotCreateTransactionException("Cannot create Hibernate transaction", ex.getCause());
		}
		catch (HibernateException ex) {
			throw new CannotCreateTransactionException("Cannot create Hibernate transaction", ex);
		}
	}

	protected void doCommit(TransactionStatus status) throws TransactionException {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		if (txObject.getSessionHolder().isRollbackOnly()) {
			// nested Hibernate transaction demanded rollback-only
			doRollback(status);
		}
		else {
			logger.debug("Committing Hibernate transaction");
			try {
				txObject.getSessionHolder().getTransaction().commit();
			}
			catch (net.sf.hibernate.TransactionException ex) {
				throw new TransactionSystemException("Cannot commit Hibernate transaction", ex.getCause());
			}
			catch (HibernateException ex) {
				// assumably from an implicit flush
				throw SessionFactoryUtils.convertHibernateAccessException(ex);
			}
			finally {
				closeSession(txObject);
			}
		}
	}

	protected void doRollback(TransactionStatus status) throws TransactionException {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		logger.debug("Rolling back Hibernate transaction");
		try {
			txObject.getSessionHolder().getTransaction().rollback();
		}
		catch (net.sf.hibernate.TransactionException ex) {
			throw new TransactionSystemException("Cannot rollback Hibernate transaction", ex.getCause());
		}
		catch (HibernateException ex) {
			throw new TransactionSystemException("Cannot rollback Hibernate transaction", ex);
		}
		finally {
			closeSession(txObject);
		}
	}

	protected void doSetRollbackOnly(TransactionStatus status) throws TransactionException {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		logger.debug("Setting Hibernate transaction rollback-only");
		txObject.getSessionHolder().setRollbackOnly();
	}

	private void closeSession(HibernateTransactionObject txObject) {
		// remove the session holder from the thread
		if (txObject.isNewSessionHolder()) {
			SessionFactoryUtils.getThreadObjectManager().removeThreadObject(this.sessionFactory);
		}

		// remove the JDBC connection holder from the thread, if set
		if (this.dataSource != null) {
			DataSourceUtils.getThreadObjectManager().removeThreadObject(this.dataSource);
		}

		try {
			Connection con = txObject.getSessionHolder().getSession().connection();

			// reset transaction isolation to previous value, if changed for the transaction
			if (txObject.getPreviousIsolationLevel() != null) {
				logger.debug("Resetting isolation level to " + txObject.getPreviousIsolationLevel());
				con.setTransactionIsolation(txObject.getPreviousIsolationLevel().intValue());
			}

			// reset read-only
			if (con.isReadOnly()) {
				con.setReadOnly(false);
			}
		}
		catch (HibernateException ex) {
			logger.warn("Cannot reset transaction isolation", ex);
		}
		catch (SQLException ex) {
			logger.warn("Cannot reset transaction isolation", ex);
		}
		finally {
			if (txObject.isNewSessionHolder()) {
				logger.debug("Closing Hibernate session after transaction");
				try {
					SessionFactoryUtils.closeSessionIfNecessary(txObject.getSessionHolder().getSession(), this.sessionFactory);
				}
				catch (CleanupFailureDataAccessException ex) {
					// just log it, to keep a transaction-related exception
					logger.error("Cannot close Hibernate Session after transaction", ex);
				}
			}
			else {
				logger.debug("Not closing pre-bound Hibernate Session after transaction");
			}
		}
	}
	
}
