package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 Created by iRock
 27.10.2015
 */
@Entity
@Table(name = "accounts")
public class Account
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "user_id")
	private long userId;

	@Column(name = "account")
	private String account;

	public Account()
	{
	}

	public Account(long userId, String account)
	{
		this.userId = userId;
		this.account = account;
	}

	public long getId()
	{
		return id;
	}

	public long getUserId()
	{
		return userId;
	}

	public String getAccount()
	{
		return account;
	}
}