package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.User;

/**
 Created by iRock
 17.10.2015
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>
{}