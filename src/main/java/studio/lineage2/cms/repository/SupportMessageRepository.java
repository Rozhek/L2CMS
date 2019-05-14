package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.SupportMessage;

/**
 Created by iRock
 03.11.2015
 */
@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long>
{}