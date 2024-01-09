/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.validation;

/**
 * Interface to be implemented by objects that can validate
 * application-specific objects. This enables validation to
 * be decoupled from the interface and placed in business objects.
 * @author Rod Johnson
 */
public interface Validator {
	
	/**
	 * Return whether or not this object can validate objects
	 * of the given class.
	 */
	boolean supports(Class clazz);
	
	/**
	 * Validate an object, which must be of a class for which
	 * the supports() method returned true.
	 * @param obj  Populated object to validate
	 * @param errors  Errors object we're building. May contain
	 * errors for this field relating to types.
	 */
	void validate(Object obj, Errors errors);

}
