package studio.lineage2.cms.controllers;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.model.Bonus;
import studio.lineage2.cms.service.BonusCodeService;
import studio.lineage2.cms.service.GameService;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.model.Player;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.model.UserItem;
import studio.lineage2.cms.model.UserItemType;
import studio.lineage2.cms.repository.GameServerRepository;
import studio.lineage2.cms.service.AccountService;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.MailFormatterService;
import studio.lineage2.cms.service.PlayerService;
import studio.lineage2.cms.service.UserIpService;
import studio.lineage2.cms.service.UserItemsService;
import studio.lineage2.cms.service.UserService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.utils.Rnd;
import studio.lineage2.cms.xmlrpc.JsonMessage;
import studio.lineage2.cms.xmlrpc.XMLRPCMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 Created by iRock
 23.10.2015
 */
@Controller
@RequestMapping("/account")
public class AccountController
{
	@Value("${mail.send}")
	private boolean sendMail;

	@Autowired
	private AccountService accountService;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	private UserItemsService userItemsService;

	@Autowired
	private GameServerRepository gameServerRepository;

	@Autowired
	private GameService gameService;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@Autowired
	private MailFormatterService mailFormatter;

	@Autowired
	private BonusCodeService bonusService;

	@Autowired
	VelocityEngine velocityEngine;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, HttpServletRequest request, Locale locale)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!(object instanceof User))
		{
			return "redirect:/login";
		}
		User user = (User) object;

		UserIp last = userIpService.findLastUserIp(user.getId());

		model.addAttribute("lastIp", last != null ? last.getIp():"");
		model.addAttribute("lastLogin", last != null ? last.getTime():"");
		model.addAttribute("currentIp", userIpService.getProxifiedAddress(request));

		UserItem userItem = userItemsService.findOne(user.getId(), UserItemType.MONEY.getId());
		model.addAttribute("MONEY", userItem == null ? 0 : userItem.getItemCount());

		model.addAttribute("prefix", Rnd.getPrefix());

		//model.addAttribute("identifier", userIdentifierService.findOne(user.getId()));
		List<Account> accounts = accountService.findAllByUserId(user.getId());
		model.addAttribute("accounts", accounts);
		List<Player> players = new ArrayList<>();
		for(Account acc : accounts)
			players.addAll(playerService.findAllByAccountId(acc.getId()));
		model.addAttribute("players", players);

		model.addAttribute("mainmenu", true);
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/account/index.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "/settings", method = { RequestMethod.GET })
	public String showSettings(ModelMap model, Locale locale)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!(object instanceof User))
		{
			return "redirect:/login";
		}
		User user = (User) object;

		List<Account> accounts = accountService.findAllByUserId(user.getId());
		model.addAttribute("accounts", accounts);
		List<Player> players = new ArrayList<>();
		for(Account acc : accounts)
			players.addAll(playerService.findAllByAccountId(acc.getId()));
		model.addAttribute("players", players);

		model.addAttribute("settingsmenu", true);
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/account/settings.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "/transfer", method = { RequestMethod.GET })
	public String showTransfer(ModelMap model, Locale locale)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!(object instanceof User))
		{
			return "redirect:/login";
		}
		User user = (User) object;

		UserItem userItem = userItemsService.findOne(user.getId(), UserItemType.MONEY.getId());
		model.addAttribute("MONEY", userItem == null ? 0 : userItem.getItemCount());

		List<Account> accounts = accountService.findAllByUserId(user.getId());
		model.addAttribute("accounts", accounts);
		List<Player> players = new ArrayList<>();
		for(Account acc : accounts)
			players.addAll(playerService.findAllByAccountId(acc.getId()));
		model.addAttribute("players", players);

		model.addAttribute("transfermenu", true);
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/account/transfer.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	public String getMoneyCount(User user, Locale locale)
	{
		UserItem userItem = userItemsService.findOne(user.getId(), UserItemType.MONEY.getId());
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, userItem == null ? "0" : String.valueOf(userItem.getItemCount()));
	}

	public String addCoins(User user, String account_name, String player_id, String count, Locale locale, HttpServletRequest request)
	{
		if(!accountService.findAccount(user.getId(), account_name))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));

		XMLRPCMessage reg = gameService.xmlrpcAddCoins(user, count, player_id, locale, request);
		if(reg == null)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(reg.getType() != XMLRPCMessage.MessageType.OK)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, reg.getMessage());
		}
		else{
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.transfer_success", locale));
		}
	}

	public String addBonus(User user, String code, Locale locale)
	{
		code = code.replace("-","");
		if(!code.matches("[A-Za-z0-9]{16,16}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("bonus.not_found", locale));
		}
		code = code.toUpperCase();
		Bonus bonuscode = bonusService.getBonusCode(code);
		if(bonuscode == null )
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("bonus.not_found", locale));
		}
		Calendar cal = Calendar.getInstance();
		if((bonuscode.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("bonus.not_found", locale));
		}
		else if(bonuscode.isActivated())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("bonus.already_activate", locale));
		}
		else{
			bonuscode.activate(user.getId());
			userItemsService.addUserItem(user.getId(),UserItemType.MONEY.getName(), UserItemType.MONEY.getId(), bonuscode.getValue(), "logging.bonus", bonuscode.getValue() + " " + UserItemType.MONEY.getName());
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("bonus.activated", locale));
		}
	}

	public String getIpLog(User user, Locale locale)
	{
		StringBuilder body = new StringBuilder("");
		body.append("<table class=\"table table-vcenter\"><thead><tr><th>Action</th><th>Date</th><th>IP</th><th>Browser</th></tr></thead><tbody>");
		for(UserIp userIp : userIpService.findLastUserIp(user.getId(), 15))
			body.append("   <tr class=\"").append(userIp.getType()).append("\"><td>").append(localizedMessage.getMessage("logging.authorization", locale)).append("</td><td>").append(userIp.getTime()).append("</td><td>").append(userIp.getIp()).append("</td><td>").append(userIp.getBrowser()).append("</td></tr>     ");
		body.append("</tbody></table>");

		return JsonMessage.formAnswerBody(localizedMessage.getMessage("logging.authorization_title", locale), body.toString());
	}

	public String getMainPasswordForm(User user, Locale locale)
	{
		ModelMap model = new ModelMap();
		String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/form/mainpasswordform.vm"), "UTF-8", model);

		return JsonMessage.formAnswerBody(localizedMessage.getMessage("security.change_pass_title", locale), body);
	}

	public String getAccPasswordForm(User user, String account_name, Locale locale)
	{
		ModelMap model = new ModelMap();
		if(!accountService.findAccount(user.getId(), account_name))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));
		model.addAttribute("account_name", account_name);
		String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/form/accountpasswordform.vm"), "UTF-8", model);

		return JsonMessage.formAnswerBody(localizedMessage.getMessage("account.change_pass_title", locale), body);
	}

	public String getAccRecoveryForm(User user, String account_name, Locale locale)
	{
		ModelMap model = new ModelMap();
		if(!accountService.findAccount(user.getId(), account_name))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));
		model.addAttribute("account_name", account_name);
		String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, LocalizePath.build(locale, "cp/$$/form/recoverpasswordform.vm"), "UTF-8", model);

		return JsonMessage.formAnswerBody(localizedMessage.getMessage("account.recover_pass_title", locale), body);
	}

	public String changeMainPassword(String password, String newpassword, String verify, Locale locale, HttpServletRequest request)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!(object instanceof User))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));
		User user = (User) object;
		String browser = userIpService.findCurrentBrowser(request);

		if(!user.isActive())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.need_verification", locale));
		}

		if(password==null || newpassword==null || verify==null || password.isEmpty() || newpassword.isEmpty() || verify.isEmpty())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("security.change_password_empty", locale));
		}

		if(!newpassword.matches("[A-Za-z0-9]{8,32}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("security.change_password_invalid", locale));
		}

		if(!newpassword.equals(verify))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("security.change_password_notverified", locale));
		}

		if(!new BCryptPasswordEncoder().matches(password, user.getPassword()))
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.PASSWORDCHANGE, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("security.change_password_wrong", locale));
		}

		user.setPassword(new BCryptPasswordEncoder().encode(newpassword));
		userService.saveUser(user);
		userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.PASSWORDCHANGE, true, browser));

		if(mailFormatter.sendChangeMessage(user.getUsername(), password, locale))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("security.change_success", locale));
		}
		else
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale), "login");
		}
	}

	public String updateAccounts(User user, Locale locale)
	{
		if(gameService.xmlrpcUpdatePlayers(user).getType() == XMLRPCMessage.MessageType.OK)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("simplemsg.update_success", locale), "account");
		}
		else
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.update_failed", locale));
		}
	}

	public String registerAccount(User user, String prefix, String account, String password, String passwordverify, Locale locale, HttpServletRequest request)
	{
		if(!user.isActive())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.need_verification", locale));
		}

		if(!password.equals(passwordverify)){
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("registration.verify_password", locale));
		}

		if(!account.matches("[A-Za-z0-9]{4,11}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.login_invalid", locale));
		}

		String browser = userIpService.findCurrentBrowser(request);
		XMLRPCMessage reg = gameService.xmlrpcRegister(user, prefix, account, password, locale);
		if(reg == null)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CREATE, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(reg.getType() != XMLRPCMessage.MessageType.OK)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CREATE, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, reg.getMessage());
		}
		else{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CREATE, true, browser));
			if(mailFormatter.sendAccountRegisterMessage(user.getUsername(), prefix+account, password, locale))
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.registration_success", locale));
			}
			else
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale), "login");
			}
		}
	}

	public String addAccount(User user, String account, String password, Locale locale, HttpServletRequest request)
	{
		if(!user.isActive())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.need_verification", locale));
		}

		String browser = userIpService.findCurrentBrowser(request);
		if(accountService.findAccountByName(account)!=null)
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));

		if(accountService.findAllByUserId(user.getId()).size()>6)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("account.limit_exceeded", locale));
		}

		XMLRPCMessage add = gameService.xmlrpcLoginAccount(account, password);
		if(add == null)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CREATE, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(add.getType() != XMLRPCMessage.MessageType.OK)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CREATE, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.wrong_password", locale));
		}
		else{
			accountService.save(new Account(user.getId(), account));
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CREATE, true, browser));

			if(mailFormatter.sendAccountRegisterMessage(user.getUsername(), account, password, locale))
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.registration_success", locale));
			}
			else
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale), "login");
			}
		}
	}

	public String changeAccountPassword(User user, String account, String oldpass, String password, String passwordverify, Locale locale, HttpServletRequest request)
	{
		if(!password.equals(passwordverify)){
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("security.change_password_notverified", locale));
		}
		String browser = userIpService.findCurrentBrowser(request);
		XMLRPCMessage change = gameService.xmlrpcChangePassword(user, account, oldpass, password, locale);
		if(change == null)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CHANGEPASSWORD, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(change.getType() != XMLRPCMessage.MessageType.OK)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CHANGEPASSWORD, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, change.getMessage());
		}
		else{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_CHANGEPASSWORD, true, browser));
			if(mailFormatter.sendAccountChangeMessage(user.getUsername(), account, password, locale))
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.change_success", locale));
			}
			else
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale), "login");
			}
		}
	}

	public String recoverAccountPassword(User user, String account, Locale locale, HttpServletRequest request)
	{
		if(!user.isActive())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.need_verification", locale));
		}

		String browser = userIpService.findCurrentBrowser(request);
		String password = Rnd.getPassword();
		XMLRPCMessage recov = gameService.xmlrpcRecoverPassword(user, account, password, locale);
		if(recov == null)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_RECOVER, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(recov.getType() != XMLRPCMessage.MessageType.OK)
		{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_RECOVER, false, browser));
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, recov.getMessage());
		}
		else{
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACCOUNT_RECOVER, true, browser));
			if(mailFormatter.sendAccountRecoverMessage(user.getUsername(), account, password, locale))
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.recovery_success", locale));
			}
			else
			{
				return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale), "login");
			}
		}
	}

	public String sendVerifyMessage(User user, Locale locale) {

		if(!sendMail)
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale));

		String content;
		String token = UUID.randomUUID().toString();
		userService.createVerificationToken(user, token);

		if(mailFormatter.sendVerifyMessage(user.getUsername(),token, locale))
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("verification.success", locale));
		else
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale));
	}

	public String teleportCharacter(User user, String account, String player_id, Locale locale)
	{
		if(!accountService.findAccount(user.getId(), account))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));

		XMLRPCMessage message = gameService.xmlrpcTeleportPlayer(account, player_id, locale);
		if(message ==null){
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(message.getType() != XMLRPCMessage.MessageType.OK)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, message.getMessage());
		}
		else
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.teleport_success", locale));
		}
	}

	public String unlockCharacter(User user, String account, String player_id, Locale locale)
	{
		if(!accountService.findAccount(user.getId(), account))
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));

		try{
			if(!playerService.findOne(Long.parseLong(player_id)).isHwidLock())
				return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.unlock_notbinded", locale));
		}
		catch(Exception e){
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));
		}

		XMLRPCMessage message = gameService.xmlrpcUnlockPlayer(account, player_id, locale);
		if(message ==null){
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.connect_failed", locale));
		}
		else if(message.getType() != XMLRPCMessage.MessageType.OK)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, message.getMessage());
		}
		else
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("account.unlock_success", locale));
		}
	}

	public String cabinetChangeLang(){
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, "", "location.reload(true);");
	}

	/*@RequestMapping(value = "/setVoteIdentifier", method = { RequestMethod.POST })
	public @ResponseBody XMLRPCMessage setVoteIdentifier(String username, Locale locale)
	{
		if(!username.matches("[A-Za-z0-9]{6,16}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.vote_invalid_format", locale));
		}

		if(userIdentifierService.contains(username))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.vote_exists", locale));
		}

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserIdentifier userIdentifier = new UserIdentifier();
		userIdentifier.setUserId(user.getId());
		userIdentifier.setIdentifier(username);
		userIdentifierService.save(userIdentifier);
		return new XMLRPCMessage(XMLRPCMessage.MessageType.OK);
	}*/

}