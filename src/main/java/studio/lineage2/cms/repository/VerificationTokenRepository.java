package studio.lineage2.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.VerificationToken;

/**
 * Created by iRock on 07.04.2017.
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>
{
	VerificationToken findByToken(String token);

	VerificationToken findByUser(User user);
}
