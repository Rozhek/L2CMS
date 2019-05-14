package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.Variable;

/**
 Created by iRock
 29.10.2015
 */
@Repository
public interface VariablesRepository extends JpaRepository<Variable, String>
{}