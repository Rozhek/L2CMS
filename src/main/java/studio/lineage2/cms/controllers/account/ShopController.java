package studio.lineage2.cms.controllers.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.model.ShopPacket;
import studio.lineage2.cms.model.ShopPacketItem;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserItem;
import studio.lineage2.cms.model.UserItemType;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.ShopPacketService;
import studio.lineage2.cms.service.UserItemsService;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 Created by iRock
 11.11.2015
 */
//@Controller
//@RequestMapping("/account/shop")
public class ShopController
{
	/*@Autowired
	private ShopPacketService shopPacketService;

	@Autowired
	private UserItemsService userItemsService;

	@Autowired
	private LocalizedMessageService localizedMessage;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserItem userItem = userItemsService.findOne(user.getId(), UserItemType.MONEY.getId());
		model.addAttribute("MONEY", userItem == null ? 0 : userItem.getItemCount());

		model.addAttribute("shopPacketList", shopPacketService.findAll());
		model.addAttribute("shopPacketItemList", shopPacketService.findAllItem());

		model.addAttribute("shopmenu", true);

		model.addAttribute("page", "cp/account/shop.vm");
		return "cp/index";
	}

	@RequestMapping(value = "/buy/{packetId}", method = { RequestMethod.GET })
	public String buy(@PathVariable int packetId, Locale locale, HttpServletRequest request)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ShopPacket shopPacket = shopPacketService.findOne(packetId);
		if(shopPacket == null)
		{
			return "redirect:/shop";
		}
		if(userItemsService.removeUserItem(user.getId(), UserItemType.MONEY.getId(), shopPacket.getPacketPrice(), "logging.shop_progress", shopPacket.getPacketName()))
		{
			for(ShopPacketItem shopPacketItem : shopPacketService.findAllByPacketId(shopPacket.getId()))
			{
				userItemsService.addUserItem(user.getId(), shopPacketItem.getItemName(), shopPacketItem.getItemId(), shopPacketItem.getItemCount(), "logging.shop_buy", shopPacket.getPacketName());
			}
		}
		return "redirect:/shop";
	}*/
}