package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 Created by iRock
 01.11.2015
 */
@Entity
@Table(name = "users_ip", indexes = { @Index(name = "idx_userIp", columnList = "user_id") })
public class UserIp
{
	public enum Action{
		AUTHORIZE(0, "logging.authorization"),
		PASSWORDCHANGE(1, "logging.password_change"),
		RECOVER(2, "logging.recover"),
		ACCOUNT_CREATE(3, "logging.account_create"),
		ACCOUNT_CHANGEPASSWORD(4, "logging.account_changepassword"),
		ACCOUNT_RECOVER(5, "logging.account_recover"),
		ACTIVATE(6, "logging.activate");

		private int id;
		private String name;

		Action(int id, String name)
		{
			this.id = id;
			this.name = name;
		}

		private String getName(){
			return name;
		}
	}

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "user_id")
	private long userId;

	@Column(name = "ip")
	private String ip;

	@Column(name = "date")
	private long date;

	@Column(name = "action")
	private long action;

	@Column(name = "success")
	private boolean success;

	@Column(name = "browser")
	private String browser;

	public UserIp()
	{
	}

	public UserIp(long userId, String ip, long date, Action action, boolean success, String browser)
	{
		this.userId = userId;
		this.ip = ip;
		this.date = date;
		this.action = action.ordinal();
		this.success = success;
		this.browser = browser;
	}

	public UserIp(long userId, String ip, long date, long action, boolean success, String browser)
	{
		this.userId = userId;
		this.ip = ip;
		this.date = date;
		this.action = action;
		this.success = success;
		this.browser = browser;
	}

	public long getId()
	{
		return id;
	}

	public long getUserId()
	{
		return userId;
	}

	public void setUserId(long userId)
	{
		this.userId = userId;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public long getDate()
	{
		return date;
	}

	public void setDate(long date)
	{
		this.date = date;
	}

	public String getType()
	{
		return success ? "success" : "danger";
	}

	public String getBrowser()
	{
		return browser;
	}

	public long getAction()
	{
		return action;
	}

	public String getActionText()
	{
		if(action>=Action.values().length)
			return null;
		return Action.values()[(int)action].getName();
	}

	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public String getTime()
	{
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);

		return dateFormat.format(calendar.getTime());
	}
}