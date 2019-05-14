package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.UserItem;
import studio.lineage2.cms.model.UserItemLog;
import studio.lineage2.cms.repository.UserItemsRepository;
import studio.lineage2.cms.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 04.11.2015
 */
@Service
public class UserItemsService
{
	@Autowired
	@Qualifier("userItemsRepository")
	private UserItemsRepository userItemsRepository;

	@Autowired
	@Qualifier("userRepository")
	private UserRepository userRepository;

	@Autowired
	private UserItemsLogService userItemsLogService;

	public void save(UserItem userItem)
	{
		userItemsRepository.save(userItem);
	}

	public void delete(UserItem userItem)
	{
		userItemsRepository.delete(userItem);
	}

	public List<UserItem> findOne(long userId)
	{
		return userItemsRepository.findByUserId(userId).stream().collect(Collectors.toList());
	}

	public UserItem findOne(long userId, int itemId)
	{
		for(UserItem userItem : findOne(userId))
		{
			if(userItem.getItemId() == itemId)
			{
				return userItem;
			}
		}
		return null;
	}

	public UserItemLog addUserItem(long userId, String itemName, int itemId, long itemCount, String text, String text_param)
	{
		if(itemCount < 1)
		{
			return null;
		}
		synchronized(userRepository.findOne(userId))
		{
			UserItem userItem = findOne(userId, itemId);
			if(userItem == null)
			{
				userItem = new UserItem();
				userItem.setUserId(userId);
				userItem.setItemName(itemName);
				userItem.setItemId(itemId);
				userItem.setItemCount(0);
			}
			userItem.setItemCount(userItem.getItemCount() + itemCount);
			save(userItem);
			UserItemLog itemLog = new UserItemLog(userItem.getUserId(), userItem.getItemName(), userItem.getItemId(), itemCount, text, text_param);
			userItemsLogService.save(itemLog);
			return itemLog;
		}
	}

	public UserItemLog removeUserItem(long userId, int itemId, long itemCount, String text, String text_param)
	{
		if(itemCount < 1)
		{
			return null;
		}

		synchronized(userRepository.findOne(userId))
		{
			UserItem userItem = findOne(userId, itemId);
			if(userItem == null || userItem.getItemCount() < itemCount)
			{
				return null;
			}
			userItem.setItemCount(userItem.getItemCount() - itemCount);
			if(userItem.getItemCount() > 0)
			{
				save(userItem);
			}
			else
			{
				delete(userItem);
			}
			UserItemLog itemLog = new UserItemLog(userItem.getUserId(), userItem.getItemName(), userItem.getItemId(), -itemCount, text, text_param);
			userItemsLogService.save(itemLog);
			return itemLog;
		}
	}
}