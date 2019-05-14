package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.Block;

/**
 Created by iRock
 29.10.2015
 */
@Repository
public interface BlockRepository extends JpaRepository<Block, Long>
{}