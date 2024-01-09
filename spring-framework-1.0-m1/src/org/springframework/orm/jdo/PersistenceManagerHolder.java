package org.springframework.orm.jdo;

import javax.jdo.PersistenceManager;

/**
 * Holder wrapping a JDO PersistenceManager.
 * Features rollback-only support for nested JDO transactions.
 *
 * <p>JdoTransactionManager binds instances of this class
 * to the thread, for a given PersistenceManagerFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTransactionManager
 * @see PersistenceManagerFactoryUtils
 */
public class PersistenceManagerHolder {

	private PersistenceManager persistenceManager;

	private boolean rollbackOnly;

	public PersistenceManagerHolder(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

}
