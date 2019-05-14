package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.VoteMMOTOP;

/**
 Created by iRock
 04.11.2015
 */
@Repository
public interface VoteMMOTOPRepository extends JpaRepository<VoteMMOTOP, Long>
{
	VoteMMOTOP findByVoteId(long voteId);
}