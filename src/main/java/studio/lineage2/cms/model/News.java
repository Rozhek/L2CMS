package studio.lineage2.cms.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Calendar;

/**
 Created by iRock
 23.10.2015
 */
@Entity
@Table(name = "news")
public class News
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "title", length = 512)
	private String title;

	@Column(name = "content", length = 10240)
	@Type(type = "org.hibernate.type.TextType")
	private String content;

	@Column(name = "link", length = 512)
	private String link;

	@Column(name = "date")
	private long date;

	@Column(name = "image", length = 1024000)
	@Lob
	private byte[] image;

	@Column(name = "lang", length = 8, nullable = false)
	@ColumnDefault("\"ru\"")
	private String lang;

	@Transient
	private static final String[] enNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	@Transient
	private static final String[] ruNames = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня", "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

	public long getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public long getDate()
	{
		return date;
	}

	public void setDate(long date)
	{
		this.date = date;
	}

	public byte[] getImage()
	{
		return image;
	}

	public void setImage(byte[] image)
	{
		this.image = image;
	}

	public String getLang()
	{
		return lang;
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}

	public String getDay()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(getDate());

		return String.valueOf(calendar.getTime().getDate());
	}
	public String getMonth()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(getDate());

		if(lang.equals("en"))
			return enNames[calendar.getTime().getMonth()];
		return ruNames[calendar.getTime().getMonth()];
	}
}