package studio.lineage2.cms.controllers.admin;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.model.Support;
import studio.lineage2.cms.model.SupportMessage;
import studio.lineage2.cms.model.SupportType;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.SupportMessageService;
import studio.lineage2.cms.service.SupportService;
import studio.lineage2.cms.service.UserItemsService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import java.util.Calendar;
import java.util.Locale;

/**
 Created by iRock
 03.11.2015
 */
@Controller
@RequestMapping("/admin/support")
public class AdminSupportController
{
	@Autowired
	private SupportService supportService;

	@Autowired
	private SupportMessageService supportMessageService;

	@Autowired
	private UserItemsService userItemsService;

	@Autowired
	VelocityEngine velocityEngine;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		model.addAttribute("adminmenu", true);
		model.addAttribute("supportList", supportService.findAllSupport(SupportType.ALL));
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/support.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	public String view(User user, Locale locale, String support_id)
	{
		if(!user.getIsAdmin())
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));

		int id;
		try{
			id = Integer.parseInt(support_id);
		}catch(Exception e){
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));
		}

		ModelMap model = new ModelMap();
		Support support = supportService.findSupportById(id);
		if(support == null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));

		model.addAttribute("support", support);
		model.addAttribute("messages", supportMessageService.findSupportMessagesBySupportId(support.getId()));
		model.addAttribute("types", SupportType.values());

		String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/admin/support/ticketview.vm"), "UTF-8", model);

		return JsonMessage.formAnswerBody(support.getTitle(), body);
	}

	public String addMessage(User user, Locale locale, String support_id, String message, String support_type)
	{
		if(!user.getIsAdmin())
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));

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

		Support support = supportService.findSupportById(id);
		if(support == null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));

		SupportMessage supportMessage = new SupportMessage();
		supportMessage.setSupportId(support.getId());
		supportMessage.setAuthorId(user.getId());
		supportMessage.setDate(Calendar.getInstance().getTimeInMillis());
		supportMessage.setPostIsAdmin(true);
		supportMessage.setContent(message);

		int type;
		if(support_type!=null && !support_type.equals("0"))
		{
			try{
				type = Integer.parseInt(support_type);
			}catch(Exception e){
				return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.error", locale));
			}
			support.setType(SupportType.values()[type]);
		}
		else
		{
			switch(support.getType())
			{
				case NEW:
				case REPLY:
				case FORDEVELOPER:
				case FORTESTER:
				{
					support.setType(SupportType.WAIT);
				}
				break;
			}
		}

		supportMessageService.save(supportMessage);
		supportService.save(support);

		return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("support.message_success", locale), "location.reload(true);");

	}
}