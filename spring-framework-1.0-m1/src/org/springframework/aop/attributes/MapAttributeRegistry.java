/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.attributes;

import java.lang.reflect.AccessibleObject;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.AttributeRegistry;

/**
 * 
 * @author Rod Johnson
 * @since 19-May-2003
 * @version $Id: MapAttributeRegistry.java,v 1.1.1.1 2003/08/14 16:20:12 trisberg Exp $
 */
public class MapAttributeRegistry implements AttributeRegistry {
	
	/**
	 * Map from Method (on class or interface) to attribute[]
	 */
	private Map attributeMap = new HashMap();
	
	public void setAttributes(AccessibleObject ao, Object[] atts) {
		this.attributeMap.put(ao, atts);
	}

	/**
	 * @see org.aopalliance.intercept.AttributeRegistry#getAttributes(java.lang.reflect.AccessibleObject)
	 */
	public Object[] getAttributes(AccessibleObject ao) {
		return (Object[]) this.attributeMap.get(ao);
	}

	/**
	 * @see org.aopalliance.intercept.AttributeRegistry#getAttributes(java.lang.Class)
	 */
	public Object[] getAttributes(Class clazz) {
		throw new UnsupportedOperationException("getAttributes");
	}

}
