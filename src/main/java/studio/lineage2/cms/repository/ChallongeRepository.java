package studio.lineage2.cms.repository;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 Created by iRock
 23.11.2015
 */
@Component
public class ChallongeRepository
{
	@Value("${credintials}")
	private String credintials;

	@Value("${tournament}")
	private String tournament;

	@Value("${matchPerRound}")
	private int matchPerRound;

	private final SimpleDateFormat timezoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM", Locale.ENGLISH);
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("ha 'GMT'XXX", Locale.ENGLISH);
	private final String matchesURL = "https://api.challonge.com/v1/tournaments/%%/matches.json";
	private final String teamsURL = "https://api.challonge.com/v1/tournaments/%%/participants.json";

	private Match nextMatch;

	private Map<Integer, Team> group_teams = new HashMap<>();
	private Map<Character, List<Team>> group_stage = new HashMap<>();

	private Map<Integer, Team> final_teams = new HashMap<>();
	private Map<Integer, Match> final_stage = new HashMap<>();

	private Map<Integer, List<Match>> schedule = new HashMap<>();
	private Map<Integer, String> schedule_state = new HashMap<>();

	@Scheduled(initialDelay = 1000, fixedRate = 5 * 60 * 1000L)
	public void load()
	{
		if(tournament.isEmpty())
			return;

		List<StringMap<StringMap>> pre_teams = send(teamsURL.replace("%%", tournament));
		if(pre_teams!=null)
		{
			Map<Integer, Team> final_teams = new HashMap<>(), group_teams = new HashMap<>();
			StringMap value;
			for(StringMap<StringMap> pre_team : pre_teams)
				if((value = pre_team.get("participant"))!=null){
					Team team = new Team(value);
					final_teams.put(team.getId(),team);
					group_teams.put(team.getGroupId(),team);
				}
			this.final_teams = final_teams;
			this.group_teams = group_teams;
		}

		List<StringMap<StringMap>> pre_matches = send(matchesURL.replace("%%", tournament));
		if(pre_matches!=null)
		{
			List<Match> matches = new ArrayList<>();
			List<Integer> groups = new ArrayList<>();
			for(StringMap<StringMap> pre_match : pre_matches)
				if(pre_match.get("match")!=null)
				{
					Match match = new Match(pre_match.get("match"));
					matches.add(match);
					if(match.getGroup()!=null && !groups.contains(match.getGroup()))
						groups.add(match.getGroup());
					Collections.sort(groups);
				}

			setNextMatch(matches);
			setGroupStage(matches, groups);
			setFinalStage(matches);
			setSchedule(matches);
		}
	}

	private void setNextMatch(List<Match> matches){
		Match next = null; Date nextDate = null, date = null, now = new Date();
		for(Match match : matches)
		{
			if(next==null || nextDate == null){
				next = match;
				nextDate = parseDate(match.getStartTime());
				continue;
			}
			date = parseDate(match.getStartTime());
			if(date!=null && date.before(nextDate) && date.after(now)){
				next = match;
				nextDate = date;
			}
		}
		if(nextDate ==null || nextDate.before(now))
			return;

		this.nextMatch = next;

	}

	private void setGroupStage(List<Match> matches, List<Integer> groups){

		Map<Character, List<Team>> group_stage = new HashMap<>();
		for(int group : groups)
		{
			List<Team> teams = new ArrayList<>();
			for(Match match : matches)
			{
				if(match.getGroup()!=null && match.getGroup()==group)
				{
					Team team;
					if((team = group_teams.get(match.getTeam1()))!=null && !teams.contains(team))
						teams.add(team);

					if((team = group_teams.get(match.getTeam2()))!=null && !teams.contains(team))
						teams.add(team);

					if(match.getWinner()!=null)
					{
						if((team = group_teams.get(match.getWinner()))!=null)
							team.incWin();

						if((team = group_teams.get(match.getLoser()))!=null)
							team.incLose();
					}
				}

			}
			Collections.sort(teams, (t1, t2) -> {
				if(t2.getWins()-t1.getWins() !=0)
					return t2.getWins()-t1.getWins();
				return t1.getLoses()-t2.getLoses();}
			);
			group_stage.put((char)(groups.indexOf(group)+65), teams);

		}
		this.group_stage = group_stage;
	}

