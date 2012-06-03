package temporal.web;

import java.io.IOException;
import java.io.StringReader;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FilenameUtils;

public class WebContextUriResolver implements URIResolver {

	public static final String SUPPORTED_SCHEMA = "resource://";
	
	private static final String ABSOLUTE_PATH_PREFIX = "/";
	
	private ServletRequest request;
	
	private HttpServletResponse response;
	
	public WebContextUriResolver(ServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		String hrefAbsolutePath;
		if (href.startsWith(ABSOLUTE_PATH_PREFIX)) {
			hrefAbsolutePath = href;
		} else {
			if (base == null) throw new RuntimeException("Resolving relative path without a base is not possible.");
			if (!base.startsWith(SUPPORTED_SCHEMA)) {return null;}
			String baseWithoutSchema = base.substring(SUPPORTED_SCHEMA.length());
			hrefAbsolutePath = FilenameUtils.getPath(baseWithoutSchema)+href;
			hrefAbsolutePath = ABSOLUTE_PATH_PREFIX + FilenameUtils.normalize(hrefAbsolutePath);
		}
		CharResponseWrapper responseWrapper = new CharResponseWrapper(response);
		try {
			request.getRequestDispatcher(hrefAbsolutePath).forward(request, responseWrapper);
		} catch (ServletException e) {
			throw new RuntimeException("Exception while getting the resolved content", e);
		} catch (IOException e) {
			throw new RuntimeException("Exception while getting the resolved content", e);
		}
		String systemId = SUPPORTED_SCHEMA+hrefAbsolutePath.substring(ABSOLUTE_PATH_PREFIX.length());
		StreamSource streamSource = new StreamSource(new StringReader(responseWrapper.toString()), systemId);		
		return streamSource;
	}

}