package studio.lineage2.cms.utils;

import java.util.Locale;

/**
 * Created by iRock on 07.06.2017.
 */
public class LocalizePath
{
	public static String build(Locale locale, String prePath)
	{
		switch(locale.getLanguage())
		{
			case "en":
				return prePath.replace("$$", "en");
			default:
				return prePath.replace("$$", "ru");
		}
	}
}
