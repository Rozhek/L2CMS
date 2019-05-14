package studio.lineage2.cms.model;

/**
 Created by iRock
 04.11.2015
 */
public enum SupportPrioritet
{
	LOW("Низкий"),
	MIDDLE("Средний"),
	HIGH("Высокий"),
	URGENT("Срочный");

	private String name;

	SupportPrioritet(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}