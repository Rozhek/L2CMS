package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Block;
import studio.lineage2.cms.model.Variable;
import studio.lineage2.cms.repository.BlockRepository;
import studio.lineage2.cms.repository.VariablesRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 29.10.2015
 */
@Service
public class BlockService
{
	@Autowired
	@Qualifier("blockRepository")
	private BlockRepository blockRepository;

	@Autowired
	@Qualifier("variablesRepository")
	private VariablesRepository variables;

	public void save(Block block)
	{
		blockRepository.save(block);
	}

	public void delete(long id)
	{
		blockRepository.delete(id);
	}

	public List<Block> findAll()
	{
		return blockRepository.findAll();
	}

	public Block findOne(long id)
	{
		return blockRepository.findOne(id);
	}

	@Cacheable("blocks")
	public List<Block> findAll(int type, String lang)
	{
		return blockRepository.findAll().stream().filter(block -> block.getType()==type && (block.getLang().isEmpty() || block.getLang().equals(lang))).collect(Collectors.toList());
	}

	@Cacheable("blocks")
	public long getVar(String param)
	{
		Variable var = variables.findOne(param);
		try
		{
			return var == null ? 0L : var.getValue();
		}
		catch(NumberFormatException e){
			return 0L;
		}
	}

	public boolean setVar(String param, String value)
	{
		try
		{
			setVar(param, Long.parseLong(value));
			return true;
		}
		catch(NumberFormatException e){
			return false;
		}

	}

	public synchronized void setVar(String param, long value)
	{
		Variable var = variables.findOne(param);
		if(var == null) var = new Variable(param);
		variables.save(var.setValue(value));
	}

}