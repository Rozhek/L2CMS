package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.VerificationToken;
import studio.lineage2.cms.repository.UserRepository;
import studio.lineage2.cms.repository.VerificationTokenRepository;

/**
 Created by iRock
 17.10.2015
 */
@Service
public class UserService
{
	@Autowired
	@Qualifier("userRepository")
	private UserRepository userRepository;

	@Autowired
	private VerificationTokenRepository tokenRepository;

	public void addUser(User user)
	{
		userRepository.save(user);
	}

	public void saveUser(User user)
	{
		userRepository.save(user);
	}

	public User findUserByName(String username) throws UsernameNotFoundException
	{
		for(User user : userRepository.findAll())
		{
			if(user.getUsername().equals(username))
			{
				return user;
			}
		}
		throw new UsernameNotFoundException(username);
	}

	public boolean checkUserByName(String username)
	{
		for(User user : userRepository.findAll())
		{
			if(user.getUsername().equals(username))
			{
				return true;
			}
		}
		return false;
	}

	public User findUserById(String userId)
	{
		User user;
		try{
			if((user = userRepository.findOne(Long.parseLong(userId))) !=null)
			{
				return user;
			}
		}
		catch(UsernameNotFoundException e){
			throw new UsernameNotFoundException(String.valueOf(userId));
		}
		catch(Exception e){
			return null;
		}
		return null;
	}

	public int userCount()
	{
		return userRepository.findAll().size();
	}

	public VerificationToken getVerificationToken(String VerificationToken) {
		return tokenRepository.findByToken(VerificationToken);
	}

	public void createVerificationToken(User user, String token) {
		VerificationToken myToken = new VerificationToken(token, user);
		tokenRepository.save(myToken);
	}
}