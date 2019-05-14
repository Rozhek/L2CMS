package studio.lineage2.cms.service.captcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.regex.Pattern;

@Service
public class CaptchaService{
	private final static Logger LOGGER = LoggerFactory.getLogger(CaptchaService.class);

	@Value("${recaptchaKeySecret}")
	private String recaptchaKeySecret;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	private static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

	public void processResponse(final String response, final String clientIp) throws AuthenticationException
	{

		if(recaptchaKeySecret.isEmpty())
			return;

		LOGGER.debug("Attempting to validate response {}", response);

		if (!responseSanityCheck(response)) {
			throw new PreAuthenticatedCredentialsNotFoundException("wrongcaptcha");
		}

		final URI verifyUri = URI.create(String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s", recaptchaKeySecret, response, clientIp));
		try {
			final GoogleResponse googleResponse = restTemplate().getForObject(verifyUri, GoogleResponse.class);
			LOGGER.debug("Google's response: {} ", googleResponse.toString());

			if (!googleResponse.isSuccess()) {
				if (googleResponse.hasClientError()) {
					loginAttemptService.loginFailed(clientIp);
				}
				throw new PreAuthenticatedCredentialsNotFoundException("wrongcaptcha");
			}
		} catch (RestClientException rce) {
			throw new PreAuthenticatedCredentialsNotFoundException("trylater");
		}
	}

	private boolean responseSanityCheck(final String response) {
		return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
	}
}