package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.UserItemLog;
import studio.lineage2.cms.model.UserItemType;
import studio.lineage2.cms.repository.UserItemsLogRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 04.11.2015
 */
@Service
public class UserItemsLogService
{
	@Autowired
	@Qualifier("userItemsLogRepository")
	private UserItemsLogRepository userItemsLogRepository;

	public void save(UserItemLog userItemLog)
	{
		userItemsLogRepository.save(userItemLog);
	}

	public List<UserItemLog> findAll(long userId)
	{
		List<UserItemLog> userItemLogs = userItemsLogRepository.findAll().stream().filter(userItemLog -> userItemLog.getUserId() == userId).collect(Collectors.toList());
		Collections.reverse(userItemLogs);
		return userItemLogs;
	}

	public int getCreditAmount()
	{
		return userItemsLogRepository.getAllAmount(UserItemType.MONEY.getId());
	}

	public UserItemLog findOne(long logId)
	{
		return userItemsLogRepository.findOne(logId);
	}
}