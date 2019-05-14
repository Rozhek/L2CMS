package studio.lineage2.cms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.lineage2.cms.controllers.account.SupportController;
import studio.lineage2.cms.controllers.admin.AdminSupportController;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.service.RateLimitService;
import studio.lineage2.cms.utils.Rnd;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

/**
 Created by iRock
 23.10.2015
 */


@RestController
@RequestMapping(value = "/data", headers = "x-requested-with=XMLHttpRequest")
public class DataController
{
	@Autowired
	private AccountController accountController;

	@Autowired
	private PayController payController;

	@Autowired
	private SupportController supportController;

	@Autowired
	private AdminSupportController adminSupportController;

	@Autowired
	private RateLimitService rateLimitService;

	@RequestMapping(value = "/prefix/get", method = RequestMethod.GET)
	public String prefix()
	{
		return Rnd.getPrefix();
	}

	@RequestMapping(value = "/controllers", method = RequestMethod.POST)
	public ResponseEntity formData(String module, @RequestParam Map<String,String> params, Locale locale, HttpServletRequest request)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = null;
		if(object instanceof User)
			user = (User) object;


		if(module == null || module.isEmpty() || user == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

		if(rateLimitService.isBlocked(request.getRemoteAddr()))
			return ResponseEntity.ok(rateLimitService.getMessage(locale));
		rateLimitService.put(request.getRemoteAddr());

		String value;
		switch(module)
		{
			case "global_informaiton_block":
				if((value = params.get("open")) != null && value.equals("open_logs_auth"))
				{
					return ResponseEntity.ok(accountController.getIpLog(user, locale));
				}
				else if((value = params.get("open")) != null && value.equals("change_password_ma"))
				{
					return ResponseEntity.ok(accountController.getMainPasswordForm(user, locale));
				}
				else if((value = params.get("action")) != null && value.equals("change_password_ma"))
				{
					return ResponseEntity.ok(accountController.changeMainPassword(params.get("change-password-old"), params.get("change-password-new"), params.get("change-password-verify"), locale, request));
				}
				else if((value = params.get("action")) != null && value.equals("send_verification"))
				{
					String verifyKey = "mail_".concat(request.getRemoteAddr());
					if(rateLimitService.isBlocked(verifyKey))
						return ResponseEntity.ok(rateLimitService.getMailMessage(locale));
					rateLimitService.putMail(verifyKey);
					return ResponseEntity.ok(accountController.sendVerifyMessage(user, locale));
				}
				break;
			case "head_wedget_main":
				if((value = params.get("action")) != null && value.equals("balance_update"))
					return ResponseEntity.ok(accountController.getMoneyCount(user, locale));
				break;
			case "block_wedget_support":
				if((value = params.get("act")) != null && value.equals("open-tickets"))
					return ResponseEntity.ok(supportController.view(user, locale, params.get("idt")));

				if((value = params.get("act")) != null && value.equals("send_answer_ticket"))
					return ResponseEntity.ok(supportController.addMessage(user, locale, params.get("tickets-id"), params.get("tickets-reply")));

				if((value = params.get("act")) != null && value.equals("tickets-new"))
					return ResponseEntity.ok(supportController.getTicketForm(user, locale, "ticketform"));

				if((value = params.get("act")) != null && value.equals("tickets-new-quick-app-bot"))
					return ResponseEntity.ok(supportController.getTicketForm(user, locale, "botform"));

				if((value = params.get("act")) != null && value.equals("tickets-new-quick-app"))
					return ResponseEntity.ok(supportController.getTicketForm(user, locale, "donationform"));

				if((value = params.get("act")) != null && value.equals("tickets-new-quick-drop-item"))
					return ResponseEntity.ok(supportController.getTicketForm(user, locale, "itemform"));

				if((value = params.get("send")) != null && value.equals("tickets-new"))
					return ResponseEntity.ok(supportController.addTicket(user, locale, params));

				if((value = params.get("send")) != null && value.equals("tickets-new-bot"))
					return ResponseEntity.ok(supportController.addBotTicket(user, locale, params));

				if((value = params.get("send")) != null && value.equals("tickets-new-donation"))
					return ResponseEntity.ok(supportController.addDonationTicket(user, locale, params));

				if((value = params.get("send")) != null && value.equals("tickets-new-drop-item"))
					return ResponseEntity.ok(supportController.addItemTicket(user, locale, params));

				if((value = params.get("act")) != null && value.equals("open-tickets-admin"))
					return ResponseEntity.ok(adminSupportController.view(user, locale, params.get("idt")));

				if((value = params.get("act")) != null && value.equals("admin_answer_ticket"))
					return ResponseEntity.ok(adminSupportController.addMessage(user, locale, params.get("tickets-id"), params.get("tickets-reply"), params.get("support_type")));
				break;
			case "block_wedget_payment_agrigator":
				if((value = params.get("method")) != null)
					return ResponseEntity.ok(payController.init(user, value, params.get("sum"), params.get("account"), locale));
				break;
			case "buttons_buy_coins":
				if((value = params.get("char")) != null)
					return ResponseEntity.ok(accountController.addCoins(user, params.get("account"), value, params.get("count"), locale, request));
				break;
			case "buttons_bonus_code":
				if((value = params.get("bonus-cod")) != null)
					return ResponseEntity.ok(accountController.addBonus(user, value, locale));
				break;
			case "buttons_create_account":
				if((value = params.get("register-account")) != null)
					return ResponseEntity.ok(accountController.registerAccount(user, params.get("register-prefix"), value, params.get("register-password"), params.get("register-password-verify"), locale, request));
				break;
			case "buttons_add_account":
				if((value = params.get("register-account")) != null)
					return ResponseEntity.ok(accountController.addAccount(user, value, params.get("register-password"), locale, request));
				break;
			case "account_list_all":
				if((value = params.get("account_list_refresh")) != null && value.equals("1"))
				{
					return ResponseEntity.ok(accountController.updateAccounts(user, locale));
				}
				else if((value = params.get("open")) != null && value.equals("change"))
				{
					return ResponseEntity.ok(accountController.getAccPasswordForm(user, params.get("account_name"), locale));
				}
				else if((value = params.get("open")) != null && value.equals("recovery"))
				{
					return ResponseEntity.ok(accountController.getAccRecoveryForm(user, params.get("account_name"), locale));
				}
				else if(params.get("teleport-account") != null && params.get("teleport-char") != null)
				{
					return ResponseEntity.ok(accountController.teleportCharacter(user, params.get("teleport-account"), params.get("teleport-char"), locale));
				}
				else if(params.get("unlock-account") != null && params.get("unlock-char") != null)
				{
					return ResponseEntity.ok(accountController.unlockCharacter(user, params.get("unlock-account"), params.get("unlock-char"), locale));
				}
				else if((value = params.get("change-password-account")) != null)
				{
					return ResponseEntity.ok(accountController.changeAccountPassword(user, value, params.get("change-password-old"), params.get("change-password-new"), params.get("change-password-verify"), locale, request));
				}
				else if((value = params.get("recovery-password-account")) != null)
				{
					return ResponseEntity.ok(accountController.recoverAccountPassword(user, value, locale, request));
				}
				break;
			case "select_language":
				if((value = params.get("lang")) != null && (value.equals("en") || value.equals("ru")))
					return ResponseEntity.ok(accountController.cabinetChangeLang());
				break;
		}


		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
}

