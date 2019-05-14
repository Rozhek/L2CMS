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
@Table(name = "shop_packets")
public class ShopPacket
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@Column(name = "packet_name")
	private String packetName;

	@Column(name = "packet_price")
	private int packetPrice;

	@Column(name = "image", length = 1024000)
	private byte[] image;

	public ShopPacket()
	{
	}

	public long getId()
	{
		return id;
	}

	public String getPacketName()
	{
		return packetName;
	}

	public void setPacketName(String packetName)
	{
		this.packetName = packetName;
	}

	public int getPacketPrice()
	{
		return packetPrice;
	}

	public void setPacketPrice(int packetPrice)
	{
		this.packetPrice = packetPrice;
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