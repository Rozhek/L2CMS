package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.Support;

/**
 Created by iRock
 02.11.2015
 */
@Repository
public interface SupportRepository extends JpaRepository<Support, Long>
{}