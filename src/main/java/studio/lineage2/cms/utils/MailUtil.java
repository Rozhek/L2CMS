package studio.lineage2.cms.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 Created by iRock
 24.11.2015
 */
@Component
public class MailUtil
{
	@Value("${smtp}")
	private String smtp;

	@Value("${port}")
	private int port;

	@Value("${email}")
	private String email;

	@Value("${password}")
	private String password;

	@Value("${ssl}")
	private boolean ssl;

	@Value("${ssl.trust.all}")
	private boolean trustAll;

	@Value("${mail.from}")
	private String from;

	@Value("${mail.from_name}")
	private String from_name;

	private JavaMailSenderImpl mailSender;

	public void send(String to, String title, String content, boolean htmlMail)
	{
		if(smtp.isEmpty())
		{
			return;
		}

		if(mailSender == null)
		{
			mailSender = new JavaMailSenderImpl();
			mailSender.setProtocol("smtp");
			mailSender.setHost(smtp);
			mailSender.setPort(port);
			mailSender.setUsername(email);
			mailSender.setPassword(password);

			Properties mailProps = new Properties();
			mailProps.put("mail.smtp.auth", "true");
			mailProps.put("mail.smtp.ssl.enable", ssl);
			if(trustAll) mailProps.put("mail.smtp.ssl.trust", "*");

			mailSender.setJavaMailProperties(mailProps);
		}

		try
		{
			final MimeMessage mimeMessage = mailSender.createMimeMessage();
			final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
			message.setFrom(new InternetAddress(from, from_name));
			message.setTo(to);
			message.setSubject(title);
			message.setText(content, htmlMail);
			mailSender.send(mimeMessage);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
}