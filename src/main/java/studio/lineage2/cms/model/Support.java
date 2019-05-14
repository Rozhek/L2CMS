package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 Created by iRock
 02.11.2015
 */
@Entity
@Table(name = "support")
public class Support
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "title")
	private String title;

	@OneToMany(mappedBy = "supportId")
	@OrderBy("id asc")
	private List<SupportMessage> messages = new ArrayList<>();

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private SupportType type;

	@Column(name = "prioritet")
	@Enumerated(EnumType.STRING)
	private SupportPrioritet prioritet;

	public Support()
	{
	}

	public long getId()
	{
		return id;
	}

	public long getAuthorId()
	{
		return messages.get(0).getAuthorId();
	}

	public String getTitle()
	{
		if(title.length() > 25)
			return title.substring(0, 25);

		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContent()
	{
		String label;
		label = messages.get(0).getContent().replace("<br>", " ");

		if(label.length()>50)
			label = label.substring(0, 50);

		return label;
	}

	public long getDate()
	{
		return messages.get(0).getDate();
	}

	public long getMsgCount()
	{
		return messages.size();
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public SupportType getType()
	{
		return type;
	}

	public String getGridLabel()
	{
		switch(type)
		{
			case NEW:
				return "warning";
			case REPLY:
			case WAIT:
				return "success";
			case FORDEVELOPER:
			case FORTESTER:
				return "info";
			case CLOSE:
				return "danger";
		}
		return "";
	}

	public String getMessageLabel()
	{
		switch(type)
		{
			case NEW:
			case FORDEVELOPER:
			case FORTESTER:
			case CLOSE:
				return "default";
			case REPLY:
				return "warning";
			case WAIT:
				return "success";
		}
		return "";
	}

	public void setType(SupportType type)
	{
		this.type = type;
	}

	public SupportPrioritet getPrioritet()
	{
		return prioritet;
	}

	public void setPrioritet(SupportPrioritet prioritet)
	{
		this.prioritet = prioritet;
	}

	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	public String getTime()
	{
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(getDate());

		return dateFormat.format(calendar.getTime());
	}
}