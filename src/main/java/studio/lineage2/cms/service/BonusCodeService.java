package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Bonus;
import studio.lineage2.cms.repository.BonusRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 27.10.2015
 */
@Service
public class BonusCodeService
{
	@Autowired
	@Qualifier("bonusRepository")
	private BonusRepository bonusRepository;

	public List<Bonus> findAllByUserId(long userId)
	{
		return bonusRepository.findAll().stream().filter(bonus -> bonus.getUserId() == userId).collect(Collectors.toList());
	}

	public void save(Bonus bonus)
	{
		bonusRepository.save(bonus);
	}

	public void activateBonusCode(String bonusCode, long userId) {
		getBonusCode(bonusCode).activate(userId);
	}

	public Bonus getBonusCode(String bonusCode) {
		return bonusRepository.findByCode(bonusCode);
	}

	public boolean createBonusCode(String code, long value) {
		Bonus bonuscode = new Bonus(code, value);
		try {
			bonusRepository.save(bonuscode);
		}
		catch(Exception e) {
			// how to find out the reason for the rollback exception?
			return false;
		}
		return true;
	}
}