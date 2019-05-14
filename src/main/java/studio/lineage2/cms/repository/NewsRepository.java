package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.lineage2.cms.model.News;

import java.util.List;

/**
 Created by iRock
 28.10.2015
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long>
{}