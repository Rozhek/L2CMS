package studio.lineage2.cms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 Created by iRock
 27.10.2015
 */
@Entity
@Table(name = "players", indexes = { @Index(name = "idx_account", columnList = "account_id"), @Index(name = "idx_name", columnList = "name") })
public class Player
{
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "account_id")
	private long accountId;

	@Column(name = "name")
	private String name;

	@Column(name = "charclass")
	private String charclass;

	@Column(name = "level")
	private int level;

	@Column(name = "pvp")
	private int pvp;

	@Column(name = "pk")
	private int pk;

	@Column(name = "karma")
	private int karma;

	@Column(name = "lastaccess")
	private long lastaccess;

	@Column(name = "hwid", nullable = false)
	private boolean hwid = false;

	public Player()
	{
	}

	public Player(long id, long accountId, String name, String charclass, int level, int pvp, int pk, int karma, long lastaccess, boolean hwid)
	{
		this.id = id;
		this.accountId = accountId;
		this.name = name;
		this.charclass = charclass;
		this.level = level;
		this.pvp = pvp;
		this.pk = pk;
		this.karma = karma;
		this.lastaccess = lastaccess;
		this.hwid = hwid;
	}

	public long getId()
	{
		return id;
	}

	public long getAccountId()
	{
		return accountId;
	}

	public String getName()
	{
		return name;
	}

	public String getCharClass()
	{
		return charclass;
	}

	public int getLevel()
	{
		return level;
	}

	public int getPvp()
	{
		return pvp;
	}

	public int getPk()
	{
		return pk;
	}

	public int getKarma()
	{
		return karma;
	}

	public long getLastAccess()
	{
		return lastaccess;
	}

	public void setAccountId(long accountId)
	{
		this.accountId = accountId;
	}

	public void setName(String name)
	{
		this.name = name ;
	}

	public void setCharClass(String charclass)
	{
		this.charclass = charclass ;
	}

	public void setLevel(int level)
	{
		this.level = level ;
	}

	public void setPvp(int pvp)
	{
		this.pvp = pvp;
	}

	public void setPk(int pk)
	{
		this.pk = pk;
	}

	public void setKarma(int karma)
	{
		this.karma = karma;
	}

	public void setLastAccess(long lastaccess)
	{
		this.lastaccess = lastaccess;
	}

	public void setHwid(boolean hwid)
	{
		this.hwid = hwid;
	}

	public boolean isHwidLock()
	{
		return hwid;
	}


	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//TODO localize date format

	public String getTime()
	{
		if(lastaccess == 0)
			return "";
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));//TODO get timezone from browser

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(lastaccess * 1000L);

		return dateFormat.format(calendar.getTime());
	}
}