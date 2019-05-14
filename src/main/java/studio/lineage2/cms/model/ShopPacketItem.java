package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 Created by iRock
 05.11.2015
 */
@Entity
@Table(name = "shop_packet_items")
public class ShopPacketItem
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "packet_id")
	private long packetId;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "item_id")
	private int itemId;

	@Column(name = "item_count")
	private long itemCount;

	@Column(name = "image")
	private byte[] image;

	public ShopPacketItem()
	{
	}

	public long getId()
	{
		return id;
	}

	public long getPacketId()
	{
		return packetId;
	}

	public void setPacketId(long packetId)
	{
		this.packetId = packetId;
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

	public byte[] getImage()
	{
		return image;
	}

	public void setImage(byte[] image)
	{
		this.image = image;
	}
}