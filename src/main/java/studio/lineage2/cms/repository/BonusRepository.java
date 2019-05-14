package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.lineage2.cms.model.Bonus;

/**
 * Created by iRock on 07.04.2017.
 */
public interface BonusRepository extends JpaRepository<Bonus, Long>
{
	Bonus findByCode(String code);
}
