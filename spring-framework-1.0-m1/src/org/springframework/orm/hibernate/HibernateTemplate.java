package org.springframework.orm.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.type.Type;
import org.springframework.dao.DataAccessException;

/**
 * Helper class that simplifies Hibernate data access code, and converts
 * checked HibernateExceptions into unchecked HibernateJdbc/SystemExceptions,
 * compatible to the org.springframework.dao exception hierarchy.
 *
 * <p>Typically used to implement data access or business logic services that
 * use Hibernate within their implementation but are Hibernate-agnostic in
 * their interface. The latter resp. code calling the latter only have to deal
 * with business objects, query objects, and org.springframework.dao exceptions.
 *
 * <p>The central method is "execute", supporting Hibernate code implementing
 * the HibernateCallback interface. It provides Hibernate Session handling
 * such that neither the HibernateCallback implementation nor the calling
 * code needs to explicitly care about retrieving/closing Hibernate Sessions,
 * or handling Session lifecycle exceptions. For typical single step actions,
 * there are various convenience methods (find, load, saveOrUpdate, delete).
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a SessionFactory reference, or get prepared in an application context
 * and given to services as bean reference. Note: The SessionFactory should
 * always be configured as bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>This class can be considered a programmatic alternative to
 * HibernateInterceptor. The major advantage is its straightforwardness, the
 * major disadvantage that no checked application exceptions can get thrown
 * from within data access code. Respective checks and the actual throwing of
 * such exceptions can often be deferred to after callback execution, though. 
 *
 * <p>Note that even if HibernateTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be Hibernate-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * code dependencies. For example, switching to JTA is just a matter of
 * Spring configuration (use JtaTransactionManager instead), without needing
 * to touch application code.
 *
 * <p>LocalSessionFactoryBean is the preferred way of obtaining a reference
 * to a specific Hibernate SessionFactory, at least in a non-EJB environment.
 *
 * <p>Note: This class, like all of Spring's Hibernate support, requires
 * Hibernate 2.0 (initially developed with RC1).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateCallback
 * @see HibernateInterceptor
 * @see HibernateTransactionManager
 * @see LocalSessionFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class HibernateTemplate extends HibernateAccessor {

	private boolean allowCreate = true;

	/**
	 * Create a new HibernateTemplate instance.
	 */
	public HibernateTemplate() {
	}

	/**
	 * Create a new HibernateTemplate instance.
	 * @param sessionFactory SessionFactory to create Sessions
	 */
	public HibernateTemplate(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
		afterPropertiesSet();
	}

	/**
	 * Create a new HibernateTemplate instance.
	 * @param sessionFactory SessionFactory to create Sessions
	 * @param allowCreate if a new Session should be created
	 * if no thread-bound found
	 */
	public HibernateTemplate(SessionFactory sessionFactory, boolean allowCreate) {
		setSessionFactory(sessionFactory);
		setAllowCreate(allowCreate);
		afterPropertiesSet();
	}

	/**
	 * Set if a new Session should be created if no thread-bound found.
	 * <p>HibernateTemplate is aware of a respective Session bound to the
	 * current thread, for example when using HibernateTransactionManager.
	 * If allowCreate is true, a new Session will be created if none found.
	 * If false, an IllegalStateException will get thrown in this case.
	 * @see SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	/**
	 * Return if a new Session should be created if no thread-bound found.
	 */
	public boolean isAllowCreate() {
		return allowCreate;
	}

	/**
	 * Execute the action specified by the given action object within a session.
	 * Application exceptions thrown by the action object get propagated to the
	 * caller (can only be unchecked). Hibernate exceptions are transformed into
	 * appropriate DAO ones. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like HibernateTransactionManager.
	 * Generally, callback code must not touch any Session lifecycle methods,
	 * like close, disconnect, or reconnect, to let the template do its work.
	 * @param action action object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 * @see HibernateTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 */
	public Object execute(HibernateCallback action) throws DataAccessException {
		Session session = (!this.allowCreate ?
				SessionFactoryUtils.getSession(getSessionFactory(), false) :
				SessionFactoryUtils.getSession(getSessionFactory(), getEntityInterceptor()));
		if (getFlushMode() == FLUSH_NEVER) {
			session.setFlushMode(FlushMode.NEVER);
		}
		try {
			Object result = action.doInHibernate(session);
			if (isFlushNecessary(SessionFactoryUtils.isSessionBoundToThread(session, getSessionFactory()))) {
				session.flush();
			}
			return result;
		}
		catch (HibernateException ex) {
			throw SessionFactoryUtils.convertHibernateAccessException(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
		}
	}

	/**
	 * Return the persistent instance of the given entity class with the
	 * given identifier. Note that this method returns null if not found,
	 * in contrast to Session.load itself.
	 * <p>This is a convenience method for single step actions,
	 * mirroring Session.load.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance, or null if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#load(Class,Serializable)
	 */
	public Object load(final Class entityClass, final Serializable id) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				try {
					return session.load(entityClass, id);
				}
				catch (ObjectNotFoundException ex) {
					// Session.load throws this exception if not found
					// -> the contract of this template method is to return null
					return null;
				}
			}
		});
	}

	/**
	 * Save respectively update the given persistent instance,
	 * according to its id (matching the configured "unsaved-value"?).
	 * <p>This is a convenience method for single step actions,
	 * mirroring Session.saveOrUpdate.
	 * @param entity the persistent instance to save resp. update
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#saveOrUpdate(Object)
	 */
	public void saveOrUpdate(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.saveOrUpdate(entity);
				return null;
			}
		});
	}

	/**
	 * Save the given persistent instance.
	 * <p>This is a convenience method for single step actions,
	 * mirroring Session.save.
	 * @param entity the persistent instance to save
	 * @return the generated identifier
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#saveOrUpdate(Object)
	 */
	public Serializable save(final Object entity) throws DataAccessException {
		return (Serializable) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.save(entity);
			}
		});
	}

	/**
	 * Update the given persistent instance.
	 * <p>This is a convenience method for single step actions,
	 * mirroring Session.update.
	 * @param entity the persistent instance to update
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#saveOrUpdate(Object)
	 */
	public void update(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.update(entity);
				return null;
			}
		});
	}

	/**
	 * Delete the given persistent instance.
	 * <p>This is a convenience method for single step actions,
	 * mirroring Session.delete.
	 * @param entity the persistent instance to delete
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(Object)
	 */
	public void delete(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.delete(entity);
				return null;
			}
		});
	}

	/**
	 * Delete all given persistent instances.
	 * This can be combined with any of the find methods to delete by query
	 * in two lines of code, similar to Session's delete by query methods.
	 * <p>This is a convenience method for single step actions,
	 * mirroring Session.delete.
	 * @param entities the persistent instances to delete
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(String)
	 */
	public void deleteAll(final List entities) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				for (Iterator it = entities.iterator(); it.hasNext();) {
					session.delete(it.next());
				}
				return null;
			}
		});
	}

	/**
	 * Execute the specified action assuming that the result object is a List.
	 * <p>This is a convenience method for executing Hibernate find calls
	 * within an action.
	 * @param action action object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 */
	public List executeFind(HibernateCallback action) throws DataAccessException {
		return (List) execute(action);
	}

	/**
	 * Execute a query for persistent instances.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryString a query expressed in Hibernate's query language
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	public List find(final String queryString) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	public List find(final String queryString, final Object value) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				queryObject.setParameter(0, value);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a query for persistent instances, binding one value
	 * to a "?" parameter of the given type in the query string.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	public List find(final String queryString, final Object value, final Type type)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				queryObject.setParameter(0, value, type);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	public List find(final String queryString, final Object[] values) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value);
				}
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a query for persistent instances, binding a number of
	 * values to "?" parameters of the given types in the query string.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	public List find(final String queryString, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value, types[i]);
				}
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a query for persistent instances, binding the properties
	 * of the given bean to <i>named</i> parameters in the query string.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Query.setProperties. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param valueBean the values of the parameters
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 * @see net.sf.hibernate.Query#setProperties
	 */
	public List findByValueBean(final String queryString, final Object valueBean)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				queryObject.setProperties(valueBean);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a named query for persistent instances.
	 * A named query is defined in a Hibernate mapping file.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find for named queries. Use a custom HibernateCallback
	 * implementation to leverage the full power of Hibernate's Query API.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	public List findByNamedQuery(final String queryName) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find for named queries. Use a custom HibernateCallback
	 * implementation to leverage the full power of Hibernate's Query API.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	public List findByNamedQuery(final String queryName, final Object value)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setParameter(0, value);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find for named queries. Use a custom HibernateCallback
	 * implementation to leverage the full power of Hibernate's Query API.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param type Hibernate type of the parameter
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	public List findByNamedQuery(final String queryName, final Object value, final Type type)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setParameter(0, value, type);
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find for named queries. Use a custom HibernateCallback
	 * implementation to leverage the full power of Hibernate's Query API.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param values the values of the parameters
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	public List findByNamedQuery(final String queryName, final Object[] values)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value);
				}
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Session.find for named queries. Use a custom HibernateCallback
	 * implementation to leverage the full power of Hibernate's Query API.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	public List findByNamedQuery(final String queryName, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value, types[i]);
				}
				return queryObject.list();
			}
		});
	}

	/**
	 * Execute a named query for persistent instances, binding the properties
	 * of the given bean to <i>named</i> parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * <p>This is a convenience method for single step actions, mirroring
	 * Query.setProperties. Use a custom HibernateCallback implementation to
	 * leverage the full power of Hibernate's Query API.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param valueBean the values of the parameters
	 * @return the List of persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 * @see net.sf.hibernate.Query#setProperties
	 */
	public List findByNamedQueryAndValueBean(final String queryName, final Object valueBean)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setProperties(valueBean);
				return queryObject.list();
			}
		});
	}

	/**
	 * Create a Query object for the given Session and the given query string.
	 * Applies a transaction timeout, if any.
	 * @param session current Hibernate Session
	 * @param queryString the HQL query string
	 * @return the Query object
	 * @throws HibernateException if the query could not be created
	 */
	public Query createQuery(Session session, String queryString) throws HibernateException {
		Query queryObject = session.createQuery(queryString);
		SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
		return queryObject;
	}

	/**
	 * Create a named Query object for the given Session and the given query name.
	 * Applies a transaction timeout, if any.
	 * @param session current Hibernate Session
	 * @param queryName the name of the query in the Hibernate mapping file
	 * @return the Query object
	 * @throws HibernateException if the query could not be created
	 */
	public Query getNamedQuery(Session session, String queryName) throws HibernateException {
		Query queryObject = session.getNamedQuery(queryName);
		SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
		return queryObject;
	}

}
