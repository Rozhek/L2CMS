package studio.lineage2.cms.model;

/**
 Created by iRock
 03.11.2015
 */
public enum SupportType
{
	ALL("Все заявки"),
	NEW("Новая"),
	WAIT("Ожидает ответа пользователя"),
	REPLY("Ответ получен"),
	FORDEVELOPER("В разработке"),
	FORTESTER("Тестируется"),
	CLOSE("Закрыто");

	private String name;

	SupportType(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}