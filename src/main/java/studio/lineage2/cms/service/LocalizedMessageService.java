package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 Created by iRock
 30.10.2015
 */
@Component
public class LocalizedMessageService
{
	@Autowired
	private LocaleResolver localeResolver;

	@Resource(name = "messageSource")
	private MessageSource messages;

	public String getMessage(String ref, HttpServletRequest httpServletRequest)
	{
		String msg;
		try{
			msg = messages.getMessage(ref, null, localeResolver.resolveLocale(httpServletRequest));
		}
		catch(NoSuchMessageException e) {
			msg = "Error! Message not found!";
		}
		return msg;
	}

	public String getMessage(String ref, Locale locale)
	{
		String msg;
		try{
			msg = messages.getMessage(ref, null, locale);
		}
		catch(NoSuchMessageException e) {
			msg = "Error! Message not found!";
		}
		return msg;
	}

	public String getMessage(String ref, HttpServletRequest httpServletRequest, Object[] args)
	{
		String msg;
		try{
			msg = messages.getMessage(ref, args, localeResolver.resolveLocale(httpServletRequest));
		}
		catch(NoSuchMessageException e) {
			msg = "Error! Message not found!";
		}
		return msg;
	}

	public String getMessage(String ref, Locale locale, Object[] args)
	{
		String msg;
		try{
			msg = messages.getMessage(ref, args, locale);
		}
		catch(NoSuchMessageException e) {
			msg = "Error! Message not found!";
		}
		return msg;
	}

}