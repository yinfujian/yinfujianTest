/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.bind;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Parameter extraction methods.
 * This class supports an approach distinct from data binding,
 * in which parameters of specific types are required.
 * This is very useful for simple submissions.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 */
public abstract class RequestUtils {

	/**
	 * Throw a ServletException if the given HTTP request method should be rejected.
	 * @param request request to check
	 * @param method method (such as "GET") which should be rejected
	 * @param method to reject
	 */
	public static void rejectRequestMethod(HttpServletRequest request, String method) throws ServletException {
		if (request.getMethod().equals(method))
			throw new ServletException("This resource does not support request method '" + method + "'");
	}

	/**
	 * Get an int parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 */
	public static int getIntParameter(HttpServletRequest request, String name, int defaultVal) {
		try {
			return getRequiredIntParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an int parameter, throwing an exception if it isn't found or isn't a number.
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int getRequiredIntParameter(HttpServletRequest request, String name) throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new ServletRequestBindingException("Required int parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new ServletRequestBindingException("Required int parameter '" + name + "' contained no value");
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException ex) {
				throw new ServletRequestBindingException("Required int parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}

	/**
	 * Get an int parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 */
	public static long getLongParameter(HttpServletRequest request, String name, long defaultVal) {
		try {
			return getRequiredLongParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a long parameter, throwing an exception if it isn't found or isn't a number.
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long getRequiredLongParameter(HttpServletRequest request, String name) throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new ServletRequestBindingException("Required long parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new ServletRequestBindingException("Required long parameter '" + name + "' contained no value");
		try {
			return Long.parseLong(s);
		}
		catch (NumberFormatException ex) {
				throw new ServletRequestBindingException("Required long parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}

	/**
	 * Get a double parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 */
	public static double getDoubleParameter(HttpServletRequest request, String name, double defaultVal) {
		try {
			return getRequiredDoubleParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a double parameter, throwing an exception if it isn't found or isn't a number.
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double getRequiredDoubleParameter(HttpServletRequest request, String name) throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new ServletRequestBindingException("Required double parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new ServletRequestBindingException("Required double parameter '" + name + "' contained no value");
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex) {
				throw new ServletRequestBindingException("Required double parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}
	
	/**
	 * Get a boolean parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 */
	public static boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultVal) {
		try {
			return getRequiredBooleanParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a boolean parameter, throwing an exception if it isn't found or isn't a boolean.
	 * True is true or yes (no case) or 1.
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean getRequiredBooleanParameter(HttpServletRequest request, String name) throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new ServletRequestBindingException("Required boolean parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new ServletRequestBindingException("Required boolean parameter '" + name + "' contained no value");
		return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equals("1");
	}

	/**
	 * Get a string parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 */
	public static String getStringParameter(HttpServletRequest request, String name, String defaultVal) {
		try {
			return getRequiredStringParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a string parameter, throwing an exception if it isn't found or isn't a boolean.
	 * True is true or yes (no case) or 1.
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String getRequiredStringParameter(HttpServletRequest request, String name) throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new ServletRequestBindingException("Required string parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new ServletRequestBindingException("Required string parameter '" + name + "' contained no value");
		return s;
	}

}
