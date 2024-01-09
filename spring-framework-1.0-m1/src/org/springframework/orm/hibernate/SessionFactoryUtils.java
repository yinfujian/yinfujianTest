package org.springframework.orm.hibernate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.ObjectDeletedException;
import net.sf.hibernate.PersistentObjectException;
import net.sf.hibernate.QueryException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.StaleObjectStateException;
import net.sf.hibernate.TransientObjectException;
import net.sf.hibernate.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ThreadObjectManager;

/**
 * Helper class featuring methods for Hibernate session handling,
 * allowing for reuse of Hibernate Session instances within transactions.
 * Supports synchronization with JTA transactions via JtaTransactionManager,
 * to allow for proper transactional handling of the JVM-level cache.
 *
 * <p>Used by HibernateTemplate, HibernateInterceptor, and
 * HibernateTransactionManager. Can also be used directly in application
 * code, e.g. in combination with HibernateInterceptor.
 *
 * <p>Note: This class, like all of Spring's Hibernate support, requires
 * Hibernate 2.0 (initially developed with RC1).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateTemplate
 * @see HibernateInterceptor
 * @see HibernateTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public abstract class SessionFactoryUtils {

	private static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);

	/**
	 * Per-thread mappings: SessionFactory -> SessionHolder
	 */
	private static final ThreadObjectManager threadObjectManager = new ThreadObjectManager();

	/**
	 * Return the thread object manager for Hibernate sessions, keeping a
	 * SessionFactory/SessionHolder map per thread for Hibernate transactions.
	 * <p>Note: This is an SPI method, not intended to be used by applications.
	 * @return the thread object manager
	 * @see #getSession
	 * @see HibernateTransactionManager
	 */
	public static ThreadObjectManager getThreadObjectManager() {
		return threadObjectManager;
	}

	/**
	 * Return if the given Session is bound to the current thread,
	 * for the given SessionFactory.
	 * @param session Session that should be checked
	 * @param sessionFactory SessionFactory that the Session was created with
	 * @return if the Session is bound for the SessionFactory
	 */
	public static boolean isSessionBoundToThread(Session session, SessionFactory sessionFactory) {
		SessionHolder sessionHolder = (SessionHolder) threadObjectManager.getThreadObject(sessionFactory);
		return (sessionHolder != null && session == sessionHolder.getSession());
	}

	/**
	 * Get a Hibernate Session for the given factory. Is aware of a respective Session
	 * bound to the current thread, for example when using HibernateTransactionManager.
	 * Will create a new Session else, if allowCreate is true.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param allowCreate if a new Session should be created if no thread-bound found
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 */
	public static Session getSession(SessionFactory sessionFactory, boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException {
		if (!threadObjectManager.hasThreadObject(sessionFactory) && !allowCreate) {
			throw new IllegalStateException("Not allowed to create new Session");
		}
		return getSession(sessionFactory, null);
	}

	/**
	 * Get a Hibernate Session for the given factory. Is aware of a respective Session
	 * bound to the current thread, for example when using HibernateTransactionManager.
	 * Will always create a new Session else.
	 * <p>Supports setting a Session-level Hibernate entity interceptor that allows
	 * to inspect and change property values before writing to and reading from the
	 * database. Such an interceptor can also be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 */
	public static Session getSession(SessionFactory sessionFactory, Interceptor entityInterceptor)
	    throws DataAccessResourceFailureException {
		SessionHolder holder = (SessionHolder) threadObjectManager.getThreadObject(sessionFactory);
		if (holder != null) {
			return holder.getSession();
		}
		try {
			logger.debug("Opening Hibernate session");
			return (entityInterceptor != null ? sessionFactory.openSession(entityInterceptor) : sessionFactory.openSession());
		}
		catch (JDBCException ex) {
			// SQLException underneath
			throw new DataAccessResourceFailureException("Cannot not open Hibernate session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Cannot not open Hibernate session", ex);
		}
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given Hibernate Query object.
	 * @param query the Hibernate Query object
	 * @param sessionFactory Hibernate SessionFactory that the Query was created for
	 */
	public static void applyTransactionTimeout(Query query, SessionFactory sessionFactory) {
		SessionHolder sessionHolder = (SessionHolder) threadObjectManager.getThreadObject(sessionFactory);
		if (sessionHolder != null && sessionHolder.getDeadline() != null) {
			query.setTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from
	 * the org.springframework.dao hierarchy.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 */
	public static DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (ex instanceof JDBCException) {
			// SQLException during Hibernate code used by the application
			return new HibernateJdbcException("Exception in Hibernate data access code", (JDBCException) ex);
		}
		if (ex instanceof QueryException) {
			return new InvalidDataAccessResourceUsageException("Invalid Hibernate query", ex);
		}
		if (ex instanceof StaleObjectStateException) {
			return new OptimisticLockingFailureException("Version check failed", ex);
		}
		if (ex instanceof PersistentObjectException) {
			return new InvalidDataAccessApiUsageException("Invalid object state", ex);
		}
		if (ex instanceof TransientObjectException) {
			return new InvalidDataAccessApiUsageException("Invalid object state", ex);
		}
		if (ex instanceof ObjectDeletedException) {
			return new InvalidDataAccessApiUsageException("Invalid object state", ex);
		}
		// fallback
		return new HibernateSystemException("Exception in Hibernate data access code", ex);
	}

	/**
	 * Close the given Session, created via the given factory,
	 * if it isn't bound to the thread.
	 * @param session Session to close
	 * @param sessionFactory Hibernate SessionFactory that the Session was created with
	 * @throws DataAccessResourceFailureException if the Session couldn't be closed
	 */
	public static void closeSessionIfNecessary(Session session, SessionFactory sessionFactory)
	    throws CleanupFailureDataAccessException {
		if (session == null || isSessionBoundToThread(session, sessionFactory)) {
			return;
		}
		if (TransactionSynchronizationManager.isActive()) {
			logger.debug("Registering transaction synchronization for Hibernate session");
			TransactionSynchronizationManager.register(new SessionSynchronization(session, sessionFactory));
			// use same Session for further Hibernate actions within the transaction
			// to save resources (thread object will get remoed by synchronization)
			threadObjectManager.bindThreadObject(sessionFactory, new SessionHolder(session));
		}
		else {
			doCloseSession(session);
		}
	}

	/**
	 * Actually perform close on the given Session.
	 */
	private static void doCloseSession(Session session) throws CleanupFailureDataAccessException {
		logger.debug("Closing Hibernate session");
		try {
			session.close();
		}
		catch (JDBCException ex) {
			// SQLException underneath
			throw new CleanupFailureDataAccessException("Cannot close Hibernate session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			throw new CleanupFailureDataAccessException("Cannot close Hibernate session", ex);
		}
	}


	/**
	 * Callback for resource cleanup at the end of a non-Hibernate transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class SessionSynchronization implements TransactionSynchronization {

		private Session session;
		private SessionFactory sessionFactory;

		private SessionSynchronization(Session session, SessionFactory sessionFactory) {
			this.session = session;
			this.sessionFactory = sessionFactory;
		}

		public void afterCompletion(int status) {
			threadObjectManager.removeThreadObject(this.sessionFactory);
			doCloseSession(this.session);
		}
	}

}
