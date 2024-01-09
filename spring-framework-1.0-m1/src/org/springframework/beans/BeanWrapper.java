/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Map;

/**
 * The central interface of the Spring JavaBeans infrastructure.
 * Interface to be implemented by classes that can manipulate Java beans.
 *
 * <p>Implementing classes have the ability to get and set
 * property values (individually or in bulk), get property descriptors
 * and query the readability and writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting of
 * properties on subproperties to an unlimited depth.
 *
 * <p>If a property update causes an exception, a PropertyVetoException will be thrown.
 * Bulk updates continue after exceptions are encountered, throwing an exception
 * wrapping <b>all</b> exceptions encountered during the update.
 *
 * <p>BeanWrapper implementations can be used repeatedly, with their "target" or
 * wrapped object changed.
 *
 * <p>This interface supports the ability to add standard JavaBeans API
 * PropertyChangeListeners and VetoableChangeListeners, without the need for
 * supporting code in the target class. VetoableChangeListeners can veto
 * individual property changes.
 * 
 * @author Rod Johnson
 * @since 13 April 2001
 * @version $Id: BeanWrapper.java,v 1.1.1.1 2003/08/14 16:20:15 trisberg Exp $
 */
public interface BeanWrapper {

	/**
	 * Path separator for nested properties.
	 * Follows normal Java conventions: getFoo().getBar() would be "foo.bar".
	 */
	String NESTED_PROPERTY_SEPARATOR = ".";

	/**
	 * Set a property value. This method is provided for convenience only.
	 * The setPropertyValue(PropertyValue) method is more powerful,
	 * providing the ability to set indexed properties etc.
	 * @param propertyName name of the property to set value of
	 * @param value the new value
	 */
	void setPropertyValue(String propertyName, Object value) throws PropertyVetoException, BeansException;

	/**
	 * Update a property value.
	 * <b>This is the preferred way to update an individual property.</b>
	 * @param pv object containing new property value
	 */
	void setPropertyValue(PropertyValue pv) throws PropertyVetoException, BeansException;

	/**
	 * Get the value of a property.
	 * @param propertyName name of the property to get the value of
	 * @return the value of the property.
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't readable, or if the property getter throws an exception.
	 */
	Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * Get the value of an indexed property.
	 * @param propertyName name of the property to get value of
	 * @param index index from 0 of the property
	 * @return the value of the property
	 * @throws FatalBeanException if there is no such indexed property
	 * or if the getter method throws an exception.
	 */
	Object getIndexedPropertyValue(String propertyName, int index) throws BeansException;

	/**
	 * Perform a bulk update from a Map.
	 * <p>Bulk updates from PropertyValues are more powerful: This method is
	 * provided for convenience. It is impossible to set indexed properties
	 * using this method. Otherwise, behaviour will be identical to that of
	 * the setPropertyValues(PropertyValues) method.
	 * @param m Map to take properties from. Contains property value objects,
	 * keyed by property name
	 */
	void setPropertyValues(Map m) throws BeansException;

	/**
	 * The preferred way to perform a bulk update.
	 * <p>Note that performing a bulk update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a vetoed property change or a type mismatch,
	 * but <b>not</b> an invalid fieldname or the like) is encountered, throwing a
	 * PropertyVetoExceptionsException containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated stay changed.
	 * <p>Does not allow unknown fields.
	 * Equivalent to setPropertyValues(pvs, false, null).
	 * @param pvs PropertyValues to set on the target object
	 */
	void setPropertyValues(PropertyValues pvs) throws BeansException;

	/**
	 * Perform a bulk update with full control over behavior.
	 * Note that performing a bulk update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a vetoed property change or a type mismatch,
	 * but <b>not</b> an invalid fieldname or the like) is encountered, throwing a
	 * PropertyVetoExceptionsException containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated stay changed.
	 * <p>Does not allow unknown fields.
	 * @param pvs PropertyValues to set on the target object
	 * @param ignoreUnknown should we ignore unknown values (not found in the bean!?)
	 * @param pvsValidator property values validator. Ignored if it's null.
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, PropertyValuesValidator pvsValidator) throws BeansException;

	/**
	 * Get the PropertyDescriptors standard JavaBeans introspection identified
	 * on this object.
	 * @return the PropertyDescriptors standard JavaBeans introspection identified
	 * on this object
	 */
	PropertyDescriptor[] getPropertyDescriptors() throws BeansException;

	/**
	 * Get the property descriptor for a particular property, or null if there
	 * is no such property.
	 * @param propertyName property to check status for
	 * @return the property descriptor for a particular property, or null if there
	 * is no such property
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException;

	/**
	 * Return whether this property is readable.
	 * @return whether this property is readable
	 * @param propertyName property to check status for
	 */
	boolean isReadableProperty(String propertyName);

