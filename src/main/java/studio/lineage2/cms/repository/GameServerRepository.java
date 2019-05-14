package studio.lineage2.cms.repository;

import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import studio.lineage2.cms.service.AccountService;
import studio.lineage2.cms.service.UserItemsLogService;
import studio.lineage2.cms.utils.XMLRpcServer;
import studio.lineage2.cms.utils.XMLRpcUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 Created by iRock
 23.11.2015
 */
@Component
public class GameServerRepository
{
	@Autowired
	private AccountService accountService;

	@Autowired
	private UserItemsLogService userItemsLogService;

	@Value("${topPvPCount}")
	private int topPvPCount;

	@Value("${topPKCount}")
	private int topPKCount;

	@Value("${topLevelCount}")
	private int topLevelCount;

	@Value("${topAdenaCount}")
	private int topAdenaCount;

	@Value("${topClanCount}")
	private int topClanCount;

	@Value("${banListCount}")
	private int banListCount;

	private Integer online = 0, accounts=0, players=0, golds = 0, credits = 0;;
	private List<TopPlayer> topPvP = new CopyOnWriteArrayList<>();
	private List<TopPlayer> topPK = new CopyOnWriteArrayList<>();
	private List<TopPlayer> topLevel = new CopyOnWriteArrayList<>();
	private List<TopPlayer> topAdena = new CopyOnWriteArrayList<>();
	private List<HeroPlayer> heroes = new CopyOnWriteArrayList<>();
	private List<TopClan> clans = new CopyOnWriteArrayList<>();
	private List<CastleInfo> castles = new CopyOnWriteArrayList<>();
	private List<BannedPlayer> banlist = new CopyOnWriteArrayList<>();
	private List<Alert> alertList = new CopyOnWriteArrayList<>();
	private Type topType = new TypeToken<CopyOnWriteArrayList<TopPlayer>>(){}.getType(),
			heroType = new TypeToken<CopyOnWriteArrayList<HeroPlayer>>(){}.getType(),
			clanType = new TypeToken<CopyOnWriteArrayList<TopClan>>(){}.getType(),
			castleType = new TypeToken<CopyOnWriteArrayList<CastleInfo>>(){}.getType(),
			bannedType = new TypeToken<CopyOnWriteArrayList<BannedPlayer>>(){}.getType(),
			alertType = new TypeToken<CopyOnWriteArrayList<Alert>>(){}.getType();


	public int getOnline()
	{
		return online;
	}

	public int getAccountAmount()
	{
		return accounts;
	}

	public int getPlayersAmount()
	{
		return players;
	}

	public int getGoldsAmount()
	{
		return golds;
	}

	public int getCreditAmount()
	{
		return credits;
	}

	public List<Alert> getAlerts()
	{
		return alertList;
	}
	/*public Set<String> getPlayers(long userId)
	{
		Set<String> players = new LinkedHashSet<>();
		for(Account account : accountService.findAllByUserId(userId))
		{
			List<String> list = XMLRpcUtil.send(XMLRpcServer.GAME, List.class, "PlayerService.xmlrpcGetPlayerNamesByAccount", account.getAccount());
			if(list != null)
			{
				players.addAll(list.stream().collect(Collectors.toList()));
			}
		}
		return players;
	}

	public Set<String> getPlayers(long userId, String accountName)
	{
		Set<String> players = new LinkedHashSet<>();
		Account account;
		if((account = accountService.findAccountByName(userId,accountName))!=null)
		{
			List<String> list = XMLRpcUtil.send(XMLRpcServer.GAME, List.class, "PlayerService.xmlrpcGetPlayerNamesByAccount", account.getAccount());
			if(list != null)
			{
				players.addAll(list.stream().collect(Collectors.toList()));
			}
		}
		return players;
	}*/

	public List<TopPlayer> getTopPvP()
	{
		return topPvP;
	}

	public List<TopPlayer> getTopPK()
	{
		return topPK;
	}

	public List<TopPlayer> getTopLevel()
	{
		return topLevel;
	}

	public List<TopPlayer> getTopAdena()
	{
		return topAdena;
	}

	public List<HeroPlayer> getHeroes()
	{
		return heroes;
	}

