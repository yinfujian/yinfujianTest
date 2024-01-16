/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */


package org.springframework.context;

import java.util.EventListener;

/**
 * Interface to be implemented by event listeners.
 * Based on standard java.util base class for Observer
 * 要由事件侦听器实现的接口。
 * 基于观察者的标准java.util基类
 * design pattern.
 * @author  Rod Johnson
 */
public interface ApplicationListener extends EventListener {

	/**
	* Handle an application event
	 * 处理应用事件
	* @param e event to respond to
	*/
    void onApplicationEvent(ApplicationEvent e);

}

