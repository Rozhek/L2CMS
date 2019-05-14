package studio.lineage2.cms.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by iRock on 18.06.2017.
 */
@Service
public class RateLimitService
{
	private final int MIN_SECS = 1;
	private final int MAIL_SECS = 300;
	private LoadingCache<String, Long> limiterCache;

	@Autowired
	private LocalizedMessageService localizedMessage;

	public RateLimitService() {
		super();
		limiterCache = CacheBuilder.newBuilder().
				expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Long>() {
			public Long load(String key) {
				return 0L;
			}
		});
	}

	public void put(String key) {
		limiterCache.put(key, System.currentTimeMillis() + MIN_SECS * 1000L);
	}

	public void putMail(String key) {
		limiterCache.put(key, System.currentTimeMillis() + MAIL_SECS * 1000L);
	}

	public boolean isBlocked(String key) {
		try {
			return limiterCache.get(key) >= System.currentTimeMillis();
		} catch (ExecutionException e) {
			return false;
		}
	}

	public String getMessage(Locale locale)
	{
		return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("security.too_fast", locale));
	}

	public boolean isMailBlocked(String key) {
		try {
			return limiterCache.get(key) >= System.currentTimeMillis();
		} catch (ExecutionException e) {
			return false;
		}
	}

	public String getMailMessage(Locale locale)
	{
		return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("security.too_fast_mail", locale));
	}
}
