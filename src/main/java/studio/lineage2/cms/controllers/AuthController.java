package studio.lineage2.cms.controllers;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import studio.lineage2.cms.model.VerificationToken;
import studio.lineage2.cms.service.GameService;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.MailFormatterService;
import studio.lineage2.cms.service.UserIpService;
import studio.lineage2.cms.service.UserService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.utils.Rnd;
import studio.lineage2.cms.xmlrpc.JsonMessage;
import studio.lineage2.cms.xmlrpc.XMLRPCMessage;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 Created by iRock
 23.10.2015
 */
@Controller
public class AuthController
{
	@Value("${recaptchaKeySite}")
	private String recaptchaKeySite;

	@Value("${verify}")
	private boolean verify;

	@Value("${xmlRpcRegAccount}")
	private boolean xmlRpcRegAccount;

	@Autowired
	private MailFormatterService mailFormatter;

	@Autowired
	private UserService userService;

	@Resource
	private ProviderManager authenticationManager;

	@Autowired
	private GameService gameService;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	VelocityEngine velocityEngine;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/login", method = { RequestMethod.GET }, headers = "x-requested-with!=XMLHttpRequest")
	public String show(ModelMap model, HttpServletRequest request, HttpServletResponse response, Locale locale)
	{
		Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(user instanceof User)
		{
			return "redirect:/account";
		}
		else
		{
			model.addAttribute("prefix", Rnd.getPrefix());
			model.addAttribute("recaptchaKeySite", recaptchaKeySite);
			model.addAttribute("authmenu", true);
			model.addAttribute("page", LocalizePath.build(locale, "cp/$$/auth.vm"));
			return LocalizePath.build(locale, "cp/$$/index");
		}
	}

