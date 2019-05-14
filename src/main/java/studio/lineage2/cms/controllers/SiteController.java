package studio.lineage2.cms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import studio.lineage2.cms.model.News;
import studio.lineage2.cms.repository.ForumRepository;
import studio.lineage2.cms.repository.GameServerRepository;
import studio.lineage2.cms.service.NewsService;
import studio.lineage2.cms.service.BlockService;
import studio.lineage2.cms.service.ShopPacketService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.utils.Rnd;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 Created by iRock
 23.10.2015
 */
@Controller
@RequestMapping("/")
public class SiteController implements ErrorController
{
	@Value("${hostname}")
	private String hostname;

	@Value("${forumType}")
	private String forumType;

	@Value("${onlineRate}")
	private double onlineRate;

	@Value("${onlineSub}")
	private int onlineSub;

	@Value("${recaptchaKeySite}")
	private String recaptchaKeySite;

	@Autowired
	private NewsService newsService;

	@Autowired
	private BlockService blockService;

	@Autowired
	private ShopPacketService shopPacketService;

	@Autowired
	private GameServerRepository gameServerRepository;

	@RequestMapping(method = {
			RequestMethod.GET,
			RequestMethod.HEAD
	})
	public String showIndex(ModelMap model, Locale locale)
	{
		List<News> newsList = newsService.findAll(locale.getLanguage());
		Collections.reverse(newsList);
		model.addAttribute("bannerList", blockService.findAll(1, locale.getLanguage()));
		model.addAttribute("streamList", blockService.findAll(2, locale.getLanguage()));
		model.addAttribute("newsList", newsList);
		model.addAttribute("themeList", ForumRepository.getThemes());
		int online = (int)(gameServerRepository.getOnline() * onlineRate);
		model.addAttribute("online", online);
		model.addAttribute("percent", Math.min(online*100/onlineSub,100));
		model.addAttribute("prizepool", blockService.getVar("pool_amount"));
		if(gameServerRepository.getTopPvP() != null)
		{
			model.addAttribute("topPvPList", gameServerRepository.getTopPvP(10));
		}
		if(gameServerRepository.getTopPK() != null)
		{
			model.addAttribute("topPKList", gameServerRepository.getTopPK(10));
		}
		if(gameServerRepository.getTopLevel() != null)
		{
			model.addAttribute("topLevelList", gameServerRepository.getTopLevel(10));
		}
		model.addAttribute("forumType", forumType);

		model.addAttribute("page", LocalizePath.build(locale, "site/$$/main.vm"));
		return LocalizePath.build(locale, "site/$$/index");
	}

	@RequestMapping(value={"/reg"}, method = RequestMethod.GET)
	public String showRegPage(ModelMap model, Locale locale)
	{
		model.addAttribute("prefix", Rnd.getPrefix());
		model.addAttribute("recaptchaKeySite", recaptchaKeySite);
		int online = (int)(gameServerRepository.getOnline() * onlineRate);
		model.addAttribute("online", online);
		model.addAttribute("percent", Math.min(online*100/onlineSub,100));
		model.addAttribute("prizepool", blockService.getVar("pool_amount"));

		model.addAttribute("page", LocalizePath.build(locale, "site/$$/reg.vm"));
		return LocalizePath.build(locale, "site/$$/index");
	}

	@RequestMapping(value={"/support"}, method = RequestMethod.GET)
	public String showSupportPage(ModelMap model, Locale locale)
	{
		int online = (int)(gameServerRepository.getOnline() * onlineRate);
		model.addAttribute("online", online);
		model.addAttribute("percent", Math.min(online*100/onlineSub,100));
		model.addAttribute("prizepool", blockService.getVar("pool_amount"));

		model.addAttribute("page", LocalizePath.build(locale, "site/$$/support.vm"));
		return LocalizePath.build(locale, "site/$$/index");
	}

	@RequestMapping(value={"/stats"}, method = RequestMethod.GET)
	public String showStatsPage(ModelMap model, Locale locale)
	{
		int online = (int)(gameServerRepository.getOnline() * onlineRate);
		model.addAttribute("online", online);
		model.addAttribute("percent", Math.min(online*100/onlineSub,100));
		model.addAttribute("prizepool", blockService.getVar("pool_amount"));
		model.addAttribute("topPvPList", gameServerRepository.getTopPvP());
		model.addAttribute("topPKList", gameServerRepository.getTopPK());
		model.addAttribute("topLevelList", gameServerRepository.getTopLevel());
		model.addAttribute("topAdenaList", gameServerRepository.getTopAdena());
		model.addAttribute("heroesList1", gameServerRepository.getHeroes(true));
		model.addAttribute("heroesList2", gameServerRepository.getHeroes(false));
		model.addAttribute("topClanList", gameServerRepository.getTopClan());
		model.addAttribute("castleList", gameServerRepository.getCastles());
		model.addAttribute("banList1", gameServerRepository.getBans(true));
		model.addAttribute("banList2", gameServerRepository.getBans(false));
		model.addAttribute("hostname", hostname);

		model.addAttribute("page", LocalizePath.build(locale, "site/$$/stats.vm"));
		return LocalizePath.build(locale, "site/$$/index");
	}

	@RequestMapping(value={"/en"}, method = RequestMethod.GET)
	public String showIndexEn(ModelMap model)
	{
		return showIndex(model, Locale.ENGLISH);
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

	@RequestMapping(value = "/error")
	public String error(Locale locale) {
		return LocalizePath.build(locale, "cp/$$/error");
	}

	@RequestMapping(value = "/image/{id}")
	@ResponseBody
	@Cacheable("images")
	public byte[] image(@PathVariable long id)
	{
		News news = newsService.findOne(id);
		return news == null ? new byte[]{} : news.getImage();
	}
/*
	@RequestMapping(value = "/imageShop/{id}")
	@ResponseBody
	@Cacheable("images")
	public byte[] imageShop(@PathVariable long id)
	{
		return shopPacketService.findOne(id).getImage();
	}

	@RequestMapping(value = "/imageShopItem/{id}")
	@ResponseBody
	@Cacheable("images")
	public byte[] imageShopItem(@PathVariable long id)
	{
		return shopPacketService.findOneItem(id).getImage();
	}
	*/
}