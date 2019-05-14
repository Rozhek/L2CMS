package studio.lineage2.cms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 Created by iRock
 29.10.2015
 */
@Entity
@Table(name = "variables")
public class Variable
{
	@Id
	@Column(name = "param", unique = true, nullable = false)
	private String param;

	@Column(name = "value")
	private long value;

	public String getParam()
	{
		return param;
	}

	public Variable()
	{
		param = "";
	}

	public Variable(String param)
	{
		this.param = param;
	}

	public long getValue()
	{
		return value;
	}

	public Variable setValue(long value)
	{
		this.value = value;
		return this;
	}
}