package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.SupportMessage;
import studio.lineage2.cms.repository.SupportMessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 03.11.2015
 */
@Service
public class SupportMessageService
{
	@Autowired
	@Qualifier("supportMessageRepository")
	private SupportMessageRepository supportMessageRepository;

	public void save(SupportMessage supportMessage)
	{
		supportMessageRepository.save(supportMessage);
	}

	public List<SupportMessage> findSupportMessagesBySupportId(long supportId)
	{
		List<SupportMessage> supportMessages = supportMessageRepository.findAll().stream().filter(supportMessage -> supportMessage.getSupportId() == supportId).collect(Collectors.toList());
		return supportMessages;
	}
}