	private void setFinalStage(List<Match> matches){
		Map<Integer, Match> final_stage = new HashMap<>();

		for(Match match : matches)
		{
			if(match.getGroup()==null){
				switch (match.getStage()){
					case "A":
						final_stage.put(1, match);
						break;
					case "B":
						final_stage.put(2, match);
						break;
					case "C":
						final_stage.put(3, match);
						break;
					case "D":
						final_stage.put(4, match);
						break;
					case "E":
						final_stage.put(5, match);
						break;
					case "F":
						final_stage.put(6, match);
						break;
					case "G":
						final_stage.put(7, match);
						break;
					case "3P":
						final_stage.put(0, match);
						break;
				}
			}
		}
		this.final_stage = final_stage;
	}

	private void setSchedule(List<Match> matches){
		Map<Integer, List<Match>> schedule = new HashMap<>();
		Map<Integer, String> schedule_state = new HashMap<>();
		List<Match> finals = new ArrayList<>();
		List<Match> semis = new ArrayList<>();
		List<Match> quarters = new ArrayList<>();
		List<Match> groups = new ArrayList<>();

		for(Match match : matches)
		{
			if(match.getGroup()==null){
				switch (match.getStage()){
					case "A":
						if(final_stage.size()>6) quarters.add(match); else if(final_stage.size()>2) semis.add(match); else finals.add(match);
						break;
					case "B":
						if(final_stage.size()>6) quarters.add(match); else semis.add(match);
						break;
					case "C":
						if(final_stage.size()>6) quarters.add(match); else finals.add(match);
						break;
					case "D":
						quarters.add(match);
						break;
					case "E":
					case "F":
						semis.add(match);
						break;
					case "G":
					case "3P":
						finals.add(match);
						break;
				}
			}
			else groups.add(match);
		}

		if(!groups.isEmpty())
		{
			Collections.sort(groups, (match1, match2) -> {
				if(match1.getStartTimestamp()!= 0 && match2.getStartTimestamp() != 0)
					return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
				else if(match1.getStartTimestamp()!= 0 || match2.getStartTimestamp() != 0)
					return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
				return match1.getRound() - match2.getRound();
			});

			int rounds = (int)Math.ceil((double) groups.size() / (double)matchPerRound);

			for(int i=1; i <= rounds; i++)
				schedule.put(i, groups.subList((i-1)* matchPerRound, groups.size() > i * matchPerRound ? i*matchPerRound : groups.size()));

		}

		if(!quarters.isEmpty())
		{
			Collections.sort(quarters, (match1, match2) -> {
				if(match1.getStartTimestamp()!= 0 && match2.getStartTimestamp() != 0)
					return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
				else if(match1.getStartTimestamp()!= 0 || match2.getStartTimestamp() != 0)
					return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
				return match1.getRound() - match2.getRound();
			});
			schedule.put(-3, quarters);
		}

		if(!semis.isEmpty())
		{
			Collections.sort(semis, (match1, match2) -> {
				if(match1.getStartTimestamp()!= 0 && match2.getStartTimestamp() != 0)
					return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
				else if(match1.getStartTimestamp()!= 0 || match2.getStartTimestamp() != 0)
					return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
				return match1.getRound() - match2.getRound();
			});
			schedule.put(-2, semis);
		}

		if(!finals.isEmpty())
		{
			Collections.sort(finals, (match1, match2) -> {
				if(match1.getStartTimestamp()!= 0 && match2.getStartTimestamp() != 0)
					return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
				else if(match1.getStartTimestamp()!= 0 || match2.getStartTimestamp() != 0)
					return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
				return match1.getRound() - match2.getRound();
			});
			schedule.put(-1, finals);
		}

		int counter;
		for(int id : schedule.keySet())
		{
			counter = 0;
			for(Match match : schedule.get(id))
				if(match.getWinner() != null)
					counter++;
			if(counter==schedule.get(id).size())
				schedule_state.put(id, "past-week");
			else if(counter>0)
				schedule_state.put(id, "current-week");
			else
				schedule_state.put(id, "future-week");
		}

		this.schedule = schedule;
		this.schedule_state = schedule_state;
	}

	public Map<Integer, Team> getGroupTeams(){ return group_teams; }
	public Map<Integer, Team> getFinalTeams(){ return final_teams; }

	public Map<Integer, Match> getFinalStage()	{ return final_stage; }
	public Map<Character, List<Team>> getGroupStage()	{ return group_stage; }


	public Map<Integer, List<Match>> getSchedule()	{return schedule;}
	public Map<Integer, String> getScheduleState()	{return schedule_state;}
	public Match getNextMatch(){ return nextMatch; }

	private Date parseDate(Object obj){
		if(obj==null)
			return null;

		Date date = null;
		try
		{
			date = timezoneFormat.parse((String) obj);
		}
		catch(ParseException | ClassCastException e)
		{
			e.printStackTrace();
		}
		return date;
	}

