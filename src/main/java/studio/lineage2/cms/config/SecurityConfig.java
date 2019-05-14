package studio.lineage2.cms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.UserService;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 Created by iRock
 19.10.2015
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
	@Autowired
	private UserService userService;

	@Value("${unitpayVerificationFile}")
	private String unitpayVerificationFile;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@Value("${recaptchaKeySecret}")
	private String recaptchaKeySecret;

	@Bean
	public FilterRegistrationBean captchaValidation() throws Exception {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(captchaFilter());
		registrationBean.addUrlPatterns("/register");
		registrationBean.addUrlPatterns("/reminder");
		registrationBean.setOrder(1);
		return registrationBean;
	}

	@Bean
	CaptchaFilter captchaFilter() throws Exception
	{
		CaptchaFilter captchaFilter = new CaptchaFilter();
		captchaFilter.setAuthenticationManager(authenticationManager());
		captchaFilter.setAuthenticationFailureHandler(failureHandler());
		return captchaFilter;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.
				csrf().disable().
				addFilterBefore(captchaFilter(),UsernamePasswordAuthenticationFilter.class).
				authorizeRequests().
				antMatchers("/", "/stats", "/support", "/reg", "/login", "/register","/reminder", "/confirmation",
						"/error", "/data/prefix/get", "/**/css/**", "/**/js/**", "/**/fonts/**", "/**/img/**", "/en",
						"/**/image/**", "/**/images/**", "/contest/**", "/favicon.ico", "/success", "/failed",
						"/patch/**").permitAll().
				antMatchers("/robots.txt", "/sitemap.xml","/pay/check*", unitpayVerificationFile).anonymous().
				antMatchers("/admin/**").hasRole("ADMIN").
				anyRequest().hasRole("USER").and().
				formLogin().loginPage("/login").defaultSuccessUrl("/account").successHandler(successHandler()).failureHandler(failureHandler()).and().
				logout().logoutUrl("/logout").logoutSuccessUrl("/").and().
				headers().cacheControl().disable().and().
				rememberMe();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.userDetailsService(userService::findUserByName).passwordEncoder(new BCryptPasswordEncoder());
	}

	private SimpleUrlAuthenticationSuccessHandler successHandler() {
		return new SimpleUrlAuthenticationSuccessHandler() {
			private RequestCache requestCache = new HttpSessionRequestCache();

			@Override
			public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException
			{
				SavedRequest savedRequest = requestCache.getRequest(httpServletRequest, httpServletResponse);

				if (savedRequest == null) {
					httpServletResponse.getWriter().append(JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("security.login_success", httpServletRequest), "location.reload(true);"));
					httpServletResponse.setStatus(200);
					return;
				}

				clearAuthenticationAttributes(httpServletRequest);

				// Use the DefaultSavedRequest URL
				String targetUrl = ((DefaultSavedRequest)savedRequest).getRequestURI().substring(1);
				httpServletResponse.getWriter().append(JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("security.login_success", httpServletRequest), targetUrl));
				httpServletResponse.setStatus(200);}
		};
	}

	private SimpleUrlAuthenticationFailureHandler failureHandler() {
		return new SimpleUrlAuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

				String mode = null;
				if(!recaptchaKeySecret.isEmpty())
				{
					final String URI = httpServletRequest.getRequestURI();
					switch(URI)
					{
						case "/login":
							mode = "grecaptcha.reset(recaptcha1_key)";
							break;
						case "/reminder":
							mode = "grecaptcha.reset(recaptcha2_key)";
							break;
						case "/register":
							mode = "grecaptcha.reset(recaptcha3_key)";
							break;
					}
				}
				String errorMessage = JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("security.wrong_password", httpServletRequest),mode);
				if (e.getMessage().equalsIgnoreCase("blocked")) {
					errorMessage = JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("security.login_blocked", httpServletRequest),mode);
				}
				else if(e.getMessage().equalsIgnoreCase("wrongcaptcha")) {
					errorMessage = JsonMessage.generateMessage(JsonMessage.JsonType.warning, localizedMessage.getMessage("security.wrong_captcha", httpServletRequest),mode);
				}
				else if(e.getMessage().equalsIgnoreCase("trylater")) {
					errorMessage = JsonMessage.generateMessage(JsonMessage.JsonType.info, localizedMessage.getMessage("security.login_trylater", httpServletRequest),mode);
				}
				httpServletResponse.getWriter().append(errorMessage);
				httpServletResponse.setStatus(200);
			}
		};
	}
}