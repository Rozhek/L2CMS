package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 Created by iRock
 04.11.2015
 */
@Entity
@Table(name = "user_items", indexes = { @Index(name = "idx_userItem", columnList = "user_id") })
public class UserItem
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "user_id")
	private long userId;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "item_id")
	private int itemId;

	@Column(name = "item_count")
	private long itemCount;

	public UserItem()
	{
	}

	public long getId()
	{
		return id;
	}

	public long getUserId()
	{
		return userId;
	}

	public void setUserId(long userId)
	{
		this.userId = userId;
	}

	public String getItemName()
	{
		return itemName;
	}

	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}

	public int getItemId()
	{
		return itemId;
	}

	public void setItemId(int itemId)
	{
		this.itemId = itemId;
	}

	public long getItemCount()
	{
		return itemCount;
	}

	public void setItemCount(long itemCount)
	{
		this.itemCount = itemCount;
	}
}