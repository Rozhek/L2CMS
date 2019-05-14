package studio.lineage2.cms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import studio.lineage2.cms.interceptor.MenuInterceptor;
import studio.lineage2.cms.interceptor.SiteInterceptor;

import java.util.Locale;

/**
 Created by iRock
 02.11.2015
 */
@Configuration
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:./conf/ext.properties", ignoreResourceNotFound = true)
@EnableScheduling
public class WebConfig extends WebMvcConfigurerAdapter
{
	@Value("${spring.messageSource}")
	private String messageSourceString;

	@Autowired
	@Qualifier("menuInterceptor")
	private MenuInterceptor menuInterceptor;

	@Autowired
	@Qualifier("siteInterceptor")
	private SiteInterceptor siteInterceptor;

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setCacheSeconds(43200);
		messageSource.setBasename(messageSourceString);
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource());
		return bean;
	}

	@Override
	public Validator getValidator() {
		return validator();
	}

	@Bean
	public LocaleResolver localeResolver(){
		CookieLocaleResolver resolver = new CookieLocaleResolver();
		resolver.setDefaultLocale(new Locale("ru"));
		resolver.setCookieName("lang");
		resolver.setCookieMaxAge(432000);
		return resolver;
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("images", "news", "blocks");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		registry.addInterceptor(menuInterceptor);
		registry.addInterceptor(siteInterceptor);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// turn off all suffix pattern matching
		configurer.setUseSuffixPatternMatch(false);
		//configurer.setUseRegisteredSuffixPatternMatch(true);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
	}
}