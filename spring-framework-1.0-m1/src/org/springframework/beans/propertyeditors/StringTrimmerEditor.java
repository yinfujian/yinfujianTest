package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

/**
 * Property editor that trims Strings.
 * Allows to transform an empty string into a null value.
 * Needs to be explictly registered, e.g. for command binding.
 * @author Juergen Hoeller
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 * @see org.springframework.web.bind.BindInitializer#initBinder
 */
public class StringTrimmerEditor extends PropertyEditorSupport {

	private boolean emptyAsNull;

	/**
	 * Create a new instance.
	 * @param emptyAsNull whether to transform an empty string to null
	 */
	public StringTrimmerEditor(boolean emptyAsNull) {
		this.emptyAsNull = emptyAsNull;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null) {
			setValue(null);
		}
		else {
			String value = text.trim();
			if (this.emptyAsNull && "".equals(value)) {
				setValue(null);
			}
			else {
				setValue(value);
			}
		}
	}

}
