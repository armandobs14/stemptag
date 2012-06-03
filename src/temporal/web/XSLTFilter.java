package temporal.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Servlet filter that should emulate client side browser xsl transformations in
 * server. It looks for &lt;xml-stylesheet&gt; processing instruction in
 * the original response for the path to the stylesheet. 
 * 
 * The path to the stylesheet is assumed to be relative
 * to the url that triggered this filter.
 */
public class XSLTFilter implements Filter {

	private static final String STYLESHEET_BEGIN_MARK = "href=\"";
	
	private FilterConfig config;

	/**
	 * Method that implements this filter's main function.
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		CharResponseWrapper wrapper = new CharResponseWrapper((HttpServletResponse) response);
		filterChain.doFilter(request, wrapper);
		String content = wrapper.toString();
		if (content.length() == 0) {
			return;
		}
		String stylesheetPath = config.getInitParameter("stylesheetPath");
		if ( stylesheetPath==null || stylesheetPath.trim().length()==0 ) {
			if (content.indexOf("<?xml-stylesheet")<0) {
				out.write(content);
				return;
			} 
			int indexOfStylesheetFilePathBegin = content.indexOf(STYLESHEET_BEGIN_MARK) + STYLESHEET_BEGIN_MARK.length();
			int indexOfStylesheetFilePathEnd = content.indexOf("\"", indexOfStylesheetFilePathBegin);
			stylesheetPath = content.substring( indexOfStylesheetFilePathBegin, indexOfStylesheetFilePathEnd);
		}
		int indexOfRequestPath = httpRequest.getContextPath().length()+1;
		String requestPath = httpRequest.getRequestURI().substring(indexOfRequestPath);
		WebContextUriResolver webContextUriResolver = new WebContextUriResolver(request, (HttpServletResponse) response);
		StringReader sr = new StringReader(content);
		String requestSystemId = WebContextUriResolver.SUPPORTED_SCHEMA+requestPath;
		Source xmlSource = new StreamSource(sr, requestSystemId);		
		Source stylesheetSource;
		try {
			stylesheetSource = webContextUriResolver.resolve(stylesheetPath, requestSystemId);
		} catch (TransformerException e) {
			throw new RuntimeException("Exception while resolving stylesheet's URI", e);
		}
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setURIResolver(webContextUriResolver);
			Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setURIResolver(webContextUriResolver);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			transformer.transform(xmlSource, result);
			String resultString = sw.toString();
			response.setContentLength(resultString.length());
			out.write(resultString);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Exception during XSL transformation.", e);
		} catch (TransformerException e) {
			throw new RuntimeException("Exception during XSL transformation.", e);
		}
	}

	@Override
	public void destroy() {
		this.config = null;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

}