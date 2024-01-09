package org.springframework.web.servlet.view;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * View that redirects to an internal or external URL,
 * exposing all model attributes as HTTP query parameters.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Revision: 1.1.1.1 $
 */
public class RedirectView extends AbstractView {
	
	private String url;

	public RedirectView() {
	}
	 
	public RedirectView(String url) {
		setUrl(url);
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	protected String getUrl() {
		return url;
	}
	
	/**
	 * Subclasses can override this method to return name-value pairs for query strings,
	 * which will be URLEncoded and formatted by this class.
	 * This implementation tries to stringify all model elements.
	 */
	protected Map queryProperties(Map model) {
		return model;
	}

	/**
	 * Convert model to request parameters and redirect to url.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (getUrl() == null)
			throw new ServletException("RedirectView is not configured: URL cannot be null");
		
		StringBuffer url = new StringBuffer(getUrl());
		
		// If there are not already some parameters, we need a ?
		boolean first = (getUrl().indexOf('?') < 0);
		
		Iterator entries = queryProperties(model).entrySet().iterator();
		while (entries.hasNext()) {
			if (first) {
				url.append("?");
				first = false;
			}
			else {
				url.append("&");
			}

			Map.Entry entry = (Map.Entry) entries.next();

			url.append(URLEncoder.encode(entry.getKey().toString()));
			url.append("=");
			url.append(URLEncoder.encode(entry.getValue().toString()));
		}
		
		response.sendRedirect(response.encodeRedirectURL(url.toString()));
	}
	
}