	public List<TopClan> getTopClan()
	{
		return clans;
	}

	public List<TopPlayer> getTopPvP(int limit)	{ return topPvP.subList(0,Math.min(limit, topPvP.size())); }

	public List<TopPlayer> getTopPK(int limit)
	{
		return topPK.subList(0,Math.min(limit, topPK.size()));
	}

	public List<TopPlayer> getTopLevel(int limit)
	{
		return topLevel.subList(0,Math.min(limit, topLevel.size()));
	}

	public List<TopPlayer> getTopAdena(int limit)
	{
		return topAdena.subList(0,Math.min(limit, topAdena.size()));
	}

	public List<HeroPlayer> getHeroes(boolean first)
	{
		int half = (int)Math.ceil(heroes.size()/2d);//2
		return heroes.subList(first? 0 : half, first? half : heroes.size());
	}

	public List<CastleInfo> getCastles()
	{
		return castles;
	}

	public List<BannedPlayer> getBans(boolean first)
	{
		int half = (int)Math.ceil(banlist.size()/2d);//2
		return banlist.subList(first? 0 : half, first? half : banlist.size());
	}

	@Scheduled(initialDelay = 1000, fixedRate = 5 * 60 * 1000L)
	public void load()
	{
		Integer online = XMLRpcUtil.send(XMLRpcServer.GAME, Integer.class, "WorldService.xmlrpcGetOnline");
		if(online != null)
		{
			this.online = online;
		}
		else this.online = 0;

		List<TopPlayer> topPvP = XMLRpcUtil.send(XMLRpcServer.GAME, topType, "WorldService.xmlrpcTopPvP", topPvPCount);
		if(topPvP != null)
		{
			this.topPvP = topPvP;
		}
		List<TopPlayer> topPk = XMLRpcUtil.send(XMLRpcServer.GAME, topType, "WorldService.xmlrpcTopPK", topPKCount);
		if(topPk != null)
		{
			this.topPK = topPk;
		}
		List<TopPlayer> topLevel = XMLRpcUtil.send(XMLRpcServer.GAME, topType, "WorldService.xmlrpcTopLevel", topLevelCount);
		if(topLevel != null)
		{
			this.topLevel = topLevel;
		}
		List<TopPlayer> topAdena = XMLRpcUtil.send(XMLRpcServer.GAME, topType, "WorldService.xmlrpcTopAdena", topAdenaCount);
		if(topAdena != null)
		{
			this.topAdena = topAdena;
		}
		List<HeroPlayer> heroes = XMLRpcUtil.send(XMLRpcServer.GAME, heroType, "WorldService.xmlrpcHeroes");
		if(heroes != null)
		{
			this.heroes = heroes;
		}
		List<TopClan> clans = XMLRpcUtil.send(XMLRpcServer.GAME, clanType, "WorldService.getTopClan", topClanCount);
		if(clans != null)
		{
			this.clans = clans;
		}
		List<CastleInfo> castles = XMLRpcUtil.send(XMLRpcServer.GAME, castleType, "WorldService.getCastle");
		if(castles != null)
		{
			this.castles = castles;
		}
		List<BannedPlayer> banlist = XMLRpcUtil.send(XMLRpcServer.GAME, bannedType, "WorldService.getBanList", banListCount);
		if(banlist != null)
		{
			this.banlist = banlist;
		}

	}

	@Scheduled(initialDelay = 10000, fixedRate = 30 * 60 * 1000L)
	public void metaInfo()
	{
		Integer accounts = XMLRpcUtil.send(XMLRpcServer.LOGIN, Integer.class, "WorldService.getAccountAmount");
		if(accounts != null)
		{
			this.accounts = accounts;
		}
		Integer players = XMLRpcUtil.send(XMLRpcServer.GAME, Integer.class, "WorldService.xmlrpcGetPlayers");
		if(players != null)
		{
			this.players = players;
		}

		Integer golds = XMLRpcUtil.send(XMLRpcServer.GAME, Integer.class, "WorldService.xmlrpcGetGoldAmount");
		if(golds != null)
		{
			this.golds = golds;
		}

		credits = userItemsLogService.getCreditAmount();

		List<Alert> alertList = XMLRpcUtil.send(XMLRpcServer.LOGIN, alertType, "WorldService.getTopAlerts", 10);
		if(alertList != null)
		{
			this.alertList = alertList;
		}
	}