	private <T> List send(String url){
		return send(url, null);
	}

	private <T> List send(String url, Map<String, String> body){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("Content-Type", "application/json");
		String plainCreds = credintials;
		byte[] base64Creds = Base64.encodeBase64(plainCreds.getBytes());
		map.put("Authorization", "Basic " + new String(base64Creds));
		headers.setAll(map);
		HttpEntity<?> params = new HttpEntity<>(body, headers);

		ResponseEntity<?> response = new RestTemplate().exchange(url, HttpMethod.GET, params, String.class);
		Gson gson = new Gson();
		return gson.fromJson(response.getBody().toString(), List.class);
	}

	private StringMap sendOne(String url, Map<String, String> body){
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("Content-Type", "application/json");
		String plainCreds = credintials;
		byte[] base64Creds = Base64.encodeBase64(plainCreds.getBytes());
		map.put("Authorization", "Basic " + new String(base64Creds));
		headers.setAll(map);
		HttpEntity<?> params = new HttpEntity<>(body, headers);

		ResponseEntity<?> response = new RestTemplate().exchange(url, HttpMethod.GET, params, String.class);
		Gson gson = new Gson();
		return gson.fromJson(response.getBody().toString(), StringMap.class);
	}

	public class Match{
		Integer team1, team2, winner, loser, round, group, score1, score2;
		String state, identifier, starttime, localtime, localdate;
		Date start;

		Match(StringMap match_opt){
			this.team1 = match_opt.get("player1_id")== null? null : ((Double) match_opt.get("player1_id")).intValue();
			this.team2 = match_opt.get("player2_id")== null? null : ((Double) match_opt.get("player2_id")).intValue();
			this.winner = match_opt.get("winner_id")== null? null : ((Double) match_opt.get("winner_id")).intValue();
			this.loser = match_opt.get("loser_id")== null? null : ((Double) match_opt.get("loser_id")).intValue();
			this.round = match_opt.get("round")== null? null : ((Double) match_opt.get("round")).intValue();
			this.group = match_opt.get("group_id")== null? null : ((Double) match_opt.get("group_id")).intValue();
			this.starttime =  (String) match_opt.get("scheduled_time");
			start = parseDate(match_opt.get("scheduled_time"));
			this.localtime = start == null ? null : timeFormat.format(start);
			this.localdate = start == null ? null : dateFormat.format(start);
			this.state = (String) match_opt.get("state");
			this.identifier = (String) match_opt.get("identifier");
			String[] scores = match_opt.get("scores_csv") == null? null : ((String) match_opt.get("scores_csv")).split("-");
			try{score1 = Integer.parseInt(scores[0]);}catch(Exception e){ score1 = 0; };
			try{score2 = Integer.parseInt(scores[1]);}catch(Exception e){ score2 = 0; };
		}

		public Integer getTeam1(){ return team1;}
		public Integer getTeam2(){ return team2;}
		public Integer getRound(){ return round==null? 0 : round;}
		public Integer getGroup(){ return group;}
		public Integer getWinner(){ return winner;}
		public Integer getLoser(){ return loser;}
		public String getStartTime(){ return starttime;}
		public String getLocalTime(){ return localtime;}
		public String getLocalDate(){ return localdate;}
		public String getStage(){ return identifier;}
		public String getState(){ return state;}
		public long getStartTimestamp(){ return start==null? 0 : start.getTime();}
		public Date getStart(){ return start;}
		public int getScore1(){ return score1;}
		public int getScore2(){ return score2;}
	}

	public class Team{
		int groupId, id, wins, loses;
		String name, icon;

		Team(StringMap team_opt){
			this.groupId = ((Double) (team_opt.get("group_player_ids") == null? 0 : ((ArrayList)team_opt.get("group_player_ids")).get(0))).intValue();
			this.id = ((Double) team_opt.get("id")).intValue();
			String value = (String) team_opt.get("attached_participatable_portrait_url");
			this.icon = value == null? "/contest/images/icons/"+((Double)team_opt.get("seed")).intValue()+".png"  : value;
			this.name =  (String) team_opt.get("display_name");
		}
		public int getGroupId(){ return groupId;}
		public int getId(){ return id;}
		public int getWins(){ return wins;}
		public int getLoses(){ return loses;}
		public String getName(){ return name;}
		public String getIcon(){ return icon;}
		public void incWin(){wins++;}
		public void incLose(){loses++;}
	}
}