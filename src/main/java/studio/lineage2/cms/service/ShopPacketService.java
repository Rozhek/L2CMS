package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.ShopPacket;
import studio.lineage2.cms.model.ShopPacketItem;
import studio.lineage2.cms.repository.ShopPacketItemRepository;
import studio.lineage2.cms.repository.ShopPacketRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 05.11.2015
 */
@Service
public class ShopPacketService
{
	@Autowired
	@Qualifier("shopPacketRepository")
	private ShopPacketRepository shopPacketRepository;

	@Autowired
	@Qualifier("shopPacketItemRepository")
	private ShopPacketItemRepository shopPacketItemRepository;

	public void save(ShopPacket shopPacket)
	{
		shopPacketRepository.save(shopPacket);
	}

	public List<ShopPacket> findAll()
	{
		return shopPacketRepository.findAll();
	}

	public List<ShopPacketItem> findAllItem()
	{
		return shopPacketItemRepository.findAll();
	}

	public void delete(long id)
	{
		shopPacketRepository.delete(id);
	}

	public ShopPacket findOne(long id)
	{
		return shopPacketRepository.findOne(id);
	}

	public void saveItem(ShopPacketItem shopPacketItem)
	{
		shopPacketItemRepository.save(shopPacketItem);
	}

	public List<ShopPacketItem> findAllByPacketId(long packetId)
	{
		List<ShopPacketItem> shopPacketItems = shopPacketItemRepository.findAll().stream().filter(shopPacketItem -> shopPacketItem.getPacketId() == packetId).collect(Collectors.toList());

		return shopPacketItems;
	}

	public void deleteItem(long id)
	{
		shopPacketItemRepository.delete(id);
	}

	public ShopPacketItem findOneItem(long id)
	{
		return shopPacketItemRepository.findOne(id);
	}
}