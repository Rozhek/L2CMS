package studio.lineage2.cms.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 Created by iRock
 30.10.2015
 */
public enum XMLRpcServer
{
	LOGIN,
	GAME;

	private URL url;
	private String username;
	private String password;
	private boolean connect;

	public void setUrl(String url)
	{
		try
		{
			this.url = new URL(url);
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public URL getUrl()
	{
		return url;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public boolean isConnected()
	{
		return connect;
	}

	public void setConnected(boolean value)
	{
		connect = value;
	}
}