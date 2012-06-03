package temporal.web;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CharResponseWrapper extends HttpServletResponseWrapper {

	private CharArrayWriter output = null;
	
	public CharResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new CharArrayWriter();
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(output);//maybe this should be created in constructor?
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		throw new IllegalStateException("Only getWriter() can be used on this wrapper.");
	}
	
	public String toString() {
		return output.toString();
	}
	
	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub
		super.addCookie(cookie);
	}

	@Override
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		super.addDateHeader(name, date);
	}

	@Override
	public void addHeader(String name, String value) {
		// TODO Auto-generated method stub
		super.addHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		super.addIntHeader(name, value);
	}

	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return super.encodeRedirectUrl(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return super.encodeRedirectURL(url);
	}

	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return super.encodeUrl(url);
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return super.encodeURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		super.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		super.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// TODO Auto-generated method stub
		super.sendRedirect(location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		super.setDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub
		super.setHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		super.setIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc, String sm) {
		// TODO Auto-generated method stub
		super.setStatus(sc, sm);
	}

	@Override
	public void setStatus(int sc) {
		// TODO Auto-generated method stub
		super.setStatus(sc);
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		super.flushBuffer();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		super.resetBuffer();
	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub
		super.setBufferSize(size);
	}

	@Override
	public void setContentLength(int len) {
		// TODO Auto-generated method stub
		super.setContentLength(len);
	}

	@Override
	public void setContentType(String type) {
		// TODO Auto-generated method stub
		super.setContentType(type);
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO Auto-generated method stub
		super.setCharacterEncoding(charset);
	}

	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub
		super.setLocale(loc);
	}

	@Override
	public void setResponse(ServletResponse response) {
		// TODO Auto-generated method stub
		super.setResponse(response);
	}
	
}