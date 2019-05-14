package studio.lineage2.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 Created by iRock
 19.10.2015
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer
{
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}
}