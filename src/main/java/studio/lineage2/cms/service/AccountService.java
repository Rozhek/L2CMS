package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 27.10.2015
 */
@Service
public class AccountService
{
	@Autowired
	@Qualifier("accountRepository")
	private AccountRepository accountRepository;

	public List<Account> findAllByUserId(long userId)
	{
		return accountRepository.findAll().stream().filter(account -> account.getUserId() == userId).collect(Collectors.toList());
	}

	public Account findAccountByName(String username)
	{
		return accountRepository.findAll().stream().filter(account -> account.getAccount().equalsIgnoreCase(username)).findFirst().orElse(null);//.collect(Collectors.toList()).get(0);
	}

	public void save(Account account)
	{
		accountRepository.save(account);
	}

	public void delete(Account account)
	{
		accountRepository.delete(account);
	}

	public boolean findAccount(long userId, String username)
	{
		for(Account account : findAllByUserId(userId))
		{
			if(account.getAccount().equals(username))
				return true;
		}
		return false;
	}

	public List<Account> findAll()
	{
		return accountRepository.findAll();
	}

	public Account findOne(long account_id)
	{
		return accountRepository.findOne(account_id);
	}
}