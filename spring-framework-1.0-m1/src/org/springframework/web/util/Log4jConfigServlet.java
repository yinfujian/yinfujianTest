package org.springframework.web.util;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Bootstrap servlet for custom Log4J initialization in a web environment.
 * Simply delegates to Log4jWebConfigurer.
 *
 * <p>Note: This servlet should have a lower load-on-startup value in web.xml
 * than ContextLoaderServlet, when using custom Log4J initialization.
 *
 * <p><i>Note that this class has been deprecated for containers implementing
 * Servlet API 2.4 or higher in favour of Log4jConfigListener.</i><br>
 * According to Servlet 2.4, listeners must be initialized before load-on-startup
 * servlets. Many Servlet 2.3 containers already enforce this behaviour
 * (see ContextLoaderServlet javadoc for details). If you use such a container,
 * this servlet can be replaced with Log4jConfigListener. Else or if working
 * with a Servlet 2.2 container, stick with this servlet.
 *
 * @author Juergen Hoeller
 * @author Darren Davison
 * @since 12.08.2003
 * @deprecated beyond Servlet 2.3 - use Log4jConfigListener in a
 * Servlet 2.4 compliant container, or a 2.3 container that initializes
 * listeners before load-on-startup servlets.
 * @see Log4jWebConfigurer
 * @see Log4jConfigListener
 * @see org.springframework.web.context.ContextLoaderServlet
 */
public class Log4jConfigServlet extends HttpServlet {

	public void init() {
		Log4jWebConfigurer.initLogging(getServletContext());
	}

	public void destroy() {
		Log4jWebConfigurer.shutdownLogging(getServletContext());
	}

	/**
	 * This should never even be called since no mapping to this servlet should
	 * ever be created in web.xml. That's why a correctly invoked Servlet 2.3
	 * listener is much more appropriate for initialization work ;-)
	 */
	public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
		getServletContext().log("Attempt to call service method on Log4jConfigServlet as " + request.getRequestURI() + " was ignored");
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

	public String getServletInfo() {
		return "Log4jConfigServlet for Servlet API 2.2/2.3 (deprecated in favour of Log4jConfigListener for Servlet API 2.4)";
	}

}
