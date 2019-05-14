package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;

import java.util.List;

/**
 Created by iRock
 01.11.2015
 */
@Repository
public interface UserIpRepository extends JpaRepository<UserIp, Long>
{
	List<UserIp> findByUserId(long userId);
}