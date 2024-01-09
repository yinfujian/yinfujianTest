package org.springframework.orm.jdo;

/**
 * JDO transaction object, representing a PersistenceManagerHolder.
 * Used as transaction object by JdoTransactionManager.
 *
 * <p>Instances of this class are the transaction objects that
 * JdoTransactionManager returns. They nest the thread-bound
 * PersistenceManagerHolder internally.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 */
public class JdoTransactionObject {

	private PersistenceManagerHolder persistenceManagerHolder;

	private boolean newPersistenceManagerHolder;

	public JdoTransactionObject(PersistenceManagerHolder persistenceManagerHolder, boolean newPersistenceManagerHolder) {
		this.persistenceManagerHolder = persistenceManagerHolder;
		this.newPersistenceManagerHolder = newPersistenceManagerHolder;
	}

	public PersistenceManagerHolder getPersistenceManagerHolder() {
		return persistenceManagerHolder;
	}

	public boolean isNewPersistenceManagerHolder() {
		return newPersistenceManagerHolder;
	}

}
