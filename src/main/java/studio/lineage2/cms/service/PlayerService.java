package studio.lineage2.cms.service;

import com.google.gson.internal.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.model.Player;
import studio.lineage2.cms.repository.PlayerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 Created by iRock
 27.10.2015
 */
@Service
public class PlayerService
{
	@Autowired
	@Qualifier("playerRepository")
	private PlayerRepository playerRepository;

	@Autowired
	private AccountService accountService;

	public List<Player> findAllByAccountId(long accountId)
	{
		return playerRepository.findByAccountId(accountId).stream().collect(Collectors.toList());
	}

	public long findPlayerIdByName(String playername)
	{
		Player player = playerRepository.findByNameIgnoreCase(playername).stream().findFirst().orElse(null);
		return player != null? player.getId() : 0;
	}

	public void save(Player player)
	{
		playerRepository.save(player);
	}

	public boolean findPlayer(long userId, String playername)
	{
		for(Account account : accountService.findAllByUserId(userId))
		{
			for(Player player : findAllByAccountId(account.getId()))
				if(player.getName().equals(playername))
					return true;
		}
		return false;
	}

	public void updateAll(long accountId, List<StringMap<String>> newplayers)
	{
		Set<Player> players = playerRepository.findByAccountId(accountId).stream().collect(Collectors.toSet());
		Map<Long, Boolean> deletePlayer = new HashMap<>(players.size());
		forupdate : for(StringMap<String> value : newplayers)
		{
			long id = Long.parseLong(value.get("id"));
			for(Player player : players)
				if(player.getId() == id){
					player.setAccountId(accountId);
					player.setName(value.get("name"));
					player.setCharClass(value.get("charclass"));
					player.setLevel(Integer.parseInt(value.get("level")));
					player.setPvp(Integer.parseInt(value.get("pvp")));
					player.setPk(Integer.parseInt(value.get("pk")));
					player.setKarma(Integer.parseInt(value.get("karma")));
					player.setLastAccess(Long.parseLong(value.get("lastaccess")));
					player.setHwid(Boolean.parseBoolean(value.get("hwid")));
					save(player);
					deletePlayer.put(id, false);
					continue forupdate;
				}
				else if(!deletePlayer.containsKey(player.getId()))
				{
					deletePlayer.put(player.getId(), true);
				}
			//Сохранение нового
			save(new Player(id, accountId, value.get("name"), value.get("charclass") , Integer.parseInt(value.get("level")), Integer.parseInt(value.get("pvp")), Integer.parseInt(value.get("pk")), Integer.parseInt(value.get("karma")), Long.parseLong(value.get("lastaccess")), Boolean.parseBoolean(value.get("hwid"))));
		}
		//Удаление несуществующих чаров на аккаунте
		deletePlayer.keySet().stream().filter(deletePlayer::get).forEach(playerId -> playerRepository.delete(playerId));
	}

	public void deleteAll(long accountId)
	{
		Set<Player> players = playerRepository.findByAccountId(accountId).stream().collect(Collectors.toSet());
		playerRepository.deleteInBatch(players);
	}

	public List<Player> findAll()
	{
		return playerRepository.findAll();
	}

	public Player findOne(long id)
	{
		return playerRepository.findOne(id);
	}
}