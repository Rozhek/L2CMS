package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 Created by iRock
 04.11.2015
 */
@Entity
@Table(name = "vote_mmotop")
public class VoteMMOTOP
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "vote_id", unique = true, nullable = false)
	private long voteId;

	@Column(name = "name")
	private String name;

	@Column(name = "ip")
	private String ip;

	@Column(name = "is_take")
	private boolean isTake;

	public VoteMMOTOP()
	{
	}

	public long getId()
	{
		return id;
	}

	public long getVoteId()
	{
		return voteId;
	}

	public void setVoteId(long voteId)
	{
		this.voteId = voteId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isTake()
	{
		return isTake;
	}

	public void setTake(boolean take)
	{
		isTake = take;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}
}