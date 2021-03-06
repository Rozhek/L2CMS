package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.Account;

/**
 Created by iRock
 27.10.2015
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long>
{}