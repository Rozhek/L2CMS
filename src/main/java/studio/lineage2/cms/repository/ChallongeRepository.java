package studio.lineage2.cms.repository;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.StringMap;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by iRock
 * 23.11.2015
 */
@SuppressWarnings("rawtypes")
@Component
public class ChallongeRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${credentials}")
    private String credentials;

    @Value("${tournament}")
    private String tournament;

    @Value("${matchPerRound}")
    private int matchPerRound;

    private static final SimpleDateFormat TIMEZONE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd/MM", Locale.ENGLISH);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("ha 'GMT'XXX", Locale.ENGLISH);
    private static final String matchesURL = "https://api.challonge.com/v1/tournaments/%%/matches.json";
    private static final String teamsURL = "https://api.challonge.com/v1/tournaments/%%/participants.json";
    private static final String attachmentURL = "https://api.challonge.com/v1/tournaments/%%/matches/$$/attachments.json";

    private Match nextMatch;

    private Map<Integer, Team> groupTeams = new HashMap<>();
    private Map<Character, List<Team>> groupStage = new HashMap<>();

    private Map<Integer, Team> finalTeams = new HashMap<>();
    private Map<Integer, Match> finalStage = new HashMap<>();

    private Map<Integer, List<Match>> schedule = new HashMap<>();
    private Map<Integer, String> scheduleState = new HashMap<>();

    @Scheduled(initialDelay = 1000, fixedRate = 5 * 60 * 1000L)
    public void load() {
        if (tournament.isEmpty())
            return;

        List<StringMap<StringMap>> preTeams = request(teamsURL.replace("%%", tournament));
        if (preTeams != null) {
            Map<Integer, Team> finalTeams = new HashMap<>(), groupTeams = new HashMap<>();
            StringMap value;
            for (StringMap<StringMap> preTeam : preTeams)
                if ((value = preTeam.get("participant")) != null) {
                    int id = ((Double) value.get("id")).intValue();
                    String name = Optional.of(value.get("display_name"))
                            .map(String.class::cast)
                            .orElse(StringUtils.EMPTY);
                    int seed = ((Double) value.get("seed")).intValue();
                    String icon = Optional.ofNullable(value.get("attached_participatable_portrait_url"))
                            .map(String.class::cast)
                            .orElse("/contest/images/icons/" + seed + ".png");

                    int groupId = 0;
                    List<?> groups = (List<?>) value.get("group_player_ids");
                    if (groups != null && !groups.isEmpty())
                        groupId = ((Double) groups.get(0)).intValue();

                    Team team = new Team(id, name, icon, groupId);
                    finalTeams.put(team.getId(), team);
                    groupTeams.put(team.getGroupId(), team);
                }
            this.finalTeams = finalTeams;
            this.groupTeams = groupTeams;
        }

        List<StringMap<StringMap>> preMatches = request(matchesURL.replace("%%", tournament));
        if (preMatches != null) {
            List<Match> matches = new ArrayList<>();
            List<Integer> groups = new ArrayList<>();
            StringMap value;
            for (StringMap<StringMap> preMatch : preMatches)
                if ((value = preMatch.get("match")) != null) {
                    int id = ((Double) value.get("id")).intValue();
                    Integer team1 = Optional.ofNullable(value.get("player1_id"))
                            .map(Double.class::cast)
                            .map(Double::intValue)
                            .orElse(null);
                    Integer team2 = Optional.ofNullable(value.get("player2_id"))
                            .map(Double.class::cast)
                            .map(Double::intValue)
                            .orElse(null);
                    Integer winner = Optional.ofNullable(value.get("winner_id"))
                            .map(Double.class::cast)
                            .map(Double::intValue)
                            .orElse(null);
                    Integer loser = Optional.ofNullable(value.get("loser_id"))
                            .map(Double.class::cast)
                            .map(Double::intValue)
                            .orElse(null);
                    Integer round = Optional.ofNullable(value.get("round"))
                            .map(Double.class::cast)
                            .map(Double::intValue)
                            .orElse(null);
                    Integer group = Optional.ofNullable(value.get("group_id"))
                            .map(Double.class::cast)
                            .map(Double::intValue)
                            .orElse(null);
                    Date startDate = Optional.ofNullable(value.get("scheduled_time"))
                            .map(this::parseDate)
                            .orElse(null);
                    String state = Optional.ofNullable(value.get("state"))
                            .map(String.class::cast)
                            .orElse(null);
                    String identifier = Optional.ofNullable(value.get("identifier"))
                            .map(String.class::cast)
                            .orElse(null);

                    int scoreTeam1 = 0;
                    int scoreTeam2 = 0;
                    Optional<String[]> scores = Optional.ofNullable(value.get("scores_csv"))
                            .map(String.class::cast)
                            .map(s -> s.split("-"))
                            .filter(score -> score.length >= 2);
                    if (scores.isPresent()) {
                        scoreTeam1 = Integer.parseInt(scores.get()[0]);
                        scoreTeam2 = Integer.parseInt(scores.get()[1]);
                    }

                    String link = null;
                    Optional<Double> attachments = Optional.ofNullable(value.get("attachment_count"))
                            .map(Double.class::cast)
                            .filter(d -> d > 0);
                    if (attachments.isPresent()) {
                        link = getAttachmentLink(id).orElse(null);
                    }

                    Match match = new Match(team1, team2,
                            scoreTeam1, scoreTeam2,
                            winner, loser,
                            round, group,
                            state, identifier,
                            startDate, link);
                    matches.add(match);
                    if (match.getGroup() != null && !groups.contains(match.getGroup()))
                        groups.add(match.getGroup());
                    Collections.sort(groups);
                }

            setNextMatch(matches);
            setGroupStage(matches, groups);
            setFinalStage(matches);
            setSchedule(matches);
        }
    }

    private void setNextMatch(List<Match> matches) {
        Match nextMatch = null;
        Date nextDate = null;
        Date now = new Date();
        for (Match match : matches) {
            Date date = match.getStart();

            if (nextMatch == null || nextDate == null) {
                nextMatch = match;
                nextDate = date;
                continue;
            }

            if (date != null && date.before(nextDate) && date.after(now)) {
                nextMatch = match;
                nextDate = date;
            }
        }
        if (nextDate == null || nextDate.before(now))
            return;

        this.nextMatch = nextMatch;

    }

    private void setGroupStage(List<Match> matches, List<Integer> groups) {

        Map<Character, List<Team>> groupStage = new HashMap<>();
        for (int group : groups) {
            List<Team> teams = new ArrayList<>();
            for (Match match : matches) {
                if (match.getGroup() != null && match.getGroup() == group) {
                    Team team;
                    if ((team = groupTeams.get(match.getTeam1())) != null && !teams.contains(team))
                        teams.add(team);

                    if ((team = groupTeams.get(match.getTeam2())) != null && !teams.contains(team))
                        teams.add(team);

                    if (match.getWinner() != null) {
                        if ((team = groupTeams.get(match.getWinner())) != null)
                            team.incWin();

                        if ((team = groupTeams.get(match.getLoser())) != null)
                            team.incLose();
                    }
                }

            }
            teams.sort((t1, t2) -> {
                if (t2.getWins() - t1.getWins() != 0)
                    return t2.getWins() - t1.getWins();
                return t1.getLoses() - t2.getLoses();
            });
            groupStage.put((char) (groups.indexOf(group) + 65), teams);

        }
        this.groupStage = groupStage;
    }

    private void setFinalStage(List<Match> matches) {
        Map<Integer, Match> finalStage = new HashMap<>();

        for (Match match : matches) {
            if (match.getGroup() == null) {
                switch (match.getStage()) {
                    case "A":
                        finalStage.put(1, match);
                        break;
                    case "B":
                        finalStage.put(2, match);
                        break;
                    case "C":
                        finalStage.put(3, match);
                        break;
                    case "D":
                        finalStage.put(4, match);
                        break;
                    case "E":
                        finalStage.put(5, match);
                        break;
                    case "F":
                        finalStage.put(6, match);
                        break;
                    case "G":
                        finalStage.put(7, match);
                        break;
                    case "3P":
                        finalStage.put(0, match);
                        break;
                }
            }
        }
        this.finalStage = finalStage;
    }

    private void setSchedule(List<Match> matches) {
        Map<Integer, List<Match>> schedule = new HashMap<>();
        Map<Integer, String> scheduleState = new HashMap<>();
        List<Match> finals = new ArrayList<>();
        List<Match> semis = new ArrayList<>();
        List<Match> quarters = new ArrayList<>();
        List<Match> groups = new ArrayList<>();

        for (Match match : matches) {
            if (match.getGroup() == null) {
                switch (match.getStage()) {
                    case "A":
                        if (finalStage.size() > 6) quarters.add(match);
                        else if (finalStage.size() > 2) semis.add(match);
                        else finals.add(match);
                        break;
                    case "B":
                        if (finalStage.size() > 6) quarters.add(match);
                        else semis.add(match);
                        break;
                    case "C":
                        if (finalStage.size() > 6) quarters.add(match);
                        else finals.add(match);
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
            } else groups.add(match);
        }

        if (!groups.isEmpty()) {
            groups.sort((match1, match2) -> {
                if (match1.getStartTimestamp() != 0 && match2.getStartTimestamp() != 0)
                    return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
                else if (match1.getStartTimestamp() != 0 || match2.getStartTimestamp() != 0)
                    return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
                return match1.getRound() - match2.getRound();
            });

            int rounds = (int) Math.ceil((double) groups.size() / (double) matchPerRound);
            for (int i = 1; i <= rounds; i++)
                schedule.put(i, groups.subList((i - 1) * matchPerRound, Math.min(groups.size(), i * matchPerRound)));

        }

        if (!quarters.isEmpty()) {
            quarters.sort((match1, match2) -> {
                if (match1.getStartTimestamp() != 0 && match2.getStartTimestamp() != 0)
                    return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
                else if (match1.getStartTimestamp() != 0 || match2.getStartTimestamp() != 0)
                    return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
                return match1.getRound() - match2.getRound();
            });
            schedule.put(-3, quarters);
        }

        if (!semis.isEmpty()) {
            semis.sort((match1, match2) -> {
                if (match1.getStartTimestamp() != 0 && match2.getStartTimestamp() != 0)
                    return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
                else if (match1.getStartTimestamp() != 0 || match2.getStartTimestamp() != 0)
                    return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
                return match1.getRound() - match2.getRound();
            });
            schedule.put(-2, semis);
        }

        if (!finals.isEmpty()) {
            finals.sort((match1, match2) -> {
                if (match1.getStartTimestamp() != 0 && match2.getStartTimestamp() != 0)
                    return (int) ((match1.getStartTimestamp() - match2.getStartTimestamp()) / 1000);
                else if (match1.getStartTimestamp() != 0 || match2.getStartTimestamp() != 0)
                    return (int) ((match2.getStartTimestamp() - match1.getStartTimestamp()) / 1000);
                return match1.getRound() - match2.getRound();
            });
            schedule.put(-1, finals);
        }

        int counter;
        for (int id : schedule.keySet()) {
            counter = 0;
            for (Match match : schedule.get(id))
                if (match.getWinner() != null)
                    counter++;
            if (counter == schedule.get(id).size())
                scheduleState.put(id, "past-week");
            else if (counter > 0)
                scheduleState.put(id, "current-week");
            else
                scheduleState.put(id, "future-week");
        }

        this.schedule = schedule;
        this.scheduleState = scheduleState;
    }

    public Map<Integer, Team> getGroupTeams() {
        return groupTeams;
    }

    public Map<Integer, Team> getFinalTeams() {
        return finalTeams;
    }

    public Map<Integer, Match> getFinalStage() {
        return finalStage;
    }

    public Map<Character, List<Team>> getGroupStage() {
        return groupStage;
    }


    public Map<Integer, List<Match>> getSchedule() {
        return schedule;
    }

    public Map<Integer, String> getScheduleState() {
        return scheduleState;
    }

    public Match getNextMatch() {
        return nextMatch;
    }

    private Date parseDate(Object obj) {
        if (obj == null)
            return null;

        Date date = null;
        try {
            date = TIMEZONE_FORMAT.parse((String) obj);
        } catch (ParseException | ClassCastException e) {
            e.printStackTrace();
        }
        return date;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> request(String url) {
        HttpHeaders headers = new HttpHeaders() {{
            String auth = credentials.trim();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
            set("user-agent", "web.client.RestTemplate");
            setContentType(MediaType.APPLICATION_JSON);
        }};
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<?> response;
        try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, request, String.class);
        } catch (HttpClientErrorException ex) {
            logger.error("Error while process request {}:{}", url, request, ex);
            return Collections.emptyList();
        }

        List<T> result;
        Gson gson = new Gson();
        try {
            result = (List<T>) gson.fromJson(response.getBody().toString(), List.class);
        } catch (JsonSyntaxException ex) {
            logger.error("Error while process gson response {}", response.getBody(), ex);
            return Collections.emptyList();
        }
        return result;
    }

    private Optional<String> getAttachmentLink(int matchId) {
        List<StringMap<StringMap>> attachments = request(
                attachmentURL
                        .replace("%%", tournament)
                        .replace("$$", String.valueOf(matchId))
        );

        if (attachments == null)
            return Optional.empty();

        return attachments.stream()
                .map(a -> a.get("match_attachment"))
                .filter(Objects::nonNull)
                .map(a -> a.get("url"))
                .map(String.class::cast)
                .findFirst();
    }

    @SuppressWarnings("unused")
    public static class Match {
        private final Integer team1;
        private final Integer team2;
        private final Integer winner;
        private final Integer loser;
        private final Integer round;
        private final Integer group;
        private final Integer score1;
        private final Integer score2;
        private final String state;
        private final String identifier;
        private final Date start;
        private final String link;

        public Match(Integer team1, Integer team2,
                     Integer score1, Integer score2,
                     Integer winner, Integer loser,
                     Integer round, Integer group,
                     String state, String identifier,
                     Date start, String link) {
            this.team1 = team1;
            this.team2 = team2;
            this.winner = winner;
            this.loser = loser;
            this.round = round;
            this.group = group;
            this.score1 = score1;
            this.score2 = score2;
            this.state = state;
            this.identifier = identifier;
            this.start = start;
            this.link = link;
        }

        public Integer getTeam1() {
            return team1;
        }

        public Integer getTeam2() {
            return team2;
        }

        public Integer getRound() {
            return round == null ? 0 : round;
        }

        public Integer getGroup() {
            return group;
        }

        public Integer getWinner() {
            return winner;
        }

        public Integer getLoser() {
            return loser;
        }

        public String getLocalTime() {
            if (start == null)
                return null;

            return TIME_FORMAT.format(start);
        }

        public String getLocalDate() {
            if (start == null)
                return null;

            return DATE_FORMAT.format(start);
        }

        public String getStage() {
            return identifier;
        }

        public String getState() {
            return state;
        }

        public long getStartTimestamp() {
            return start == null ? 0 : start.getTime();
        }

        public Date getStart() {
            return start;
        }

        public String getStartString() {
            return start == null ? null : TIMEZONE_FORMAT.format(start);
        }

        public int getScore1() {
            return score1;
        }

        public int getScore2() {
            return score2;
        }

        public String getLink() {
            return link;
        }
    }

    @SuppressWarnings("unused")
    public static class Team {
        private final int id;
        private final String name;
        private final String icon;
        private final int groupId;
        private int wins;
        private int loses;

        public Team(int id, String name, String icon, int groupId) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.groupId = groupId;
        }

        public int getGroupId() {
            return groupId;
        }

        public int getId() {
            return id;
        }

        public int getWins() {
            return wins;
        }

        public int getLoses() {
            return loses;
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public void incWin() {
            wins++;
        }

        public void incLose() {
            loses++;
        }
    }
}