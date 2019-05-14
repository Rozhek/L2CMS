package studio.lineage2.cms.model;

/**
 Created by iRock
 04.11.2015
 */
public enum UserItemType
{
	MONEY(-1, "Credit"),
	GOLD(4356, "Gold");

	private int id;
	private String name;

	UserItemType(int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public int getId()
	{
		return id;
	}

	public String getIdString()
	{
		return String.valueOf(id);
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public static UserItemType getByItemId(int itemId)
	{
		for(UserItemType userItemType : values())
		{
			if(userItemType.getId() == itemId)
			{
				return userItemType;
			}
		}
		return null;
	}
}