package studio.lineage2.cms.controllers.admin;

import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.ResponseBody;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.model.UserItemLog;
import studio.lineage2.cms.model.UserItemType;
import studio.lineage2.cms.repository.GameServerRepository;
import studio.lineage2.cms.service.BonusCodeService;
import studio.lineage2.cms.service.GameService;
import studio.lineage2.cms.service.UserIpService;
import studio.lineage2.cms.service.UserItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.model.Email;
import studio.lineage2.cms.repository.EmailRepository;
import studio.lineage2.cms.service.EmailService;
import studio.lineage2.cms.service.UserService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.utils.Rnd;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

/**
 Created by iRock
 28.10.2015
 */
@Controller
@RequestMapping("/admin")
public class AdminController
{
	@Autowired
	@Qualifier("emailRepository")
	private EmailRepository emailRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserItemsService userItemsService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	private BonusCodeService bonusService;

	@Autowired
	private GameServerRepository gameServerRepository;

	@Autowired
	private GameService gameService;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		model.addAttribute("adminmenu", true);
		model.addAttribute("website_accounts", userService.userCount());
		model.addAttribute("game_accounts", gameServerRepository.getAccountAmount());
		model.addAttribute("player_amount", gameServerRepository.getPlayersAmount());
		model.addAttribute("credit_purchased", gameServerRepository.getCreditAmount());
		model.addAttribute("gold_amount", gameServerRepository.getGoldsAmount());
		model.addAttribute("alert_list", gameServerRepository.getAlerts());
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/index.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "/addMoneyToUserId", method = { RequestMethod.POST })
	public @ResponseBody String addMoney(ModelMap model, String userId, String sum)
	{
		if(!userId.matches("[0-9]{1,10}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Id пользователя должен состоять из 1-10 цифр");
		}

		if(!sum.matches("[0-9]{1,10}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Сумма должна состоять из 1-10 цифр");
		}
		Integer user = Integer.parseInt(userId);
		UserItemLog itemLog = userItemsService.addUserItem(user, UserItemType.MONEY.getName(), UserItemType.MONEY.getId(), Long.parseLong(sum), "logging.admin_donate", sum + " " + UserItemType.MONEY.getName());
		if(itemLog!=null)
		{
			userIpService.save(new UserIp(user, "", Calendar.getInstance().getTimeInMillis(), itemLog.getId(), true, ""));
		}
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, sum + " " + UserItemType.MONEY.getName() + " добавлены пользователю " + userService.findUserById(userId).getUsername(), null, null);
	}

	@RequestMapping(value = "/generateBonus", method = { RequestMethod.POST })
	public @ResponseBody String generateBonus(HttpServletResponse response, String sum, String countS)
	{
		if(!sum.matches("[0-9]{1,10}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Сумма должна состоять из 1-10 цифр");
		}
		int count = Integer.parseInt(countS);
		if(count > 40)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Количество не может быть больше 40");
		if(count < 2)
			return generateCode(sum);
		StringBuilder sb = new StringBuilder();
		sb.append("Sum: ").append(sum).append("\r\n");
		for(int i = 0; i < count; i++)
		{
			String code = Rnd.getBonusCode();
			if(bonusService.createBonusCode(code, Long.parseLong(sum)))
				sb.append(code.substring(0,4)).append("-").append(code.substring(4,8)).append("-").append(code.substring(8,12)).append("-").append(code.substring(12,16)).append("\r\n");
		}
		String link ="<a download=\"bonus_codes.txt\" id=\"downloadlink\" style=\"display: block\">Download</a>";
		String code = "downloadFile(atob(\""+ new String(Base64.encode(sb.toString().getBytes()))+"\"));";
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, link, code, null);
	}

	@RequestMapping(value = "/setAlertPoints", method = { RequestMethod.POST })
	public @ResponseBody String setAlertPoints(ModelMap model, String login, String points, int alertId)
	{
		if(login.isEmpty())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Логин пользователя не указан");
		}

		if(!points.matches("[0-9]{1,2}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Сумма должна состоять из 1-2 цифр");
		}
		Integer alertPoints = Integer.parseInt(points);
		if(gameService.xmlrpcSetAlerts(login, alertPoints))
		{
			gameServerRepository.getAlerts().get(alertId-1).setPoints(points);
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Очки предупреждений успешно установлены", "$('#alert-" + alertId + "').html('" + alertPoints + "');");
		}
		return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Нет связи с сервером", null, null);
	}

	public String generateCode(String sum)
	{
		String code = Rnd.getBonusCode();
		if(!bonusService.createBonusCode(code, Long.parseLong(sum)))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Бонусный код не создан");
		code = String.join("-", code.substring(0,4), code.substring(4,8), code.substring(8,12), code.substring(12,16));
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Бонус код: " + code + " на сумму " + sum + " " + UserItemType.MONEY.getName(), null, null);
	}

	@RequestMapping(value = "/restart", method = { RequestMethod.GET })
	public void restart()
	{
		System.exit(2);
	}

	@RequestMapping(value = "/mail", method = { RequestMethod.GET })
	public String mail(ModelMap model, Locale locale)
	{
		int count = 0;
		for(Email email : emailRepository.findAll())
		{
			if(email.isSend())
			{
				count++;
			}
		}
		model.addAttribute("adminmenu", true);
		model.addAttribute("count", count);
		model.addAttribute("max", emailRepository.findAll().size());

		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/mail.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "/sendMail", method = { RequestMethod.POST })
	public String sendMail(ModelMap model, String title, String content, Locale locale)
	{
		emailService.send(title, content);
		return mail(model, locale);
	}

	@RequestMapping(value = "/sendTestEmail", method = { RequestMethod.POST })
	public String sendTestEmail(ModelMap model, String email, Locale locale)
	{
		emailService.sendTest(email, "Тест", "Тест");
		return mail(model, locale);
	}

	@RequestMapping(value = "/removeDublicateEmail", method = { RequestMethod.GET })
	public String removeDublicateEmail(ModelMap model, Locale locale)
	{
		Set<String> emails = new HashSet<>();
		for(Email email : emailRepository.findAll())
		{
			emails.add(email.getEmail());
			emailRepository.delete(email);
		}

		for(String s : emails)
		{
			emailRepository.save(new Email(s));
		}

		return mail(model, locale);
	}

	@RequestMapping(value = "/importMail", method = { RequestMethod.POST })
	public String importMail(ModelMap model, String emails, Locale locale)
	{
		StringTokenizer st = new StringTokenizer(emails, "\r\n");
		while(st.hasMoreTokens())
		{
			emailRepository.save(new Email(st.nextToken()));
		}
		return mail(model, locale);
	}
}