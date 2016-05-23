package com.chaofan188.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtil 
{
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_GET = "GET";
	private static final int CONCECTTIMEOUT = 5000;
	private static final int READTIMEOUT = 5000;
	
	private static class DefaultTrustManager implements X509TrustManager
	{

		
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException 
		{
			
			
		}

		
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static HttpURLConnection getConnection(URL url, String method, String ctype)
	throws IOException
	{
		HttpURLConnection conn = null;
		if ("https".equals(url.getProtocol()))
		{
			SSLContext ctx = null;
			try
			{
				ctx = SSLContext.getInstance("TLS");
				ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
			}catch(Exception e)
			{
				throw new IOException(e);
			}
			HttpsURLConnection connHttps = (HttpsURLConnection)url.openConnection();
			connHttps.setSSLSocketFactory(ctx.getSocketFactory());
			connHttps.setHostnameVerifier(new HostnameVerifier()
			{

				
				public boolean verify(String hostname, SSLSession session) {
					
					return true;
				}
				
			}
					);
			conn = connHttps;
		}
		else
		{
			conn = (HttpURLConnection)url.openConnection();
		}
		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("User-Agent", "bupt");
		conn.setRequestProperty("Content-Type", ctype);
		conn.setRequestProperty("Connection", "Keep-Alive");
		return conn;
	}
	
	public static String doGet(String url, Map<String, String> params) throws IOException
	{
		return doGet(url,params, DEFAULT_CHARSET);
	}
	public static String doGet(String url, Map<String, String> params, String charset) throws IOException
	{
		if (isEmpty(url) || params == null)
		{
			return null;
		}
		String response = "";
		url += "?" + buildQuery(params, charset);
		HttpURLConnection conn = null;
		String ctype = "application/x-www-form-urlencoded;charset=" + charset;
		conn = getConnection(new URL(url), METHOD_GET, ctype);
		response = getResponseAsString(conn);
		return response;
	}
	 public static String doPost(String url, Map<String, String> params, int connectTimeOut,
	            int readTimeOut) throws IOException {
	        return doPost(url, params, DEFAULT_CHARSET, connectTimeOut, readTimeOut);
	    }
	
	public static String doPost(String url, Map<String, String> params, String charset,
            int connectTimeOut, int readTimeOut) throws IOException 
	{
		HttpURLConnection conn = null;
		String response = "";
		String ctype = "application/x-www-form-urlencoded;charset=" + charset;
		conn = getConnection(new URL(url), METHOD_POST, ctype);
		conn.setConnectTimeout(connectTimeOut);
		 conn.setReadTimeout(readTimeOut);
		 conn.getOutputStream().write(buildQuery(params, charset).getBytes());
		 response = getResponseAsString(conn);
		 return response;
	
	}

	private static String buildQuery(Map<String, String> params, String charset) 
	{
		if (params == null || params.isEmpty())
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Entry<String, String> entry : params.entrySet())
		{
			if (first)
			{
				first = false;
			}else
			{
				sb.append("&");
			}
			String key = entry.getKey();
			String value = entry.getValue();
			try
			{
				sb.append(key).append("=").append(URLEncoder.encode(value, charset));
			}catch(UnsupportedEncodingException e){}
		}
		
		return sb.toString();
	}

	private static String getResponseAsString(HttpURLConnection conn) throws IOException 
	{
		String charset = getResponseCharset(conn.getContentType());
		InputStream es = conn.getErrorStream();
		if (es == null)
		{
			return getStreamAsString(conn.getInputStream(), charset);
		}
		else
		{
			String msg = getStreamAsString(es, charset);
			if (isEmpty(msg))
			{
				throw new IOException(conn.getResponseCode() + " : " + conn.getResponseMessage());
			}
			else
			{
				throw new IOException(msg);
			}
		}
		
		
	}

	private static boolean isEmpty(String str) 
	{
		if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
	}

	private static String getResponseCharset(String ctype) 
	{
		String charset = DEFAULT_CHARSET;
		if (!isEmpty(ctype))
		{
			String[] params = ctype.split("\\;");
			for (String param : params)
			{
				param = param.trim();
				if (param.startsWith("charset"))
				{
					String[] pair = param.split("\\=");
					if (pair.length == 2)
					{
						charset = pair[1].trim();
					}
				
				}
			}
		}
		return charset;
	}

	private static String getStreamAsString(InputStream input,
			String charset) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader bf = null;
		try
		{
			bf = new BufferedReader(new InputStreamReader(input, charset));
			String str;
			while ((str = bf.readLine()) != null)
			{
				sb.append(str);
			}
			return sb.toString();
		}finally
		{
			if (bf != null)
			{
				bf.close();
				bf = null;
			}
		}
		
		
	}

}