	/**
	 * Return whether this property is writable.
	 * @return whether this property is writable
	 * @param propertyName property to check status for
	 */
	boolean isWritableProperty(String propertyName);

	/**
	 * Return the bean wrapped by this object (cannot be null).
	 * @return the bean wrapped by this object
	 */
	Object getWrappedInstance();

	/**
	 * Change the wrapped object. Implementations are required
	 * to allow the type of the wrapped object to change.
	 * @param obj wrapped object that we are manipulating
	 */
	void setWrappedInstance(Object obj) throws BeansException;

	/**
	 * This method is included for efficiency. If an implementation
	 * caches all necessary information about the class,
	 * it might be faster to instantiate a new instance in the
	 * class than create a new wrapper to work with a new object
	 */
	void newWrappedInstance() throws BeansException;

	/**
	 * Convenience method to return the class of the wrapped object.
	 * @return the class of the wrapped object
	 */
	Class getWrappedClass();

	/**
	 * Register the given custom property editor for the given type and
	 * property, or for all properties of the given type.
	 * @param requiredType type of the property, can be null if a property is
	 * given but should be specified in any case for consistency checking
	 * @param propertyPath path of the property (name or nested path), or
	 * null if registering an editor for all properties of the given type
	 * @param propertyEditor editor to register
	 */
	void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor);

	/**
	 * Find a custom property editor for the given type and property.
	 * @param requiredType type of the property, can be null if a property is
	 * given but should be specified in any case for consistency checking
	 * @param propertyPath path of the property (name or nested path), or
	 * null if looking for an editor for all properties of the given type
	 * @return the registered editor, or null if none
	 */
	PropertyEditor findCustomEditor(Class requiredType, String propertyPath);


	//---------------------------------------------------------------------
	// Bean event support
	//---------------------------------------------------------------------

	/**
	 * Add a VetoableChangeListener that will be notified of property updates.
	 * @param l VetoableChangeListener notified of all property updates
	 */
	void addVetoableChangeListener(VetoableChangeListener l);

	/**
	 * Remove a VetoableChangeListener that will be notified of property updates.
	 * @param l VetoableChangeListener to remove
	 */
	void removeVetoableChangeListener(VetoableChangeListener l);

	/**
	 * Add a VetoableChangeListener that will be notified of updates to a single property.
	 * @param l VetoableChangeListener to add
	 * @param propertyName name of property this listeners will listen to updates for
	 */
	void addVetoableChangeListener(String propertyName, VetoableChangeListener l);

	/**
	 * Remove a VetoableChangeListener that will be notified of updates to a single property.
	 * @param l VetoableChangeListener to remove
	 * @param propertyName name of property this listeners formerly listened to updates for
	 */
	void removeVetoableChangeListener(String propertyName, VetoableChangeListener l);

	/**
	 * Add a PropertyChangeListener that will be notified of property updates.
	 * @param l PropertyChangeListener notified of all property updates
	 */
	void addPropertyChangeListener(PropertyChangeListener l);

	/**
	 * Remove a PropertyChangeListener that was formerly notified of property updates.
	 * @param l PropertyChangeListener to remove
	 */
	void removePropertyChangeListener(PropertyChangeListener l);

	/**
	 * Add a PropertyChangeListener that will be notified of updates to a single property.
	 * @param propertyName property the listener is interested in
	 * @param l PropertyChangeListener notified of property updates to this property
	 */
	void addPropertyChangeListener(String propertyName, PropertyChangeListener l);

	/**
	 * Remove a PropertyChangeListener that was notified of updates to a single property.
	 * @param propertyName property the listener is interested in
	 * @param l PropertyChangeListener to remove
	 */
	void removePropertyChangeListener(String propertyName, PropertyChangeListener l);

	/**
	 * Should we send out event notifications? Disabling this functionality
	 * (which is enabled by default) may improve performance.
	 * @return whether we notify listeners of property updates
	 */
	boolean isEventPropagationEnabled();

	/**
	 * Enable or disable event propogation. Any existing listeners will be preserved
	 * and will again be notified of events when event propagation is re-enabled.
	 * However no new listeners can be added in this period:
	 * Calls to add or remove listeners will be ignored.
	 * @param flag whether we notify listeners of property updates
	 */
	void setEventPropagationEnabled(boolean flag);

	/**
	 * Invoke the named method. This interface is designed to encourage
	 * working with bean properties, rather than methods, so this method
	 * shouldn't be used in most cases, but it is necessary to provide
	 * a simple means to invoking a named method.
	 * @param methodName name of the method to invoke
	 * @param args args to pass
	 * @return follows Method.invoke(). Void calls return null;
	 * primitives are wrapped as objects.
	 * @see java.lang.reflect.Method#invoke
	 */
	Object invoke(String methodName, Object[] args) throws BeansException;

}
