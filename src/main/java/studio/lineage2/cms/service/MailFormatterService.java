package studio.lineage2.cms.service;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.ui.velocity.VelocityEngineUtils;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.utils.MailUtil;
import studio.lineage2.cms.xmlrpc.XMLRPCMessage;

import java.util.Locale;

/**
 * Created by iRock on 16.11.2017.
 */
@Service
public class MailFormatterService
{
	@Value("${mail.title}")
	private String mailTitle;

	@Value("${mail.send}")
	private boolean sendMail;

	@Autowired
	private MailUtil mailUtil;

	@Value("${mail.html}")
	private boolean htmlMail;

	@Value("${hostname}")
	private String hostname;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@Autowired
	VelocityEngine velocityEngine;

	public boolean sendAccountChangeMessage(String username, String account, String password, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("account", account);
			model.addAttribute("password", password);
			model.addAttribute("hostname", hostname);

			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/mail/account_change.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("account.change_mail_1", locale)).append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("account.change_mail_2", locale)).append(account).append("\n");
			sb.append(localizedMessage.getMessage("account.change_mail_3", locale)).append(password).append("\n");
			sb.append(localizedMessage.getMessage("account.change_mail_4", locale, new String[]{hostname})).append("\n");
			content = sb.toString();
		}

		try{ mailUtil.send(username, mailTitle + localizedMessage.getMessage("account.change_mail_title", locale), content, htmlMail); }
		catch(Exception e){ e.printStackTrace(); return false; }
		return true;
	}

	public boolean sendAccountRegisterMessage(String username, String account, String password, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("account", account);
			model.addAttribute("password", password);
			model.addAttribute("hostname", hostname);
			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/mail/account_register.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("account.register_mail_1", locale)).append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("account.register_mail_2", locale)).append(account).append("\n");
			sb.append(localizedMessage.getMessage("account.register_mail_3", locale)).append(password).append("\n");
			sb.append(localizedMessage.getMessage("account.register_mail_4", locale, new String[]{hostname})).append("\n");
			content = sb.toString();
		}

		try{ mailUtil.send(username, mailTitle + localizedMessage.getMessage("account.register_mail_title", locale), content, htmlMail); }
		catch(Exception e){ e.printStackTrace(); return false; }
		return true;
	}


	public boolean sendChangeMessage(String username, String password, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("name", username);
			model.addAttribute("password", password);
			model.addAttribute("hostname", hostname);
			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/mail/password.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("security.change_mail_1", locale)).append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("security.change_mail_2", locale)).append(username).append("\n");
			sb.append(localizedMessage.getMessage("security.change_mail_3", locale)).append(password).append("\n");
			sb.append(localizedMessage.getMessage("security.change_mail_4", locale)).append("\n");
			content = sb.toString();
		}

		try{ mailUtil.send(username, mailTitle + localizedMessage.getMessage("security.change_mail_title", locale), content, htmlMail); }
		catch(Exception e){ e.printStackTrace(); return false; }
		return true;
	}

	public boolean sendAccountRecoverMessage(String username, String account, String password, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("account", account);
			model.addAttribute("password", password);
			model.addAttribute("hostname", hostname);
			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/mail/account_recover.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("account.recover_mail_1", locale)).append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("account.recover_mail_2", locale)).append(account).append("\n");
			sb.append(localizedMessage.getMessage("account.recover_mail_3", locale)).append(password).append("\n");
			sb.append(localizedMessage.getMessage("account.recover_mail_4", locale, new String[]{hostname})).append("\n");
			content = sb.toString();
		}

		try{ mailUtil.send(username, mailTitle + localizedMessage.getMessage("account.recover_mail_title", locale), content, htmlMail); }
		catch(Exception e){ e.printStackTrace(); return false; }
		return true;
	}


	public boolean sendVerifyMessage(String username, String token, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("name", username);
			model.addAttribute("token", token);
			model.addAttribute("hostname", hostname);
			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/mail/verify.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("verification.mail_1", locale)).append(" ").append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("registration.mail_2", locale)).append(" ").append(username).append("\n");
			sb.append(localizedMessage.getMessage("verification.mail_2", locale)).append(" ").append("http://").append(hostname).append("/confirmation?token=").append(token).append("\n");
			sb.append(localizedMessage.getMessage("registration.mail_8", locale)).append("\n");
			content = sb.toString();
		}

		try{mailUtil.send(username, mailTitle + localizedMessage.getMessage("verification.mail_title", locale), content, htmlMail);}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean sendRecoverMessage(String username, String password, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("name", username);
			model.addAttribute("password", password);
			model.addAttribute("hostname", hostname);
			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/mail/recover.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("recover.mail_1", locale)).append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("recover.mail_2", locale)).append(password).append("\n");
			sb.append(localizedMessage.getMessage("recover.mail_3", locale, new String[]{hostname})).append("\n");
			sb.append(localizedMessage.getMessage("recover.mail_4", locale)).append("\n");
			content = sb.toString();
		}

		try{ mailUtil.send(username, mailTitle + localizedMessage.getMessage("recover.mail_title", locale), content, htmlMail); }
		catch(Exception e){ e.printStackTrace(); return false; }
		return true;
	}


	public boolean sendRegisterMessage(String username, String password, String prefix, String account, XMLRPCMessage tryReg, String token, Locale locale) {

		if(!sendMail)
			return false;

		String content;
		if(htmlMail)
		{
			ModelMap model = new ModelMap();
			model.addAttribute("mailtitle", mailTitle);
			model.addAttribute("name", username);
			model.addAttribute("account", tryReg!=null && tryReg.getType() == XMLRPCMessage.MessageType.OK? account : "");
			model.addAttribute("password", password);
			model.addAttribute("hostname", hostname);
			model.addAttribute("token", token);

			content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, token!=null? LocalizePath.build(locale, "cp/$$/mail/register_verify.vm"): LocalizePath.build(locale, "cp/$$/mail/register.vm"), "UTF-8", model);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localizedMessage.getMessage("registration.mail_1", locale)).append(" ").append(mailTitle).append("\n\n");
			sb.append(localizedMessage.getMessage("registration.mail_2", locale)).append(" ").append(username).append("\n");
			if(tryReg == null)
				sb.append(localizedMessage.getMessage("registration.mail_3", locale)).append(" ").append("\n");
			else if(tryReg.getType() != XMLRPCMessage.MessageType.OK)
				sb.append(localizedMessage.getMessage("registration.mail_4", locale)).append(" ").append(tryReg.getMessage()).append("\n");
			else
				sb.append(localizedMessage.getMessage("registration.mail_5", locale)).append(" ").append(prefix).append(account).append("\n");
			sb.append(localizedMessage.getMessage("registration.mail_6", locale)).append(" ").append(password).append("\n");
			if(token != null)
				sb.append(localizedMessage.getMessage("registration.mail_7", locale)).append(" ").append("http://").append(hostname).append("/confirmation?token=").append(token).append("\n");
			sb.append(localizedMessage.getMessage("registration.mail_8", locale)).append("\n");
			content = sb.toString();
		}

		try{ mailUtil.send(username, mailTitle + localizedMessage.getMessage("registration.mail_title", locale), content, htmlMail); }
		catch(Exception e){ e.printStackTrace(); return false; }
		return true;
	}

}
