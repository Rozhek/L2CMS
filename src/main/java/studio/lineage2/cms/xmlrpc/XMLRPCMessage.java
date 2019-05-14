package studio.lineage2.cms.xmlrpc;

/**
 Created by iRock
 26.10.2015
 */
public class XMLRPCMessage
{
	private MessageType type;
	private String message;

	public XMLRPCMessage(MessageType type)
	{
		this.type = type;
		this.message = "";
	}

	public XMLRPCMessage(MessageType type, String message)
	{
		this.type = type;
		this.message = message;
	}

	public MessageType getType()
	{
		return type;
	}

	public String getMessage()
	{
		return message;
	}

	public enum MessageType
	{
		OK,
		FAIL
	}
}