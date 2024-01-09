package org.springframework.web.servlet.tags;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

import org.springframework.validation.Errors;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * Evaluates content if there are bind errors for a certain bean.
 * Exports an "errors" variable of type Errors for the given bean.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see BindTag
 */
public class BindErrorsTag extends RequestContextAwareTag {

	public static final String ERRORS_VARIABLE_NAME = "errors";

	private String name;

	/**
	 * Set the name of the bean that this tag should check.
	 */
	public void setName(String name) throws JspException {
		this.name = ExpressionEvaluationUtils.evaluateString("name", name, pageContext);
	}

	protected int doStartTagInternal() throws ServletException {
		Errors errors = getRequestContext().getErrors(this.name, isHtmlEscape());
		if (errors.hasErrors()) {
			this.pageContext.setAttribute(ERRORS_VARIABLE_NAME, errors);
			return EVAL_BODY_INCLUDE;
		}
		else {
			return SKIP_BODY;
		}
	}

}
