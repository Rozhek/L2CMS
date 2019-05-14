package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 Created by iRock
 03.11.2015
 */
@Entity
@Table(name = "support_messages")
public class SupportMessage
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "support_id")
	private long supportId;

	@Column(name = "author_id")
	private long authorId;

	@Column(name = "content", length = 10240)
	@Type(type = "org.hibernate.type.TextType")
	private String content;

	@Column(name = "date")
	private long date;

	@Column(name = "post_is_admin")
	private boolean postIsAdmin;

	public SupportMessage()
	{
	}

	public long getId()
	{
		return id;
	}

	public long getSupportId()
	{
		return supportId;
	}

	public void setSupportId(long supportId)
	{
		this.supportId = supportId;
	}

	public long getAuthorId()
	{
		return authorId;
	}

	public void setAuthorId(long authorId)
	{
		this.authorId = authorId;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public long getDate()
	{
		return date;
	}

	public void setDate(long date)
	{
		this.date = date;
	}

	public boolean isPostIsAdmin()
	{
		return postIsAdmin;
	}

	public void setPostIsAdmin(boolean postIsAdmin)
	{
		this.postIsAdmin = postIsAdmin;
	}

	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	public String getTime()
	{
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);

		return dateFormat.format(calendar.getTime());
	}
}