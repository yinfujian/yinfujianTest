/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context;

/**
 * Base bean to hold minimum of application context configuration.
 * Can be subclassed to add additional properties.
 * @author Rod Johnson
 */
public class ContextOptions {

	private boolean reloadable = true;

	public ContextOptions() {
	}

	/**
	 * 当应用运行的过程中，配置是否可重新加载，也就是说refresh方法是否可重新执行
	 * <p>Note: Implementations are not obliged to support thread-safe reloading
	 * in a production environment. An implementation of reloading that cannot be
	 * guaranteed to be threadsafe but is sufficient during development is all that
	 * is required. Of course any limitations of an implementation should be documented.
	 * 在生产环境中实现不必支持线程安全的重新加载。重新加载的实现不能保证是线程安全的，但在开发过程中就足够了。当然，实施的任何限制都应该记录下来。
	 * @return whether we can reload this config
	 */
	public boolean isReloadable() {
		return reloadable;
	}

	/**
	 * Set if we can reload this config.
	 * @param reloadable if we can reload this config.
	 */
	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	public String toString() {
		return getClass().getName() + ": reloadable=" + reloadable;
	}

}
