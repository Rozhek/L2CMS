package studio.lineage2.cms.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 Created by iRock
 29.10.2015
 */
public class Theme
{
	private int topicId;
	private String title;
	private String date;
	private int userId;
	private String name;
	private long avatarDate;
	private String gender;

	public Theme(int topicId, String title, int date, int userId, String name, long avatarDate, String gender)
	{
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("GMT+3"));

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date * 1000L);

		this.topicId = topicId;
		this.title = title.length() > 50 ? title.substring(0, 50) + "..." : title;
		this.date = format.format(calendar.getTime());
		this.userId = userId;
		this.name = name;
		this.avatarDate = avatarDate;
		this.gender = gender;
	}

	public int getTopicId()
	{
		return topicId;
	}

	public void setTopicId(int topicId)
	{
		this.topicId = topicId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public int getUserId()
	{
		return userId;
	}

	public void setUserId(int userId)
	{
		this.userId = userId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getAvatarDate()
	{
		return avatarDate;
	}

	public void setAvatarDate(long avatarDate)
	{
		this.avatarDate = avatarDate;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public String getGender()
	{
		return gender;
	}
}