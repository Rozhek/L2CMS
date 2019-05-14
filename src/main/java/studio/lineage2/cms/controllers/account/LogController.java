package studio.lineage2.cms.controllers.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.model.Player;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.model.UserItemLog;
import studio.lineage2.cms.repository.GameServerRepository;
import studio.lineage2.cms.service.AccountService;
import studio.lineage2.cms.service.PlayerService;
import studio.lineage2.cms.service.UserIpService;
import studio.lineage2.cms.service.UserItemsLogService;
import studio.lineage2.cms.utils.LocalizePath;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 Created by iRock
 23.10.2015
 */
@Controller
@RequestMapping(value = "/account/log", method = { RequestMethod.GET })
public class LogController
{
	@Autowired
	private AccountService accountService;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	private UserItemsLogService userItemsLogService;

	@Autowired
	private GameServerRepository gameServerRepository;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String showLogs(ModelMap model, Locale locale)
	{

		model.addAttribute("logmenu", true);

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

		List<UserLog> userLogs = new ArrayList<UserLog>();
		for(UserIp userIp : userIpService.findLastActions(user.getId(), 50)){
			if(userIp.getActionText()==null)
				userLogs.add(new UserLog(userIp, userItemsLogService.findOne(userIp.getAction())));
			else
				userLogs.add(new UserLog(userIp));
		}
		model.addAttribute("userLogs", userLogs);
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/account/log.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	public class UserLog{
		private String type, msg, time, ip, browser, param;
		public UserLog(UserIp logIp){
			type = logIp.getType();
			msg = logIp.getActionText();
			time = logIp.getTime();
			ip = logIp.getIp();
			browser = logIp.getBrowser();
			param = "";
		}
		public UserLog(UserIp logIp, UserItemLog logItem){
			type = logIp.getType();
			msg = logItem.getText();
			time = logIp.getTime();
			ip = logIp.getIp();
			browser = logIp.getBrowser();
			param = logItem.getTextParam() == null ? "" : logItem.getTextParam();
		}
		public String getType(){return type;}
		public String getMsg(){return msg;}
		public String getTime(){return time;}
		public String getIp(){return ip;}
		public String getBrowser(){return browser;}
		public String getParam(){return param;}

	}

}