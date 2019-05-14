package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Support;
import studio.lineage2.cms.model.SupportType;
import studio.lineage2.cms.repository.SupportRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 02.11.2015
 */
@Service
public class SupportService
{
	@Autowired
	@Qualifier("supportRepository")
	private SupportRepository supportRepository;

	public void save(Support support)
	{
		supportRepository.save(support);
	}

	public List<Support> findAllSupport(SupportType type)
	{
		List<Support> allSupports = supportRepository.findAll();
		Collections.reverse(allSupports);
		return allSupports.stream().filter(support -> type == SupportType.ALL && support.getType() != SupportType.CLOSE || support.getType() == type).collect(Collectors.toList());

	}

	public List<Support> findAllSupportByUserId(long userId, SupportType type)
	{
		List<Support> allSupports = supportRepository.findAll();
		Collections.reverse(allSupports);
		return allSupports.stream().filter(support -> support.getAuthorId() == userId && (type == SupportType.ALL || support.getType() == type)).collect(Collectors.toList());
	}

	public Support findSupportByIdAndUserId(long id, long userId)
	{
		for(Support support : supportRepository.findAll())
		{
			if(support.getId() == id && support.getAuthorId() == userId)
			{
				return support;
			}
		}
		return null;
	}

	public Support findSupportById(long id)
	{
		return supportRepository.findOne(id);
	}
}