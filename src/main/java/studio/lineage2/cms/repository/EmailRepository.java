package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.Email;

/**
 Created by iRock
 01.12.2015
 */
@Repository
public interface EmailRepository extends JpaRepository<Email, Long>
{}
