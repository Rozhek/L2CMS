package studio.lineage2.cms.service.vote;

import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 Created by iRock
 04.11.2015
 */
//@Service
public class VoteTopzoneService
{
	@Value("${topzoneLink}")
	private String topzoneLink;

	@Value("${topzoneServer}")
	private int topzoneServer;

	public String getApiEndpoint(String ip)
	{
		return String.format("http://l2topzone.com/api.php?API_KEY=%s&SERVER_ID=%d&IP=%s",topzoneLink, topzoneServer, ip);
	}

	public boolean hasVoted(String ip)
	{
		try{
			String endpoint = getApiEndpoint(ip);
			if(endpoint.startsWith("err"))
				return false;
			String voted = getApiResponse(endpoint);
			return tryParseBool(voted);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public boolean tryParseBool(String bool)
	{
		if(bool.startsWith("1"))
			return true;
		return Boolean.parseBoolean(bool.trim());
	}

	public String getApiResponse(String endpoint)
	{
		/*if(!mmoTopEnable)
		{
			return;
		}*/
		StringBuilder stringBuilder = new StringBuilder();
		try
		{
			URL url = new URL(endpoint);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			String line;
			while((line = reader.readLine()) != null)
			{
				stringBuilder.append(line).append("\n");
				/*StringTokenizer st = new StringTokenizer(line, "\t");
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

				save(voteMMOTOP);*/
			}
			return stringBuilder.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "err";
		}
	}

}