	@RequestMapping(value = "/register", method = { RequestMethod.POST }, headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody String reg(ModelMap model, @ModelAttribute @Valid User user, BindingResult result, @RequestParam Map<String,String> params, HttpServletRequest request,Locale locale)
	{
		if(result.hasFieldErrors())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, result.getFieldErrors().get(0).getDefaultMessage(),"grecaptcha.reset(recaptcha3_key)");
		}
		if(result.hasErrors())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("registration.error", locale), "grecaptcha.reset(recaptcha3_key)");
		}

		if(xmlRpcRegAccount && !params.get("account").matches("[A-Za-z0-9]{4,11}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.login_invalid", locale),"grecaptcha.reset(recaptcha3_key)");
		}

		if(params.get("password-verify")==null || !params.get("password-verify").equals(user.getPassword()))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("registration.verify_password", locale),"grecaptcha.reset(recaptcha3_key)");
		}

		if(params.get("terms")==null || !params.get("terms").equalsIgnoreCase("on"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("registration.empty_terms", locale),"grecaptcha.reset(recaptcha3_key)");
		}

		if(userService.checkUserByName(user.getUsername()))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("registration.exist_username", locale, new String[]{user.getUsername()}),"grecaptcha.reset(recaptcha3_key)");
		}

		String password = user.getPassword();
		user.setPassword(new BCryptPasswordEncoder().encode(password));
		user.setIsAdmin(false);
		user.setActive(!verify);
		userService.addUser(user);
		String token = null;
		if(verify){
			token = UUID.randomUUID().toString();
			userService.createVerificationToken(user, token);
		}
		userService.saveUser(user);

		XMLRPCMessage tryReg = xmlRpcRegAccount ? gameService.xmlrpcRegister(user, params.get("prefix"), params.get("account"), password, locale) : null;
		if(tryReg != null && tryReg.getType()!= XMLRPCMessage.MessageType.OK)
			logger.warn("Cannot create account: " + params.get("prefix")+params.get("account") + " password: " + password + " userId: " + user.getId()+
			"\nErrorTrace: " + tryReg.getMessage());
		boolean trySend = mailFormatter.sendRegisterMessage(user.getUsername(), password, params.get("prefix"), params.get("account"), tryReg, token, locale);

		if(!trySend)
			return JsonMessage.generateMessage(JsonMessage.JsonType.info, localizedMessage.getMessage("registration.succcess_mail_failed", locale), "$('#modal-reg-nomail').trigger('click'); $('#modal-boxRegNomail').trigger('click');");
		else if(tryReg == null || tryReg.getType() != XMLRPCMessage.MessageType.OK)
			return JsonMessage.generateMessage(JsonMessage.JsonType.info, localizedMessage.getMessage("registration.succcess_with_problem", locale), "$('#modal-reg-mail').trigger('click'); $('#modal-boxRegMail').trigger('click');");
		else
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("registration.success", locale), "$('#modal-reg-mail').trigger('click'); $('#modal-boxRegMail').trigger('click');");
	}

	@RequestMapping(value = "/confirmation", method = RequestMethod.GET)
	public String confirmRegistration(HttpServletRequest request, ModelMap model, @RequestParam("token") String token, Locale locale) {

		VerificationToken verificationToken = userService.getVerificationToken(token);
		if (verificationToken == null) {
			model.addAttribute("messagetitle", localizedMessage.getMessage("message.token_invalid_title", locale));
			model.addAttribute("messagetext", localizedMessage.getMessage("message.token_invalid_message", locale));
			return LocalizePath.build(locale, "cp/$$/message");
		}

		String browser = userIpService.findCurrentBrowser(request);
		User user = verificationToken.getUser();
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User sess_user = null;
		if(object instanceof User)
			sess_user = (User) object;
		if(sess_user!=null && sess_user.getId() == user.getId())
		{
			user = sess_user;
		}

		if (user.isActive()) {
			model.addAttribute("messagetitle", localizedMessage.getMessage("message.token_invalid_title", locale));
			model.addAttribute("messagetext", localizedMessage.getMessage("message.token_invalid_message", locale));
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACTIVATE, false, browser));
			return LocalizePath.build(locale, "cp/$$/message");
		}

		Calendar cal = Calendar.getInstance();
		if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			model.addAttribute("messagetitle", localizedMessage.getMessage("message.token_invalid_title", locale));
			model.addAttribute("messagetext", localizedMessage.getMessage("message.token_invalid_message", locale));
			userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACTIVATE, false, browser));
			return LocalizePath.build(locale, "cp/$$/message");
		}

		user.setActive(true);
		userService.saveUser(user);

		model.addAttribute("messagetitle", localizedMessage.getMessage("message.email_verify_title", locale));
		model.addAttribute("messagetext", localizedMessage.getMessage("message.email_verify_message", locale));
		userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.ACTIVATE, true, browser));
		return LocalizePath.build(locale, "cp/$$/message");
	}

	@RequestMapping(value = "/reminder", method = { RequestMethod.POST }, headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody String recover(ModelMap model, String username, Locale locale, HttpServletRequest request)
	{
		if(!userService.checkUserByName(username))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("recover.wrong_username", locale),"grecaptcha.reset(recaptcha2_key)");
		}

		User user = userService.findUserByName(username);
		if(!user.isActive())
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.need_verification", locale));
		}

		String password = Rnd.getPassword();
		user.setPassword(new BCryptPasswordEncoder().encode(password));
		userService.saveUser(user);
		String browser = userIpService.findCurrentBrowser(request);

		userIpService.save(new UserIp(user.getId(), userIpService.getProxifiedAddress(request), Calendar.getInstance().getTimeInMillis(), UserIp.Action.RECOVER, true, browser));
		if(mailFormatter.sendRecoverMessage(user.getUsername(), password, locale))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("recover.success", locale), "login");
		}
		else
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("simplemsg.mail_failed", locale), "login");
		}
	}

	public void autoLogin(HttpServletRequest request, User user, String password) {
		try {
			// Must be called from request filtered by Spring Security, otherwise SecurityContextHolder is not updated
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), password);
			token.setDetails(new WebAuthenticationDetails(request));
			Authentication authentication = authenticationManager.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

}