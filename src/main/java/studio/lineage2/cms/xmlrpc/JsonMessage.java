package studio.lineage2.cms.xmlrpc;

import com.google.gson.Gson;

public class JsonMessage{

	public enum JsonType{
		success,
		info,
		warning,
		danger
	}

	String type, text, status, location, eval;
	String title, html;
	String time;

	private JsonMessage(String type, String message, String danger, int timeOut, String eval, String location){
		this.type = type;
		this.text = message;
		this.status = danger;
		this.time = String.valueOf(timeOut);
		this.eval = eval;
		this.location = location;
	}

	private JsonMessage(String title, String html){
		this.title=title;
		this.html=html;
	}

	public static String generateMessage(JsonType type, String text){
		return generateMessage(type, text, null);
	}


	public static String generateMessage(JsonType type, String text, String mode){
		return generateMessage(type, text, mode, "msg");
	}

	public static String generateMessage(JsonType type, String text, String mode, String msgType){
		Gson gson = new Gson();
		if(mode!=null && (mode.contains(".") || mode.contains(";"))) //eval like "location.reload(true);"
			return gson.toJson(new JsonMessage(msgType, text , type.name(), 2500, mode, null));
		else if(mode!=null) // location
			return gson.toJson(new JsonMessage(msgType,  text, type.name(), type==JsonType.success? 1500 : 2500, null, mode));
		else
			return gson.toJson(new JsonMessage(msgType,  text, type.name(), 2500, null, null));
	}

	public static String formAnswerBody(String title, String html){
		Gson gson = new Gson();
		return gson.toJson(new JsonMessage(title, html));
	}
}