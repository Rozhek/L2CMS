package studio.lineage2.cms.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 Created by iRock
 29.10.2015
 */
@Entity
@Table(name = "block")
public class Block
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "type", nullable = false)
	@ColumnDefault("0")
	private int type;

	@Column(name = "background", length = 512)
	private String background;

	@Column(name = "title", length = 512)
	private String title;

	@Column(name = "text", length = 10240)
	private String text;

	@Column(name = "src", length = 512)
	private String src;

	@Column(name = "lang", length = 8)
	private String lang;

	public long getId()
	{
		return id;
	}

	public String getBackground()
	{
		return background;
	}

	public void setBackground(String background)
	{
		this.background = background;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public int getType(){return type;}

	public void setType(int type){this.type = type;}

	public String getSrc(){return src;}

	public void setSrc(String src){this.src = src;}

	public String getLang()
	{
		return lang;
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}
}