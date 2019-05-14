package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.ShopPacketItem;

/**
 Created by iRock
 06.11.2015
 */
@Repository
public interface ShopPacketItemRepository extends JpaRepository<ShopPacketItem, Long>
{}