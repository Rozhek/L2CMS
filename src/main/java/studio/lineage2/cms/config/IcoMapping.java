package studio.lineage2.cms.config;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.context.annotation.Configuration;

/**
 * Created by iRock on 07.11.2017.
 */
@Configuration
public class IcoMapping implements EmbeddedServletContainerCustomizer
{
	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("ico", "image/x-icon");
		container.setMimeMappings(mappings);
	}
}
