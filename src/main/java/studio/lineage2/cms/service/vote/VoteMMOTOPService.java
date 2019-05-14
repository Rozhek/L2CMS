package studio.lineage2.cms.service.vote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import studio.lineage2.cms.model.VoteMMOTOP;
import studio.lineage2.cms.repository.VoteMMOTOPRepository;
import studio.lineage2.cms.service.GameService;
import studio.lineage2.cms.service.PlayerService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

/**
 Created by iRock
 04.11.2015
 */
//@Service
public class VoteMMOTOPService
{
	@Value("${mmoTopEnable}")
	private boolean mmoTopEnable;
	@Value("${mmoTopLink}")
	private String mmoTopLink;
	@Value("${mmoTopItem}")
	private int mmoTopItem;
	@Value("${mmoTopItemCount}")
	private int mmoTopItemCount;

	@Autowired
	@Qualifier("voteMMOTOPRepository")
	private VoteMMOTOPRepository voteMMOTOPRepository;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private GameService gameService;

	public void save(VoteMMOTOP voteMMOTOP)
	{
		voteMMOTOPRepository.save(voteMMOTOP);
	}

	public boolean contains(long voteId)
	{
		return voteMMOTOPRepository.findByVoteId(voteId) != null;
	}

	@Scheduled(initialDelay = 1000, fixedRate = 60000)
	public void load()
	{
		if(!mmoTopEnable)
		{
			return;
		}
		try
		{
			URL url = new URL(mmoTopLink);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			String line;
			while((line = reader.readLine()) != null)
			{
				if(line.startsWith("Нет данных") || line.startsWith("QRATOR HTTP") || line.contains("Попробуйте обновить страницу"))
				{
					break;
				}

				StringTokenizer st = new StringTokenizer(line, "\t");
				long voteId = Long.parseLong(st.nextToken());
				st.nextToken();
				String ip = st.nextToken();
				String name = st.nextToken();

				if(contains(voteId))
				{
					continue;
				}

				VoteMMOTOP voteMMOTOP = new VoteMMOTOP();
				voteMMOTOP.setVoteId(voteId);
				voteMMOTOP.setName(name);
				voteMMOTOP.setIp(ip);
				voteMMOTOP.setTake(false);

				save(voteMMOTOP);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		for(VoteMMOTOP voteMMOTOP : voteMMOTOPRepository.findAll())
		{
			if(voteMMOTOP.isTake())
			{
				continue;
			}
			int player_id = (int)playerService.findPlayerIdByName(voteMMOTOP.getName());
			if(player_id == 0)
			{
				continue;
			}
			if(!gameService.xmlrpcAddItem(mmoTopItem, mmoTopItemCount, player_id))
			{
				continue;
			}
			voteMMOTOP.setTake(true);
			save(voteMMOTOP);
		}
	}


}