package studio.lineage2.cms.utils;

import com.google.gson.Gson;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import studio.lineage2.cms.xmlrpc.XMLRPCMessage;

import java.lang.reflect.Type;

/**
 Created by iRock
 23.11.2015
 */
@Component
public class XMLRpcUtil
{
	@Value("${xmlrpcLUrl}")
	private String xmlrpcLUrl;

	@Value("${xmlrpcLLogin}")
	private String xmlrpcLLogin;

	@Value("${xmlrpcLPassword}")
	private String xmlrpcLPassword;

	@Value("${xmlrpcGUrl}")
	private String xmlrpcGUrl;

	@Value("${xmlrpcGLogin}")
	private String xmlrpcGLogin;

	@Value("${xmlrpcGPassword}")
	private String xmlrpcGPassword;

	@Scheduled(initialDelay = 1000, fixedRate = 24 * 60 * 60 * 1000)
	public void load()
	{
		if(!xmlrpcLUrl.isEmpty())
		{
			XMLRpcServer.LOGIN.setUrl(xmlrpcLUrl);
			XMLRpcServer.LOGIN.setUsername(xmlrpcLLogin);
			XMLRpcServer.LOGIN.setPassword(xmlrpcLPassword);
		}

		if(!xmlrpcGUrl.isEmpty())
		{
			XMLRpcServer.GAME.setUrl(xmlrpcGUrl);
			XMLRpcServer.GAME.setUsername(xmlrpcGLogin);
			XMLRpcServer.GAME.setPassword(xmlrpcGPassword);
		}
	}

	@Scheduled(initialDelay = 1000, fixedRate = 30 * 1000L)
	public void checkConnect()
	{
		XMLRPCMessage answer;
		if(!xmlrpcLUrl.isEmpty())
		{
			answer = XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "WorldService.idle");
			XMLRpcServer.LOGIN.setConnected(answer != null && answer.getType()== XMLRPCMessage.MessageType.OK);
		}

		if(!xmlrpcGUrl.isEmpty())
		{
			answer = XMLRpcUtil.send(XMLRpcServer.GAME, XMLRPCMessage.class, "WorldService.idle");
			XMLRpcServer.GAME.setConnected(answer != null && answer.getType()== XMLRPCMessage.MessageType.OK);

		}

	}

	public static <T> T send(XMLRpcServer xmlRpcServer, Class<T> type, String method, Object... objects)
	{
		if(xmlRpcServer==null || !xmlRpcServer.isConnected() && !method.equals("WorldService.idle"))
			return null;
		Gson gson = new Gson();
		try
		{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(xmlRpcServer.getUrl());
			config.setBasicUserName(xmlRpcServer.getUsername());
			config.setBasicPassword(xmlRpcServer.getPassword());
			config.setConnectionTimeout(30000);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			String result = (String) client.execute(method, objects);
			return gson.fromJson(result, type);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static <T> T send(XMLRpcServer xmlRpcServer, Type type, String method, Object... objects)
	{
		if(xmlRpcServer==null || !xmlRpcServer.isConnected() && !method.equals("WorldService.idle"))
			return null;
		Gson gson = new Gson();
		try
		{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(xmlRpcServer.getUrl());
			config.setBasicUserName(xmlRpcServer.getUsername());
			config.setBasicPassword(xmlRpcServer.getPassword());
			config.setConnectionTimeout(30000);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			String result = (String) client.execute(method, objects);
			return gson.fromJson(result, type);
		}
		catch(Exception e)
		{
			return null;
		}
	}
}