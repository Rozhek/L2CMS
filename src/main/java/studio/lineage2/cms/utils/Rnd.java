package studio.lineage2.cms.utils;

import java.util.Random;

/**
 Created by iRock
 30.10.2015
 */
public class Rnd
{
	private static Random rnd = new Random();

	private static final String password = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String prefix = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String code = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String getPassword()
	{
		StringBuilder sb = new StringBuilder(32);
		for(int i = 0; i < 12; i++)
		{
			sb.append(password.charAt(rnd.nextInt(password.length())));
		}
		return sb.toString();
	}

	public static String getPrefix()
	{
		StringBuilder sb = new StringBuilder(3);
		for(int i = 0; i < 3; i++)
		{
			sb.append(prefix.charAt(rnd.nextInt(prefix.length())));
		}
		return sb.toString();
	}

	public static String getBonusCode()
	{
		StringBuilder sb = new StringBuilder(16);
		for(int i = 0; i < 16; i++)
		{
			sb.append(code.charAt(rnd.nextInt(code.length())));
		}
		return sb.toString();
	}

	public static int get(int max)
	{
		return get(0, max);
	}

	public static int get(int min, int max)
	{
		return rnd.nextInt(max - min) + min;
	}
}