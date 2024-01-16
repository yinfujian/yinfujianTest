/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventMulticaster;
import org.springframework.context.ApplicationListener;


/**
 * Concrete implementation of ApplicationEventMulticaster
 * Doesn't permit multiple instances of the same listener.
 * ApplicationEventMulticaster的具体实现
 * 不允许同一侦听器的多个实例。
 *
 * <p>Note that this class doesn't try to do anything clever to ensure thread
 * safety if listeners are added or removed at runtime. A technique such as
 * Copy-on-Write (Lea:137) could be used to ensure this, but the assumption in
 * this version of this framework is that listeners will be added at application
 * configuration time and not added or removed as the application runs.
 * 请注意，如果在运行时添加或删除侦听器，这个类不会试图做任何巧妙的事情来确保线程安全。
 * 可以使用诸如写时复制（Lea:137）之类的技术来确保这一点，但此框架版本中的假设是，侦听器将在应用程序配置时添加，而不会在应用程序运行时添加或删除。
 *
 * <p>All listeners are invoked in the calling thread. This allows the danger of
 * a rogue listener blocking the entire application, but adds minimal overhead.
 * 所有侦听器都在调用线程中调用。这允许流氓侦听器阻塞整个应用程序的危险，但增加了最小的开销。
 *
 * <p>An alternative implementation could be more sophisticated in both these respects.
 * 在这两个方面，另一种实施方式可能更为复杂。
 *
 * @author Rod Johnson
 */
public class ApplicationEventMulticasterImpl implements ApplicationEventMulticaster {

	/** Set of listeners */ // 监听器的容器
	private Set eventListeners = new HashSet();

	public void addApplicationListener(ApplicationListener l) {
		eventListeners.add(l);
	}

	public void removeApplicationListener(ApplicationListener l) {
		eventListeners.remove(l);
	}

	// 时间发生时，逐一调用
	public void onApplicationEvent(ApplicationEvent e) {
		Iterator i = eventListeners.iterator();
		while (i.hasNext()) {
			ApplicationListener l = (ApplicationListener) i.next();
			l.onApplicationEvent(e);
		}
	}

	public void removeAllListeners() {
		eventListeners.clear();
	}

}
