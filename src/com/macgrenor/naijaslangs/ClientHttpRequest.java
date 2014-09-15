package com.macgrenor.naijaslangs;
/**
 * <p>Title: MyJavaTools: Client HTTP Request class</p>
 * <p>Description: this class helps to send POST HTTP requests with various form data,
 * including files. Cookies can be added to be included in the request.</p>
 *
 * <p>Copyright: This is public domain;
 * The right of people to use, distribute, copy or improve the contents of the
 * following may not be restricted.</p>
 *
 * @author Vlad Patryshev, Alexei Trebounskikh
 * @version 1.4
 */
//package com.myjavatools.web;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.InputStream;
import java.util.Random;
import java.io.OutputStream;

public class ClientHttpRequest {
	HttpConnection _connection;
	OutputStream _os = null;
	InputStream _is = null;
	
	Hashtable _cookies = new Hashtable();
	String _rawCookies = "";
	
	public void closeAll() {		
		try {
			_os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			_is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			_connection = null;
			_cookies.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void connect() throws IOException {
		if (_os == null) {
			_os = _connection.openOutputStream();
		}
	}

	protected void write(char c) throws IOException {
		connect();
		_os.write(c);
	}

	protected void write(String s) throws IOException {
		connect();
		_os.write(s.getBytes());
	}

	protected void newline() throws IOException {
		connect();
		write("\r\n");
	}

	public String postAndRetrieve() {
		String s = "";
		try {
			InputStream serverInput = this.post();

			byte[] output;
			int readbytes = 0;
			int outputDefSize = 1024;

			while (true) {
				output = new byte[outputDefSize];

				readbytes = serverInput.read(output);

				if (readbytes != -1) {
					String x = new String(output, 0, readbytes);
					s += x;
				}
				else break;
			}
			
			serverInput.close();
			
			return s;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected void writeln(String s) throws IOException {
		connect();
		write(s);
		newline();
	}

	private static Random random = new Random();

	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}

	String boundary = "---------------------------" + randomString() + randomString() + randomString();

	private void boundary() throws IOException {
		write("--");
		write(boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL string
	 *
	 * @param urlString the string representation of the URL to send request to
	 * @throws IOException
	 */
	public ClientHttpRequest(String urlString) throws IOException {
		this(urlString, 0);
	}
	
	/**
	 * Creates a new multipart POST HTTP request for a specified URL string
	 *
	 * @param urlString the string representation of the URL to send request to
	 * @param timeout the timeout in milliseconds.
	 * @throws IOException
	 */
	public ClientHttpRequest(String urlString, int timeout) throws IOException {
		ConnectionFactory cfactory = new ConnectionFactory();
		cfactory.setConnectionMode(ConnectionFactory.ACCESS_READ_WRITE);
		
		if (timeout > 0) {
			cfactory.setTimeLimit(timeout);
			cfactory.setAttemptsLimit(5);
		}
		
		ConnectionDescriptor con = cfactory.getConnection(urlString);
		
		this._connection = (HttpConnection)con.getConnection();
	}

	private void postCookies() {
		StringBuffer cookieList = new StringBuffer(_rawCookies);

		for (Enumeration e = _cookies.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = _cookies.get(key);
			cookieList.append(key.toString() + "=" + value);

			if (e.hasMoreElements()) {
				cookieList.append("; ");
			}
		}
		if (cookieList.length() > 0) {
			try {
				_connection.setRequestProperty("Cookie", cookieList.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * adds a cookie to the requst
	 * @param name cookie name
	 * @param value cookie value
	 * @throws IOException
	 */
	public void setCookies(String rawCookies) throws IOException {
		this._rawCookies = (rawCookies == null) ? "" : rawCookies;
		_cookies.clear();
	}

	/**
	 * adds a cookie to the requst
	 * @param name cookie name
	 * @param value cookie value
	 * @throws IOException
	 */
	public void setCookie(String name, String value) throws IOException {
		_cookies.put(name, value);
	}

	/**
	 * adds cookies to the request
	 * @param cookies the cookie "name-to-value" Hashtable
	 * @throws IOException
	 */
	public void setCookies(Hashtable cookies) throws IOException {
		if (cookies == null) return;
		
		for (Enumeration e = cookies.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = cookies.get(key);
			_cookies.put(key, value);
		}
	}

	/**
	 * adds cookies to the request
	 * @param cookies array of cookie names and values (cookies[2*i] is a name, cookies[2*i + 1] is a value)
	 * @throws IOException
	 */
	public void setCookies(String[] cookies) throws IOException {
		if (cookies == null) return;
		for (int i = 0; i < cookies.length - 1; i+=2) {
			setCookie(cookies[i], cookies[i+1]);
		}
	}

	private void writeName(String name) throws IOException {
		newline();
		write("Content-Disposition: form-data; name=\"");
		write(name);
		write('"');
	}

	/**
	 * adds a string parameter to the request
	 * @param name parameter name
	 * @param value parameter value
	 * @throws IOException
	 */
	public void setParameter(String name, String value) throws IOException {
		boundary();
		writeName(name);
		newline(); newline();
		writeln(value);
	}

	private void pipe(InputStream in) throws IOException {
		byte[] buf = new byte[500000];
		int nread;
		int total = 0;

		synchronized (in) {
			while((nread = in.read(buf, 0, buf.length)) >= 0) {
				_os.write(buf, 0, nread);
				total += nread;
			}
		}
		_os.flush();
		buf = null;
	}

	/**
	 * adds a file parameter to the request
	 * @param name parameter name
	 * @param filename the name of the file
	 * @param is input stream to read the contents of the file from
	 * @throws IOException
	 */
	public void setParameter(String name, String filename, InputStream is) throws IOException {
		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		String type = MIMETypeAssociations.getMIMEType(filename);
		if (type == null) type = "application/octet-stream";
		writeln(type);
		newline();
		pipe(is);
		newline();
	}

	/**
	 * adds a file parameter to the request
	 * @param name parameter name
	 * @param file the file to upload
	 * @throws IOException
	 */
	public void setParameter(String name, FileConnection file) throws IOException {
		setParameter(name, file.getPath(), file.openInputStream());
	}

	/**
	 * adds a parameter to the request; if the parameter is a File, the file is uploaded, otherwise the string value of the parameter is passed in the request
	 * @param name parameter name
	 * @param object parameter value, a File or anything else that can be stringified
	 * @throws IOException
	 */
	public void setParameter(String name, Object object) throws IOException {
		if (object instanceof FileConnection) {
			setParameter(name, (FileConnection) object);
		} else {
			setParameter(name, object.toString());
		}
	}

	/**
	 * adds parameters to the request
	 * @param parameters "name-to-value" Hashtable of parameters; if a value is a file, the file is uploaded, otherwise it is stringified and sent in the request
	 * @throws IOException
	 */
	public void setParameters(Hashtable parameters) throws IOException {
		if (parameters != null) {
			for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				Object value = parameters.get(key);
				setParameter(key.toString(), value);
			}
		}
	}

	/**
	 * adds parameters to the request
	 * @param parameters array of parameter names and values (parameters[2*i] is a name, parameters[2*i + 1] is a value); if a value is a file, the file is uploaded, otherwise it is stringified and sent in the request
	 * @throws IOException
	 */
	public void setParameters(Object[] parameters) throws IOException {
		if (parameters != null) {
			for (int i = 0; i < parameters.length - 1; i += 2) {
				setParameter(parameters[i].toString(), parameters[i + 1]);
			}
		}
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added
	 * @return input stream with the server response
	 * @throws IOException
	 */
	private InputStream doPost() throws IOException {
		boundary();
		writeln("--");
		_os.close();

		_is = _connection.openInputStream();
		return _is;
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post() throws IOException {
		postCookies();
		return doPost();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with parameters that are passed in the argument
	 * @param parameters request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameters
	 */
	public InputStream post(Hashtable parameters) throws IOException {
		postCookies();
		setParameters(parameters);
		return doPost();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with parameters that are passed in the argument
	 * @param parameters request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameters
	 */
	public InputStream post(Object[] parameters) throws IOException {
		postCookies();
		setParameters(parameters);
		return doPost();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with cookies and parameters that are passed in the arguments
	 * @param cookies request cookies
	 * @param parameters request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameters
	 * @see setCookies
	 */
	public InputStream post(Hashtable cookies, Hashtable parameters) throws IOException {
		setCookies(cookies);
		postCookies();
		setParameters(parameters);
		return doPost();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with cookies and parameters that are passed in the arguments
	 * @param cookies request cookies
	 * @param parameters request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameters
	 * @see setCookies
	 */
	public InputStream post(String raw_cookies, Hashtable parameters) throws IOException {
		setCookies(raw_cookies);
		postCookies();
		setParameters(parameters);
		return doPost();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with cookies and parameters that are passed in the arguments
	 * @param cookies request cookies
	 * @param parameters request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameters
	 * @see setCookies
	 */
	public InputStream post(String[] cookies, Object[] parameters) throws IOException {
		setCookies(cookies);
		postCookies();
		setParameters(parameters);
		return doPost();
	}

	/**
	 * post the POST request to the server, with the specified parameter
	 * @param name parameter name
	 * @param value parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name, Object value) throws IOException {
		postCookies();
		setParameter(name, value);
		return doPost();
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name1, Object value1, String name2, Object value2) throws IOException {
		postCookies();
		setParameter(name1, value1);
		setParameter(name2, value2);
		return doPost();
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @param name3 third parameter name
	 * @param value3 third parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name1, Object value1, String name2, Object value2, String name3, Object value3) throws IOException {
		postCookies();
		setParameter(name1, value1);
		setParameter(name2, value2);
		setParameter(name3, value3);
		return doPost();
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @param name3 third parameter name
	 * @param value3 third parameter value
	 * @param name4 fourth parameter name
	 * @param value4 fourth parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4) throws IOException {
		postCookies();
		setParameter(name1, value1);
		setParameter(name2, value2);
		setParameter(name3, value3);
		setParameter(name4, value4);
		return doPost();
	}

}
