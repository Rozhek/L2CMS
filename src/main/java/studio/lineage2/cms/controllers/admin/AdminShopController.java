package studio.lineage2.cms.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import studio.lineage2.cms.model.ShopPacket;
import studio.lineage2.cms.model.ShopPacketItem;
import studio.lineage2.cms.service.ShopPacketService;
import studio.lineage2.cms.utils.LocalizePath;

import java.io.IOException;
import java.util.Locale;

/**
 Created by iRock
 06.11.2015
 */
@Controller
@RequestMapping("/admin/shop")
public class AdminShopController
{
	@Autowired
	private ShopPacketService shopPacketService;

	@RequestMapping(method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		model.addAttribute("shopPacket", new ShopPacket());
		model.addAttribute("shopPacketList", shopPacketService.findAll());
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/shop/index.vm"));
		model.addAttribute("menu2", LocalizePath.build(locale, "cp/$$/admin/menu.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "add", method = { RequestMethod.POST })
	public String add(ModelMap model, @ModelAttribute ShopPacket shopPacket, @RequestParam("file") MultipartFile file, Locale locale)
	{
		try
		{
			shopPacket.setImage(file.getBytes());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		shopPacketService.save(shopPacket);
		return show(model, locale);
	}

	@RequestMapping(value = "remove/{id}", method = { RequestMethod.GET })
	public String remove(ModelMap model, @PathVariable long id, Locale locale)
	{
		shopPacketService.delete(id);
		return show(model, locale);
	}

	@RequestMapping(value = "edit/{id}", method = { RequestMethod.GET })
	public String edit(ModelMap model, @PathVariable long id, Locale locale)
	{
		model.addAttribute("shopPacket", shopPacketService.findOne(id));
		model.addAttribute("shopPacketList", shopPacketService.findAll());

		model.addAttribute("shopPacketItem", new ShopPacketItem());
		model.addAttribute("shopPacketItemList", shopPacketService.findAllByPacketId(id));

		model.addAttribute("type", "addItem");

		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/shop/edit.vm"));
		model.addAttribute("menu2", LocalizePath.build(locale, "cp/$$/admin/menu.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "edit", method = { RequestMethod.POST })
	public String edit(ModelMap model, long id, @ModelAttribute ShopPacket shopPacket, @RequestParam("file") MultipartFile file, Locale locale)
	{
		ShopPacket shopPacket1 = shopPacketService.findOne(id);
		shopPacket1.setPacketName(shopPacket.getPacketName());
		shopPacket1.setPacketPrice(shopPacket.getPacketPrice());
		try
		{
			if(file.getSize() > 0)
			{
				shopPacket1.setImage(file.getBytes());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		shopPacketService.save(shopPacket1);
		return show(model, locale);
	}

	@RequestMapping(value = "addItem", method = { RequestMethod.POST })
	public String addItem(ModelMap model, long packetId, @ModelAttribute ShopPacketItem shopPacketItem, @RequestParam("file") MultipartFile file, Locale locale)
	{
		shopPacketItem.setPacketId(packetId);
		try
		{
			shopPacketItem.setImage(file.getBytes());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		shopPacketService.saveItem(shopPacketItem);
		return edit(model, packetId, locale);
	}

	@RequestMapping(value = "removeItem/{id}", method = { RequestMethod.GET })
	public String removeItem(ModelMap model, @PathVariable long id, Locale locale)
	{
		long packetId = shopPacketService.findOneItem(id).getPacketId();
		shopPacketService.deleteItem(id);
		return edit(model, packetId, locale);
	}

	@RequestMapping(value = "editItem/{id}", method = { RequestMethod.GET })
	public String editItem(ModelMap model, @PathVariable long id, Locale locale)
	{
		ShopPacketItem shopPacketItem = shopPacketService.findOneItem(id);

		model.addAttribute("shopPacket", shopPacketService.findOne(shopPacketItem.getPacketId()));
		model.addAttribute("shopPacketList", shopPacketService.findAll());

		model.addAttribute("shopPacketItem", shopPacketItem);
		model.addAttribute("shopPacketItemList", shopPacketService.findAllByPacketId(shopPacketItem.getPacketId()));

		model.addAttribute("type", "editItem");

		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/shop/edit.vm"));
		model.addAttribute("menu2", LocalizePath.build(locale, "cp/$$/admin/menu.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "editItem", method = { RequestMethod.POST })
	public String editItem(ModelMap model, long id, @ModelAttribute ShopPacketItem shopPacketItem, @RequestParam("file") MultipartFile file, Locale locale)
	{
		ShopPacketItem shopPacketItem1 = shopPacketService.findOneItem(id);
		shopPacketItem1.setItemName(shopPacketItem.getItemName());
		shopPacketItem1.setItemId(shopPacketItem.getItemId());
		shopPacketItem1.setItemCount(shopPacketItem.getItemCount());
		try
		{
			if(file.getSize() > 0)
			{
				shopPacketItem1.setImage(file.getBytes());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		shopPacketService.saveItem(shopPacketItem1);
		return edit(model, shopPacketItem1.getPacketId(), locale);
	}
}