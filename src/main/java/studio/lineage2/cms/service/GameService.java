package studio.lineage2.cms.service;

import com.google.gson.internal.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.model.UserItemLog;
import studio.lineage2.cms.model.UserItemType;
import studio.lineage2.cms.utils.XMLRpcServer;
import studio.lineage2.cms.utils.XMLRpcUtil;
import studio.lineage2.cms.xmlrpc.XMLRPCMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 Created by iRock
 23.11.2015
 */
@Service
public class GameService
{
	@Autowired
	private UserItemsService userItemsService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	private LocalizedMessageService localizedMessage;

	public XMLRPCMessage xmlrpcRegister(User user, String prefix, String username, String password, Locale locale)
	{

		if(!username.matches("[A-Za-z0-9]{4,11}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.login_invalid", locale));
		}

		if(!password.matches("[A-Za-z0-9]{8,16}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.password_invalid", locale));
		}

		if(!prefix.matches("[A-Z]{3}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.prefix_invalid", locale));
		}

		if(accountService.findAllByUserId(user.getId()).size()>6)
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.limit_exceeded", locale));
		}

		username = prefix + username;

		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.xmlrpcRegister", username, password);
		if(message == null){
			return null;
		}

		switch(message.getType())
		{
			case OK:
			{
				accountService.save(new Account(user.getId(), username));
				break;
			}
			default:
			{
				break;
			}
		}
		return message;
	}

	public XMLRPCMessage xmlrpcChangePassword(User user, String username, String oldpass, String password, Locale locale)
	{
		if(!accountService.findAccount(user.getId(), username))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.change_danger", locale));
		}

		if(!password.matches("[A-Za-z0-9]{8,16}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.change_password_invalid", locale));
		}

		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.xmlrpcChangePassword", username, oldpass, password);
		if(message == null)
			return null;
		if(message.getType()!= XMLRPCMessage.MessageType.OK)
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.change_wrong_password", locale));
		return message;

	}

	public XMLRPCMessage xmlrpcRecoverPassword(User user, String username, String password, Locale locale)
	{
		if(!accountService.findAccount(user.getId(), username))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.change_danger", locale));
		}

		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.xmlrpcSetPassword", username, password);
		if(message == null)
			return null;
		if(message.getType()!= XMLRPCMessage.MessageType.OK)
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("simplemsg.error", locale));
		return message;
	}

	public XMLRPCMessage xmlrpcUpdatePlayers(User user)
	{
		if(!XMLRpcServer.GAME.isConnected())
			new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL);

		boolean success = false;
		for(Account account : accountService.findAllByUserId(user.getId()))
		{
			if(account == null)
				continue;
			List<StringMap<String>> list = XMLRpcUtil.send(XMLRpcServer.GAME, List.class, "PlayerService.getAllCharsFromAccount", account.getAccount());
			if(list != null)
			{
				playerService.updateAll(account.getId(), list);
				success = true;
			}
			else if(XMLRpcServer.GAME.isConnected()){
				playerService.deleteAll(account.getId());
				accountService.delete(account);
				success = true;
			}
		}
		if(success)
			return new XMLRPCMessage(XMLRPCMessage.MessageType.OK);
		else
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL);
	}

	public XMLRPCMessage xmlrpcTeleportPlayer(String account, String player_id, Locale locale)
	{
		if(!player_id.matches("[0-9]{9,9}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("simplemsg.error", locale));
		}
		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.GAME, XMLRPCMessage.class, "PlayerService.unstuckPlayer", account, Integer.parseInt(player_id));
		if(message == null)
			return null;
		if(message.getType()!= XMLRPCMessage.MessageType.OK)
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.teleport_failed", locale));
		return message;

	}

	public XMLRPCMessage xmlrpcUnlockPlayer(String account, String player_id, Locale locale)
	{
		if(!player_id.matches("[0-9]{9,9}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("simplemsg.error", locale));
		}
		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.GAME, XMLRPCMessage.class, "PlayerService.resetLock", account, Integer.parseInt(player_id));
		if(message == null)
			return null;
		if(message.getType()!= XMLRPCMessage.MessageType.OK)
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("account.unlock_failed", locale));
		return message;

	}

	public XMLRPCMessage xmlrpcLoginAccount(String account, String password)
	{
		return XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.login", account, password);
	}

	/*@RequestMapping(value = "/xmlrpcResetLock/{account}", method = { RequestMethod.GET })
	public String xmlrpcResetLock(ModelMap model, @PathVariable String account, Locale locale)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!accountService.findAccount(user.getId(), account))
		{
			return show(model, locale);
		}

		XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.xmlrpcResetLock", account);

		return show(model, locale);
	}

	@RequestMapping(value = "/xmlrpcResetLock/{account}", method = { RequestMethod.GET })
	public String xmlrpcRecover(ModelMap model, @PathVariable String account, Locale locale)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!accountService.findAccount(user.getId(), account))
		{
			return show(model, locale);
		}

		XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.xmlrpcResetLock", account);

		return show(model, locale);
	}*/

	public XMLRPCMessage xmlrpcAddCoins(User user, String itemCount, String player_id, Locale locale, HttpServletRequest request)
	{
		if(!itemCount.matches("[0-9]{1,4}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("item.count_invalid", locale));
		}

		if(!player_id.matches("[0-9]{9,9}"))
		{
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("simplemsg.error", locale));
		}

		int count = Integer.parseInt(itemCount);

		if(count <= 0 || count > 7500){
			return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("item.count_invalid", locale));
		}

		String browser = userIpService.findCurrentBrowser(request);
		UserItemLog itemLog = userItemsService.removeUserItem(user.getId(), UserItemType.MONEY.getId(), count, "logging.item_progress", null);
		if(itemLog!=null)
		{
			XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.GAME, XMLRPCMessage.class, "PlayerService.xmlrpcAddItem", Integer.parseInt(player_id), UserItemType.GOLD.getId(), count);
			if(message == null || message.getType()!= XMLRPCMessage.MessageType.OK)
			{
				userItemsService.addUserItem(user.getId(), UserItemType.MONEY.getName(), UserItemType.MONEY.getId(), count, "logging.item_return", null);
				userIpService.save(new UserIp(user.getId(), request.getRemoteAddr(), Calendar.getInstance().getTimeInMillis(), itemLog.getId(), false, browser));
			}
			else
			{
				userIpService.save(new UserIp(user.getId(), request.getRemoteAddr(), Calendar.getInstance().getTimeInMillis(), itemLog.getId(), true, browser));
			}

			return message;
		}

		return new XMLRPCMessage(XMLRPCMessage.MessageType.FAIL, localizedMessage.getMessage("item.credits_not_enough", locale));
	}

	public boolean xmlrpcAddItem(int itemId, int itemCount, int player_id)
	{
		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.GAME, XMLRPCMessage.class, "PlayerService.xmlrpcAddItem", player_id, itemId, itemCount);
		return message != null && message.getType() == XMLRPCMessage.MessageType.OK;
	}

	public boolean xmlrpcSetAlerts(String account, int points)
	{
		XMLRPCMessage message = XMLRpcUtil.send(XMLRpcServer.LOGIN, XMLRPCMessage.class, "PlayerService.xmlrpcSetAlerts", account, points);
		return message != null && message.getType() == XMLRPCMessage.MessageType.OK;
	}
}