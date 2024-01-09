package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

/**
 * Properties editor for String[] type. Strings must be in CSV format.
 * This property editor is automatically registered by BeanWrapperImpl.
 * @author Rod Johnson
 * @see org.springframework.beans.BeanWrapperImpl
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

	public void setAsText(String s) throws IllegalArgumentException {
		String[] sa = StringUtils.commaDelimitedListToStringArray(s);
		setValue(sa);
	}

	public String getAsText() {
		String[] array = (String[]) this.getValue();
		return StringUtils.arrayToCommaDelimitedString(array);
	}

}
