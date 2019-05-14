package studio.lineage2.cms.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 Created by iRock
 29.11.2015
 */
@Component
public class SiteInterceptor extends HandlerInterceptorAdapter
{
	@Value("${domain}")
	private String domain;

	@Value("${xmlRpcRegAccount}")
	private boolean xmlRpcRegAccount;

	@Value("${forumLink}")
	private String forumLink;

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView model) throws Exception
	{
		if(model == null || model.getModel().size() == 0)
		{
			return;
		}
		model.addObject("domain", domain);
		model.addObject("xmlRpcRegAccount", xmlRpcRegAccount);
		model.addObject("forumLink", forumLink);
	}
}