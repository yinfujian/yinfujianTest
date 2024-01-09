package org.springframework.remoting.support;

/**
 * Abstract subclass of RemoteProxyFactoryBean,
 * adding support for authorization via username and password.
 * @author Juergen Hoeller
 * @since 13.05.2003
 */
public abstract class AuthorizableRemoteProxyFactoryBean extends RemoteProxyFactoryBean {

	private String username;

	private String password;

	/**
	 * Set the username that this factory should use to access the backend service.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	protected String getUsername() {
		return username;
	}

	/**
	 * Set the password that this factory should use to access the backend service.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	protected String getPassword() {
		return password;
	}

}
