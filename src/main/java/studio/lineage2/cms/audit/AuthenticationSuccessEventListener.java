package studio.lineage2.cms.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.service.GameService;
import studio.lineage2.cms.service.UserIpService;
import studio.lineage2.cms.service.UserService;
import studio.lineage2.cms.service.captcha.LoginAttemptService;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 Created by iRock
 01.11.2015
 */
@Component
public class AuthenticationSuccessEventListener
		implements ApplicationListener<AuthenticationSuccessEvent> {

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	private GameService gameService;

	public void onApplicationEvent(AuthenticationSuccessEvent e) {

		User user = userService.findUserByName(e.getAuthentication().getName());
		long userId = user.getId();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String ip = userIpService.getProxifiedAddress(request, ((WebAuthenticationDetails) e.getAuthentication().getDetails()));
		long date = Calendar.getInstance().getTimeInMillis();
		String browser = userIpService.findCurrentBrowser(request);

		loginAttemptService.loginSucceeded(ip);
		userIpService.save(new UserIp(userId, ip, date, UserIp.Action.AUTHORIZE, true, browser));
		gameService.xmlrpcUpdatePlayers(user);
	}
}