package temporal.web;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.*;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


public class XSLTOutputStreamFilter implements Filter {
    private FilterConfig filterConfig;
    private String xsltFileName;

    /**
     * This method is called once when the filter is first loaded.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        // xsltPath should be something like "/WEB-INF/xslt/a.xslt"
        this.xsltFileName = filterConfig.getInitParameter("xsltPath");
        if (this.xsltFileName == null) {
            throw new UnavailableException(
                    "xsltPath is a required parameter. Please "
                    + "check the deployment descriptor.");
        }

  /*      // convert the context-relative path to a physical path name
        this.xsltFileName = filterConfig.getServletContext( )
                .getRealPath(this.xsltFileName);

        // verify that the file exists
        if (this.xsltFileName == null ||
                !new File(this.xsltFileName).exists( )) {
            throw new UnavailableException(
                    "Unable to locate stylesheet: " + this.xsltFileName, 30);
        }*/
    }

    public void doFilter (ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        if (!(res instanceof HttpServletResponse)) {
            throw new ServletException("This filter only supports HTTP");
        }

        BufferedHttpResponseWrapper responseWrapper =
                new BufferedHttpResponseWrapper((HttpServletResponse) res);
        chain.doFilter(req, responseWrapper);

        // Tomcat 4.0 reuses instances of its HttpServletResponse
        // implementation class in some scenarios. For instance, hitting
        // reload( ) repeatedly on a web browser will cause this to happen.
        // Unfortunately, when this occurs, output is never written to the
        // BufferedHttpResponseWrapper's OutputStream. This means that the
        // XML output array is empty when this happens. The following
        // code is a workaround:
        byte[] origXML = responseWrapper.getBuffer( );
        if (origXML == null || origXML.length == 0) {
            // just let Tomcat deliver its cached data back to the client
            chain.doFilter(req, res);
            return;
        }

        try {
            // do the XSLT transformation
        	
     /*   	FileInputStream xsltFile = new FileInputStream(this.xsltFileName);
        	Source xslSource = new StreamSource(xsltFile);
            TransformerFactory transFact = TransformerFactory.newInstance( );
            Templates templates = transFact.newTemplates(xslSource);        	
            Transformer trans = templates.newTransformer( );*/
            
            InputStream in = null;
        	
        	in = this.getClass().getResourceAsStream(this.xsltFileName);
            if (in == null) {
                String msg = "Could not open stream for resource="
                    + this.xsltFileName;
                throw new IOException(msg);
            }
            
            
       //     bufIn = new BufferedInputStream(in);
        	StreamSource xslSource = new StreamSource(in);

            TransformerFactory transFact = TransformerFactory.newInstance( );
            Templates templates = transFact.newTemplates(xslSource);
            Transformer trans = templates.newTransformer();
        	
        	
        	
      //      Transformer trans = StylesheetCache.newTransformer(
      //              this.xsltFileName);

            ByteArrayInputStream origXMLIn = new ByteArrayInputStream(origXML);
            StreamSource xmlSource = new StreamSource(origXMLIn);

            ByteArrayOutputStream resultBuf = new ByteArrayOutputStream( );
            trans.transform(xmlSource, new StreamResult(resultBuf));

            res.setContentLength(resultBuf.size( ));
            res.setContentType("text/html");
            res.getOutputStream().write(resultBuf.toByteArray( ));
            res.flushBuffer( );
        } catch (TransformerException te) {
            throw new ServletException(te);
        }catch (Exception e ) {
        	e.printStackTrace(); 
        }
    }

    /**
     * The counterpart to the init( ) method.
     */
    public void destroy( ) {
        this.filterConfig = null;
    }
}