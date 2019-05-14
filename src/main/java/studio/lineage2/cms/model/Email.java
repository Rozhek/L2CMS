package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 Created by iRock
 01.12.2015
 */
@Entity
@Table(name = "emails")
public class Email
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "email")
	private String email;

	@Column(name = "send")
	private boolean send;

	public Email()
	{
	}

	public Email(String email)
	{
		this.email = email;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public boolean isSend()
	{
		return send;
	}

	public void setSend(boolean send)
	{
		this.send = send;
	}
}