package studio.lineage2.cms.controllers.account;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.model.Player;
import studio.lineage2.cms.model.Support;
import studio.lineage2.cms.model.SupportMessage;
import studio.lineage2.cms.model.SupportPrioritet;
import studio.lineage2.cms.model.SupportType;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.repository.GameServerRepository;
import studio.lineage2.cms.service.AccountService;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.PlayerService;
import studio.lineage2.cms.service.SupportMessageService;
import studio.lineage2.cms.service.SupportService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 Created by iRock
 02.11.2015
 */
@Controller
@RequestMapping("/account/support")
public class SupportController
{
	@Autowired
	private SupportService supportService;

	@Autowired
	private SupportMessageService supportMessageService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private PlayerService playerService;

	@Value("${unitpaySecretKey}")
	private String unitpaySecretKey;

	@Value("${g2aSecretKey2}")
	private String g2aSecretKey2;

	@Autowired
	private GameServerRepository gameServerRepository;

	@Autowired
	VelocityEngine velocityEngine;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!(object instanceof User))
		{
			return "redirect:/login";
		}
		User user = (User) object;
		model.addAttribute("supportList", supportService.findAllSupportByUserId(user.getId(), SupportType.ALL));

		List<Account> accounts = accountService.findAllByUserId(user.getId());
		model.addAttribute("accounts", accounts);
		List<Player> players = new ArrayList<>();
		for(Account acc : accounts)
			players.addAll(playerService.findAllByAccountId(acc.getId()));
		model.addAttribute("players", players);
		model.addAttribute("supportmenu", true);

		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/account/support.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	public String getTicketForm(User user, Locale locale, String form)
	{
		ModelMap model = new ModelMap();

		List<Account> accounts = accountService.findAllByUserId(user.getId());
		model.addAttribute("accounts", accounts);
		List<Player> players = new ArrayList<>();
		for(Account acc : accounts)
			players.addAll(playerService.findAllByAccountId(acc.getId()));
		model.addAttribute("players", players);

		model.addAttribute("unitpayEnabled", !unitpaySecretKey.isEmpty());
		model.addAttribute("g2aEnabled", !g2aSecretKey2.isEmpty());
		String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/support/"+form+".vm"), "UTF-8", model);

		return JsonMessage.formAnswerBody(localizedMessage.getMessage("support.ticket_title", locale), body);
	}

	public String addTicket(User user, Locale locale, Map<String,String> params)
	{
		StringBuilder body = new StringBuilder("");
		String value, title;
		if((value = params.get("support_server"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Server - ").append("High Five x30").append(".<br>");

		if((value = params.get("support_account_name"))==null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(!value.equals("0"))
			body.append("Account - ").append(value).append(".<br>");

		if((value = params.get("support_char_name"))!=null)
			body.append("Character - ").append(value).append(".<br>");

		if((value = params.get("support_category"))==null || value.isEmpty() || value.equals("0"))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Category - ").append(chooseCategory(value)).append(".<br>");

		if((value = params.get("support_title"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(value.length()>120)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.title_length", locale));
		else title = value;

		if((value = params.get("support_message"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(value.length()>1000)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.content_length", locale));
		else body.append("Comment - ").append(value);

		return add(user, locale, title, body.toString());
	}

	public String addBotTicket(User user, Locale locale, Map<String,String> params)
	{
		StringBuilder body = new StringBuilder("");
		String value;
		if((value = params.get("support_server"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Server - ").append("High Five x30").append(".<br>");

		if((value = params.get("data-date"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Date - ").append(value).append(".<br>");

		if((value = params.get("data-time"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Time - ").append(value).append(".<br>");

		if((value = params.get("nick_bots"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Nick bot - ").append(value).append(".<br>");

		if((value = params.get("comment"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(value.length()>1000)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.content_length", locale));
		else body.append("Comment - ").append(value);

		return add(user, locale, localizedMessage.getMessage("support.bot_title", locale), body.toString());
	}

	public String addDonationTicket(User user, Locale locale, Map<String,String> params)
	{
		StringBuilder body = new StringBuilder("");
		String value;
		if((value = params.get("agrigation"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Payment System - ").append(value.toUpperCase()).append(".<br>");

		if((value = params.get("transactions"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Transaction - ").append(value).append(".<br>");

		if((value = params.get("data-date"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Date - ").append(value).append(".<br>");

		if((value = params.get("data-time"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Time - ").append(value).append(".<br>");

		if((value = params.get("money-count"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Amount - ").append(value).append(".<br>");

		if((value = params.get("comment"))==null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(value.length()>1000)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.content_length", locale));
		else body.append("Comment - ").append(value);

		return add(user, locale, localizedMessage.getMessage("support.donat_title", locale), body.toString());
	}

	public String addItemTicket(User user, Locale locale, Map<String,String> params)
	{
		StringBuilder body = new StringBuilder("");
		String value;
		if((value = params.get("support_server"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Server - ").append("High Five x30").append(".<br>");

		if((value = params.get("support_account_name"))==null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(!value.equals("0"))
			body.append("Account - ").append(value).append(".<br>");

		if((value = params.get("support_char_name"))!=null)
			body.append("Character - ").append(value).append(".<br>");

		boolean sharing = params.get("sharing").equals("yes");

		if((value = sharing ? params.get("data-date-drop-sharing") : params.get("data-date-drop-no-sharing"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Lost date - ").append(value).append(".<br>");

		if((value = sharing ? params.get("data-date-last-enter-sharing") : params.get("data-date-last-enter-no-sharing"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Last enter - ").append(value).append(".<br>");

		if(sharing)
		{
			if((value = params.get("nick_sharing")) == null || value.isEmpty())
				return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
			else
				body.append("Players with access - ").append(value).append(".<br>");;
		}

		if((value = params.get("protect"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else body.append("Protected - ").append(value).append(".<br>");;

		if((value = params.get("skype"))!=null)
			body.append("Skype - ").append(value).append(".<br>");;

		if((value = sharing ? params.get("comment_share") : params.get("comment_no_share"))==null || value.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(value.length()>1000)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.content_length", locale));
		else body.append("Lost items - ").append(value);

		return add(user, locale, localizedMessage.getMessage("support.item_title", locale), body.toString());
	}

	public String add(User user, Locale locale, String title, String body)
	{
		Support support = new Support();
		support.setTitle(title);
		support.setType(SupportType.NEW);
		support.setPrioritet(SupportPrioritet.LOW);
		supportService.save(support);

		if(body.length() > 1000) body = body.substring(0,999);
		SupportMessage supportMessage = new SupportMessage();
		supportMessage.setContent(body);
		supportMessage.setAuthorId(user.getId());
		supportMessage.setDate(Calendar.getInstance().getTimeInMillis());
		supportMessage.setPostIsAdmin(false);
		supportMessage.setSupportId(support.getId());
		supportMessageService.save(supportMessage);

		return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("support.ticket_success", locale), "location.reload(true);");
	}

	public String view(User user, Locale locale, String support_id)
	{
		int id;
		try{
			id = Integer.parseInt(support_id);
		}catch(Exception e){
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));
		}

		ModelMap model = new ModelMap();
		Support support = supportService.findSupportByIdAndUserId(id, user.getId());
		if(support == null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));

		model.addAttribute("support", support);
		model.addAttribute("messages", supportMessageService.findSupportMessagesBySupportId(support.getId()));

		String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/support/ticketview.vm"), "UTF-8", model);

		return JsonMessage.formAnswerBody(support.getTitle(), body);
	}

	public String addMessage(User user, Locale locale, String support_id, String message)
	{
		int id;
		try{
			id = Integer.parseInt(support_id);
		}catch(Exception e){
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));
		}

		if(message==null || message.isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.fields_empty", locale));
		else if(message.length()>1000)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("support.content_length", locale));

		Support support = supportService.findSupportByIdAndUserId(id, user.getId());
		if(support == null || support.getType() == SupportType.CLOSE)
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));

		SupportMessage supportMessage = new SupportMessage();
		supportMessage.setSupportId(support.getId());
		supportMessage.setAuthorId(user.getId());
		supportMessage.setDate(Calendar.getInstance().getTimeInMillis());
		supportMessage.setPostIsAdmin(false);
		supportMessage.setContent(message);

		switch(support.getType())
		{
			case WAIT:
			{
				support.setType(SupportType.REPLY);
			}
			break;
		}

		supportMessageService.save(supportMessage);
		supportService.save(support);

		return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("support.message_success", locale), "location.reload(true);");

	}

	private String chooseCategory(String cat)
	{
		int value;
		try{
			value = Integer.parseInt(cat);
		}
		catch(Exception e){
			value = 0;
		};
		switch(value){
			case 1:
				return "General issues";
			case 2:
				return "Website , Forum , CP";
			case 3:
				return "Client Errors";
			case 4:
				return "Game bugs";
			case 5:
				return "Blocking of account / character";
			case 6:
				return "Loss of items";
			case 7:
				return "Report a bot";
			case 8:
				return "Problems with donation to the balance";
			case 9:
				return "Other";
			default:
				return "Other";
		}
	}

}