	public class TopPlayer{

		private String name, clazz, pvp, pk, level, adena;

		public TopPlayer(String name, String clazz, String pvp, String pk, String level, String adena){
			this.name=name;
			this.clazz=clazz;
			this.pvp=pvp;
			this.pk=pk;
			this.level=level;
			this.adena=adena;
		}

		public String getName()
		{
			return name;
		}

		public String getClazz()
		{
			return clazz;
		}

		public String getPvP()
		{
			return pvp;
		}

		public String getPk()
		{
			return pk;
		}

		public String getLevel()
		{
			return level;
		}

		public String getAdena()
		{
			return adena;
		}
	}

	public class HeroPlayer{

		private String name, clazz, matches, wins;

		public HeroPlayer(String name, String clazz, String matches, String wins){
			this.name=name;
			this.clazz=clazz;
			this.matches=matches;
			this.wins=wins;
		}

		public String getName()
		{
			return name;
		}

		public String getClazz()
		{
			return clazz;
		}

		public String getMatches()
		{
			return matches;
		}

		public String getWins()
		{
			return wins;
		}
	}

	public class TopClan
	{
		private String name, count, rep, liderName, lvl, allyCrest, clanCrest;

		public TopClan(String name, String count, String rep, String liderName, String lvl, String allyCrest, String clanCrest)
		{
			this.name = name;
			this.count = count;
			this.rep = rep;
			this.liderName = liderName;
			this.lvl = lvl;
			this.allyCrest = allyCrest;
			this.clanCrest = clanCrest;
		}

		public String getName()
		{
			return name;
		}

		public String getCount()
		{
			return count;
		}

		public String getRep()
		{
			return rep;
		}

		public String getLeader()
		{
			return liderName;
		}

		public String getLvL()
		{
			return lvl;
		}

		public String getAllyCrest()
		{
			return allyCrest;
		}

		public String getClanCrest()
		{
			return clanCrest;
		}
	}

	public class CastleInfo
	{
		private String name, owner, siege_date, tw_date, allyCrest, clanCrest;
		private List<String> wards = new ArrayList<String>();

		public CastleInfo(String name, String owner, String siege_date, String tw_date, int[] wards, String allyCrest, String clanCrest)
		{
			this.name = name;
			this.owner = owner;
			this.siege_date = siege_date;
			this.tw_date = tw_date;
			this.allyCrest = allyCrest;
			this.clanCrest = clanCrest;
			for(int ward : wards)
				this.wards.add(String.valueOf(ward-80));
		}

		public String getName()
		{
			return name;
		}

		public String getOwner()
		{
			return owner;
		}

		public String getSiegeDate()
		{
			return siege_date;
		}

		public String getTWDate()
		{
			return tw_date;
		}

		public String getAllyCrest()
		{
			return allyCrest;
		}

		public String getClanCrest()
		{
			return clanCrest;
		}

		public List<String> getWards()
		{
			return wards;
		}
	}

	public class BannedPlayer
	{
		private String name, ban_date, ban_end, reason;

		public BannedPlayer(String name, String ban_date, String ban_end, String reason)
		{
			this.name = name;
			this.ban_date = ban_date;
			this.ban_end = ban_end;
			this.reason = reason;
		}

		public String getName()
		{
			return name;
		}

		public String getBanDate()
		{
			return ban_date;
		}

		public String getBanEnd()
		{
			return ban_end;
		}

		public String getReason()
		{
			return reason;
		}
	}

	public class Alert
	{
		private String login, points, ban_expire;

		public Alert(String login, String points, String ban_expire)
		{
			this.login = login;
			this.points = points;
			this.ban_expire = ban_expire;
		}

		public String getLogin()
		{
			return login;
		}

		public String getPoints()
		{
			return points;
		}

		public String getBan_expire()
		{
			return ban_expire;
		}

		public void setPoints(String points)
		{
			this.points = points;
		}

	}
}