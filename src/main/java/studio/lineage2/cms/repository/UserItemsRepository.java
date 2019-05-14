package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.UserItem;

import java.util.List;

/**
 Created by iRock
 04.11.2015
 */
@Repository
public interface UserItemsRepository extends JpaRepository<UserItem, Long>
{
	List<UserItem> findByUserId(long userId);
}