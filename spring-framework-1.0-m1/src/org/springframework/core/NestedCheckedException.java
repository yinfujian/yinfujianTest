/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.core;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Handy class for wrapping runtime Exceptions with a root cause. This time-honoured
 * technique is no longer necessary in Java 1.4, which provides built-in support for
 * exception nesting. Thus exceptions in applications written to use Java 1.4 need not
 * extend this class.
 *
 * <p>Abstract to force the programmer to extend the class.
 * printStackTrace() etc. are forwarded to the wrapped Exception.
 * The present assumption is that all application-specific exceptions that could be
 * displayed to humans (users, administrators etc.) will implement the ErrorCoded interface.
 *
 * <p>The similarity between this class and the NestedCheckedException class is unavoidable,
 * as Java forces these two classes to have different superclasses (ah, the inflexibility
 * of concrete inheritance!).
 *
 * <p>As discussed in <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>,
 * runtime exceptions are often a better alternative to checked exceptions. However, all exceptions
 * should preserve their stack trace, if caused by a lower-level exception.
 *
 * @author Rod Johnson
 * @version $Id: NestedCheckedException.java,v 1.1.1.1 2003/08/14 16:20:24 trisberg Exp $
 */
public abstract class NestedCheckedException extends Exception implements HasRootCause {

	/** Root cause of this nested exception */
	private Throwable rootCause;

	/**
	 * Constructs a <code>ExceptionWrapperException</code> with the specified
	 * detail message.
	 * @param msg the detail message
	 */
	public NestedCheckedException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a <code>RemoteException</code> with the specified
	 * detail message and nested exception.
	 *
	 * @param msg the detail message
	 * @param ex the nested exception
	 */
	public NestedCheckedException(String msg, Throwable ex) {
		super(msg);
		rootCause = ex;
	}

	/**
	 * Returns the nested cause, or null if none.
	 */
	public Throwable getRootCause() {
		return rootCause;
	}

	/**
	 * Returns the detail message, including the message from the nested
	 * exception if there is one.
	 */
	public String getMessage() {
		if (rootCause == null)
			return super.getMessage();
		else
			return super.getMessage() + "; nested exception is: \n\t" + rootCause.toString();
	}

	/**
	 * Prints the composite message and the embedded stack trace to
	 * the specified stream <code>ps</code>.
	 * @param ps the print stream
	 */
	public void printStackTrace(PrintStream ps) {
		if (rootCause == null) {
			super.printStackTrace(ps);
		}
		else {
			//ps.println(this);
			rootCause.printStackTrace(ps);
		}
	}

	/**
	 * Prints the composite message and the embedded stack trace to
	 * the specified print writer <code>pw</code>
	 * @param pw the print writer
	 */
	public void printStackTrace(PrintWriter pw) {
		if (rootCause == null) {
			super.printStackTrace(pw);
		}
		else {
			//pw.println(this);
			rootCause.printStackTrace(pw);
		}
	}

}
