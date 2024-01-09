package org.springframework.web.bind;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

/**
 * Callback that allows for initialization of a binder with
 * custom editors before the binding. Used by BindUtils.
 * @author Jean-Pierre PAWLAK
 * @since 08.05.2003
 * @see BindUtils#bind(ServletRequest,Object,String,BindInitializer)
 * @see BindUtils#bindAndValidate(ServletRequest,Object,String,org.springframework.validation.Validator,BindInitializer)
 */
public interface BindInitializer {

	/**
	 * Initialize the given binder instance, e.g. with custom editors.
	 * Called by BindUtils#bind. This method allows you to register custom
	 * editors for certain fields of your command class. For instance, you will
	 * be able to transform Date objects into a String pattern and back, in order
	 * to allow your JavaBeans to have Date properties and still be able to
	 * set and display them in for instance an HTML interface.
	 * @param request current request
	 * @param binder new binder instance
	 * @throws ServletException in case of invalid state or arguments
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 * @see BindUtils#bind(ServletRequest,Object,String,BindInitializer)
	 */
	public void initBinder(ServletRequest request, ServletRequestDataBinder binder)
			throws ServletException ;
}
