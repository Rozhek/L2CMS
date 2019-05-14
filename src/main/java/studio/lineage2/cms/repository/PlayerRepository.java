package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.Player;

import java.util.List;

/**
 Created by iRock
 27.10.2015
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>
{
	List<Player> findByAccountId(long account_id);
	List<Player> findByNameIgnoreCase(String name);
}