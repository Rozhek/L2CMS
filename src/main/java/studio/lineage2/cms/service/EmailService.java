package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Email;
import studio.lineage2.cms.repository.EmailRepository;
import studio.lineage2.cms.utils.MailUtil;

/**
 Created by iRock
 01.12.2015
 */
@Service
public class EmailService
{
	@Autowired
	@Qualifier("emailRepository")
	private EmailRepository emailRepository;

	@Autowired
	private MailUtil mailUtil;

	public void send(String title, String content)
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				for(Email email : emailRepository.findAll())
				{
					email.setSend(false);
					emailRepository.save(email);
				}
				for(Email email : emailRepository.findAll())
				{
					mailUtil.send(email.getEmail(), title, content, false);
					email.setSend(true);
					emailRepository.save(email);
					try
					{
						sleep(1000);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	public void sendTest(String email, String title, String content)
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				mailUtil.send(email, title, content, false);
			}
		};
		thread.start();
	}
}