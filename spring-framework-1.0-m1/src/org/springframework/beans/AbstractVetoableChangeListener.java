package org.springframework.beans;

import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience superclass for JavaBeans VetoableChangeListeners.
 * This class implements the VetoableChangeListener interface to delegate
 * the method call to one of any number of validation methods defined in
 * concrete subclasses. This is a typical use of reflection to avoid the
 * need for a chain of if/else statements, discussed in
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-on-One J2EE Design and Development</a>.
 *
 * <p>The signature for validation methods must be of this form
 * (the following example validates an int property named age):
 * <p><code>
 * public void validateAge(int age, PropertyChangeEvent e) throws PropertyVetoException
 * </code>
 * <p>Note that the field can be expected to have been converted to the required type,
 * simplifying validation logic.
 *
 * <p>Validation methods must be public or protected. The return value is not required,
 * but will be ignored.
 *
 * <p>Subclasses should be threadsafe: nothing in this superclass will cause
 * a problem. Because validation methods are cached by this class's constructor,
 * the overhead of reflection is not great.
 *
 * <p><b>NB:</b>Validation methods will receive a reversion event after they have
 * vetoed a change. So, if an email property is initially null and an invalid email address
 * is supplied and vetoed by the first call to validateEmail for the given validator,
 * a second event will be sent when the email field is reverted to null. This means that
 * validation methods must be able to cope with initial values. They can, however,
 * throw another PropertyVetoException, which will be ignored by the caller.
 *
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14 16:20:14 trisberg Exp $
 */
public abstract class AbstractVetoableChangeListener implements VetoableChangeListener {

	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Prefix for validation methods: a typical name might be validateAge()
	 */
	protected static final String VALIDATE_METHOD_PREFIX = "validate";
	
	/** Validation methods, keyed by propertyName */
	private HashMap validationMethodHash = new HashMap();

	/**
	 * Creates new AbstractVetoableChangeListener.
	 * Caches validation methods for efficiency.
	 */
	public AbstractVetoableChangeListener() throws SecurityException {
		// Look at all methods in the subclass, trying to find
		// methods that are validators according to our criteria
		Method [] methods = getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			// We're looking for methods with names starting with the given prefix,
			// and two parameters: the value (which may be of any type, primitive or object)
			// and a PropertyChangeEvent.
			if (methods[i].getName().startsWith(VALIDATE_METHOD_PREFIX) &&
				methods[i].getParameterTypes().length == 2 &&
				PropertyChangeEvent.class.isAssignableFrom(methods[i].getParameterTypes()[1])) {
				// We've found a potential validator: it has the right number of parameters
				// and its name begins with validate...
				logger.debug("Found potential validator method [" + methods[i] + "]");
				Class[] exceptions = methods[i].getExceptionTypes();
				// We don't care about the return type, but we must ensure that
				// the method throws only one checked exception, PropertyVetoException
				if (exceptions.length == 1 && PropertyVetoException.class.isAssignableFrom(exceptions[0])) {
					// We have a valid validator method
					// Ensure it's accessible (for example, it might be a method on an inner class)
					methods[i].setAccessible(true);
					String propertyName = Introspector.decapitalize(methods[i].getName().substring(VALIDATE_METHOD_PREFIX.length()));
					validationMethodHash.put(propertyName, methods[i]);
					logger.debug(methods[i] + " is validator for property " + propertyName);
				}
				else {
					logger.debug("Invalid validator");
				}
			}
			else {
				logger.debug("Method [" + methods[i] + "] is not a validator");
			}
		}
	}

	/**
	 * Implementation of VetoableChangeListener.
	 * Will attempt to locate the appropriate validator method and invoke it.
	 * Will do nothing if there is no validation method for this property.
	 */
	public final void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
		if (logger.isDebugEnabled())
			logger.debug("VetoableChangeEvent: old value=[" + e.getOldValue() + "] new value=[" + e.getNewValue() + "]");

		Method method = (Method) validationMethodHash.get(e.getPropertyName());
		if (method != null) {
			try {
				logger.debug("Using validator method: " + method);
				Object val = e.getNewValue();
				method.invoke(this, new Object[] { val, e });
			}
			catch (IllegalAccessException ex) {
				logger.warn("Can't validate: Method isn't accessible");
			}
			catch (InvocationTargetException ex) {
				// This is what we're looking for: the subclass's
				// validator method vetoed the property change event
				// We know that the exception must be of the correct type (unless
				// it's a runtime exception) as we checked the declared exceptions of the
				// validator method in this class's constructor.
				// If it IS a runtime exception, we just rethrow it, to encourage the
				// author of the subclass to write robust code...
				if (ex.getTargetException() instanceof RuntimeException)
					throw (RuntimeException) ex.getTargetException();
				PropertyVetoException pex = (PropertyVetoException) ex.getTargetException();
				throw pex;
			}
		}	// if there was a validator method for this property
		else {
			logger.debug("No validation method for property: " + e.getPropertyName());
		}
	}
	
}
