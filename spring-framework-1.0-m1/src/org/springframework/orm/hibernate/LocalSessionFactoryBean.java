package org.springframework.orm.hibernate;

import java.util.Properties;

import javax.sql.DataSource;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.cfg.Environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean that creates a local Hibernate SessionFactory instance.
 * Behaves like a SessionFactory instance when used as bean reference,
 * e.g. for HibernateTemplate's sessionFactory property. Note that
 * switching to JndiObjectFactoryBean is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying data source) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings can either be read from a Hibernate XML file,
 * specified as "configLocation", or completely via this class. A typical
 * local configuration consists of one or more "mappingResources", various
 * Hibernate "properties" (not strictly necessary), and a "dataSource"
 * that the SessionFactory should use. The latter can also be specified via
 * Hibernate properties, but "dataSource" supports any Spring-configured
 * DataSource, instead of relying on Hibernate's own connection providers.
 *
 * <p>This SessionFactory handling strategy is appropriate for most types of
 * applications, from Hibernate-only single database apps to ones that need
 * distributed transactions. Either HibernateTransactionManager or
 * JtaTransactionManager can be used for transaction demarcation, the latter
 * only being necessary for transactions that span multiple databases.
 *
 * <p>Registering a SessionFactory with JNDI is only advisable when using
 * Hibernate's JCA Connector, i.e. when the application server cares for
 * initialization. Else, portability is rather limited: Manual JNDI binding
 * isn't supported by some application servers (e.g. Tomcat). Unfortunately,
 * JCA has drawbacks too: Its setup is container-specific and can be tedious.
 *
 * <p>Note that the JCA Connector's sole major strength is its seamless
 * cooperation with EJB containers and JTA services. If you do not use EJB
 * and initiate your JTA transactions via Spring's JtaTransactionManager,
 * you can get all benefits including distributed transactions and proper
 * transactional JVM-level caching with local SessionFactory setup too -
 * without any configuration hassle like container-specific setup.
 *
 * @author Juergen Hoeller
 * @since 05.05.2003
 * @see HibernateTemplate#setSessionFactory
 * @see HibernateTransactionManager#setSessionFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class LocalSessionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	private Log logger = LogFactory.getLog(getClass());

	private String configLocation;

	private String[] mappingResources;

	private Properties hibernateProperties;

	private DataSource dataSource;

	private Interceptor entityInterceptor;

	private SessionFactory sessionFactory;

	/**
	 * Set the location of the Hibernate XML config file as classpath resource.
	 * A typical value is "/hibernate.cfg.xml", in the case of web applications
	 * normally to be found in WEB-INF/classes.
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean. If neither a location
	 * nor any mapping resources are set, a default Hibernate configuration
	 * will be performed, using "/hibernate.cfg.xml".
	 */
	public final void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Hibernate mapping resources to be found in the classpath,
	 * like "/example.hbm.xml".
	 * <p>Can be used to override values from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 */
	public final void setMappingResources(String[] mappingResources) {
		this.mappingResources = mappingResources;
	}

	/**
	 * Set Hibernate properties, like "hibernate.dialect".
	 * <p>Can be used to override values in a Hibernate XML config file,
	 * or to specify all necessary properties locally.
	 * <p>Note: Do not specify a transaction provider here when using
	 * Spring-driven transactions. It is also advisable to omit connection
	 * provider settings and use a Spring-set DataSource instead.
	 * @see #setDataSource
	 */
	public final void setHibernateProperties(Properties hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	/**
	 * Set the DataSource to be used by the SessionFactory.
	 * If set, this will override any setting in the Hibernate properties.
	 * <p>Note: If this is set, the Hibernate settings do not have to define
	 * a connection provider at all, avoiding duplicated configuration.
	 */
	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this factory.
	 * <p>Such an interceptor can either be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean, or at the Session level, i.e. on
	 * HibernateTemplate, HibernateInterceptor, and HibernateTransactionManager.
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 * @see HibernateTransactionManager#setEntityInterceptor
	 */
	public final void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Initialize the SessionFactory for the given or the default location.
	 * @throws IllegalArgumentException in case of illegal property values
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	public final void afterPropertiesSet() throws IllegalArgumentException, HibernateException {
		// create Configuration instance
		Configuration config = newConfiguration();

		if (this.configLocation == null && this.mappingResources == null) {
			// default Hibernate configuration from "/hibernate.cfg.xml"
			config.configure();
		}

		if (this.configLocation != null) {
			// load Hibernate configuration from given location
			String resourceLocation = this.configLocation;
			if (!resourceLocation.startsWith("/")) {
				// always use root, as relative loading doesn't make sense
				resourceLocation = "/" + resourceLocation;
			}
			config.configure(resourceLocation);
		}

		if (this.mappingResources != null) {
			// register given Hibernate mapping definitions, contained in resource files
			for (int i = 0; i < this.mappingResources.length; i++) {
				config.addResource(this.mappingResources[i], Thread.currentThread().getContextClassLoader());
			}
		}

		if (this.hibernateProperties != null) {
			// add given Hibernate properties
			config.addProperties(this.hibernateProperties);
		}

		if (this.dataSource != null) {
			// make given DataSource available for SessionFactory configuration
			config.setProperty(Environment.CONNECTION_PROVIDER, LocalDataSourceConnectionProvider.class.getName());
			LocalDataSourceConnectionProvider.configTimeDataSourceHolder.set(this.dataSource);
		}

		if (this.entityInterceptor != null) {
			// set given entity interceptor at SessionFactory level
			config.setInterceptor(this.entityInterceptor);
		}

		// build SessionFactory instance
		logger.info("Building new Hibernate SessionFactory for LocalSessionFactoryBean [" + this + "]");
		this.sessionFactory = newSessionFactory(config);
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the Configuration instance used for SessionFactory creation.
	 * The properties of this LocalSessionFactoryBean will be applied to
	 * the Configuration object that gets returned here.
	 * <p>The default implementation creates a new Configuration instance.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom Configuration subclass.
	 * @return the Configuration instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see net.sf.hibernate.cfg.Configuration#Configuration()
	 */
	protected Configuration newConfiguration() throws HibernateException {
		return new Configuration();
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the SessionFactory instance, creating it via the given Configuration
	 * object that got prepared by this LocalSessionFactoryBean.
	 * <p>The default implementation invokes Configuration's buildSessionFactory.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom SessionFactoryImpl subclass.
	 * @param config Configuration prepared by this LocalSessionFactoryBean
	 * @return the SessionFactory instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see net.sf.hibernate.cfg.Configuration#buildSessionFactory
	 */
	protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
		return config.buildSessionFactory();
	}

	/**
	 * Return the singleton SessionFactory.
	 */
	public final Object getObject() {
		return this.sessionFactory;
	}

	public final boolean isSingleton() {
		return true;
	}

	public final PropertyValues getPropertyValues() {
		return null;
	}

	/**
	 * Close the SessionFactory on context shutdown.
	 */
	public void destroy() throws HibernateException {
		logger.info("Closing Hibernate SessionFactory of LocalSessionFactoryBean [" + this + "]");
		this.sessionFactory.close();
	}

}
