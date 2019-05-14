package studio.lineage2.cms.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import studio.lineage2.cms.model.News;
import studio.lineage2.cms.service.NewsService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 Created by iRock
 29.10.2015
 */
@Controller
@RequestMapping("/admin/news")
public class AdminNewsController
{
	@Autowired
	private NewsService newsService;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		model.addAttribute("news", new News());
		model.addAttribute("adminmenu", true);
		List<News> newsList = newsService.findAll();
		Collections.reverse(newsList);
		model.addAttribute("newsList", newsList);

		model.addAttribute("container", LocalizePath.build(locale, "cp/$$/admin/news/create.vm"));
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/news.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "add", method = { RequestMethod.POST })
	public @ResponseBody String add(ModelMap model, @ModelAttribute News news, @RequestParam("file") MultipartFile file)
	{
		if(news.getTitle().isEmpty() || news.getContent().isEmpty() || news.getLang().isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Заполните все поля");

		news.setDate(Calendar.getInstance().getTimeInMillis());
		try
		{
			news.setImage(file.getBytes());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		newsService.save(news);
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Новость успешно добавлена", "setTimeout(function(){location.reload(true);}, 1500);");
	}

	@RequestMapping(value = "remove/{id}", method = { RequestMethod.GET })
	public String remove(ModelMap model, @PathVariable long id, Locale locale)
	{
		newsService.delete(id);
		return show(model, locale);
	}

	@RequestMapping(value = "edit/{id}", method = { RequestMethod.GET })
	public String edit(ModelMap model, @PathVariable long id, Locale locale)
	{
		model.addAttribute("news", newsService.findOne(id));
		model.addAttribute("adminmenu", true);
		List<News> newsList = newsService.findAll();
		Collections.reverse(newsList);
		model.addAttribute("newsList", newsList);
		model.addAttribute("container", LocalizePath.build(locale, "cp/$$/admin/news/edit.vm"));
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/news.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "edit", method = { RequestMethod.POST })
	public @ResponseBody String edit(ModelMap model, long id, @ModelAttribute News news, @RequestParam("file") MultipartFile file)
	{
		if(news.getTitle().isEmpty() || news.getContent().isEmpty() || news.getLang().isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Заполните все поля");

		News news1 = newsService.findOne(id);
		news1.setTitle(news.getTitle());
		news1.setContent(news.getContent());
		news1.setLang(news.getLang());
		news1.setLink(news.getLink());
		try
		{
			if(file.getSize() > 0)
			{
				news1.setImage(file.getBytes());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		newsService.save(news1);
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Новость успешно изменена", "setTimeout(function(){location.reload(true);}, 1500);");
	}
}