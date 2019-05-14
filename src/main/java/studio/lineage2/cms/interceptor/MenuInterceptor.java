package studio.lineage2.cms.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import studio.lineage2.cms.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 Created by iRock
 02.11.2015
 */
@Component
public class MenuInterceptor extends HandlerInterceptorAdapter
{

	@Value("${domain}")
	private String domain;

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView model) throws Exception
	{
		if(model == null || model.getModel().size() == 0)
		{
			return;
		}
		model.addObject("domain", domain);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null)
		{
			Object o = authentication.getPrincipal();
			if(o instanceof User)
			{
				User user = (User) o;
				model.addObject("name", user.getUsername());
				model.addObject("isAdmin", user.getIsAdmin());
				model.addObject("isActive", user.isActive());
			}
		}
	}
}