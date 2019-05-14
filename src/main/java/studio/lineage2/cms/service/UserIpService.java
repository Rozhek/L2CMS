package studio.lineage2.cms.service;

import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.repository.UserIpRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 01.11.2015
 */
@Service
public class UserIpService
{
	@Autowired
	@Qualifier("userIpRepository")
	private UserIpRepository userIpRepository;

	@Value("${server.proxy.cloudflare}")
	private boolean CFEnabled;

	public void save(UserIp userIp)
	{
		userIpRepository.save(userIp);
	}

	public UserIp findLastUserIp(long userId)
	{
		return userIpRepository.findByUserId(userId).stream().filter(userip -> userip.getAction()== 0).sorted(Comparator.comparing(UserIp::getId).reversed()).findFirst().orElse(null);
	}

	public List<UserIp> findLastUserIp(long userId, int count)
	{

		return userIpRepository.findByUserId(userId).stream().filter(userip -> userip.getAction()== 0).sorted(Comparator.comparing(UserIp::getId).reversed()).limit(count).collect(Collectors.toList());
	}

	public List<UserIp> findLastActions(long userId, int count)
	{
		return userIpRepository.findByUserId(userId).stream().sorted(Comparator.comparing(UserIp::getId).reversed()).limit(count).collect(Collectors.toList());
	}

	public String findCurrentBrowser(HttpServletRequest request){
		UserAgent agent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
		return agent.getBrowser().getManufacturer().name().substring(0, 1) + agent.getBrowser().getManufacturer().name().toLowerCase().substring(1)+ " " + agent.getBrowser().getName();
	}

	public String getProxifiedAddress(HttpServletRequest request){
		return CFEnabled? request.getHeader("X-FORWARDED-FOR").split(",")[0].trim() : request.getRemoteAddr();
	}

	public String getProxifiedAddress(HttpServletRequest request, WebAuthenticationDetails details){
		return CFEnabled? request.getHeader("X-FORWARDED-FOR").split(",")[0].trim() : details.getRemoteAddress();
	}
}