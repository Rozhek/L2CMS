package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.UserItemLog;

/**
 Created by iRock
 04.11.2015
 */
@Repository
public interface UserItemsLogRepository extends JpaRepository<UserItemLog, Long>
{
	@Query("select sum(logs.itemCount) from UserItemLog logs where logs.itemId = ?1")
	int getAllAmount(int itemId);
}