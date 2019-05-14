package studio.lineage2.cms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import studio.lineage2.cms.service.captcha.CaptchaService;
import studio.lineage2.cms.service.captcha.LoginAttemptService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by iRock on 17.12.2016.
 */
public class CaptchaFilter extends AbstractAuthenticationProcessingFilter
{
	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private CaptchaService captchaService;

	private static final String POST = "POST";
	public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

	public CaptchaFilter() {
		super(new OrRequestMatcher(new AntPathRequestMatcher("/login", POST), new AntPathRequestMatcher("/register", POST), new AntPathRequestMatcher("/reminder", POST)));
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
			throws IOException, ServletException {
		super.unsuccessfulAuthentication(request, response, failed);
	}

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException
	{
		try {
			String ip = getClientIP(request);
			if (loginAttemptService.isBlocked(ip)) {
				throw new PreAuthenticatedCredentialsNotFoundException("blocked");
			}
			captchaService.processResponse(request.getParameter("g-recaptcha-response"),ip);
		}
		catch (InternalAuthenticationServiceException failed) {
			logger.error(
					"An internal error occurred while trying to authenticate the user.",
					failed);
			unsuccessfulAuthentication(request, response, failed);

			return;
		}
		catch (AuthenticationException failed) {
			// Authentication failed
			unsuccessfulAuthentication(request, response, failed);

			return;
		}

		chain.doFilter(request, response);
	}

	protected String getAlreadyFilteredAttributeName() {
		String name = getFilterName();
		if (name == null) {
			name = getClass().getName();
		}
		return name + ALREADY_FILTERED_SUFFIX;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("OncePerRequestFilter just supports HTTP requests");
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;


		if (!requiresAuthentication(httpRequest, httpResponse) || !httpRequest.getMethod().equals(POST)) {
			chain.doFilter(request, response);
			return;
		}

		String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
		boolean hasAlreadyFilteredAttribute = request.getAttribute(alreadyFilteredAttributeName) != null;

		if (hasAlreadyFilteredAttribute) {

			// Proceed without invoking this filter...
			chain.doFilter(request, response);
		}
		else {
			// Do invoke this filter...
			request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
			try {
				doFilterInternal(httpRequest, httpResponse, chain);
			}
			finally {
				// Remove the "already filtered" request attribute for this request.
				request.removeAttribute(alreadyFilteredAttributeName);
			}
		}
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException
	{
		return null;
	}

	private String getClientIP(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null){
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}
