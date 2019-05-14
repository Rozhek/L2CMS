package studio.lineage2.cms.controllers.esport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import studio.lineage2.cms.repository.ChallongeRepository;
import studio.lineage2.cms.utils.LocalizePath;

import java.util.Locale;

/**
 Created by iRock
 23.10.2015
 */
@Controller
@RequestMapping("/contest")
public class EsportController
{
	@Autowired
	private ChallongeRepository challongeRepository;

	@RequestMapping(method = {
			RequestMethod.GET,
			RequestMethod.HEAD
	})
	public String showIndex(ModelMap model, Locale locale)
	{
		model.addAttribute("groupTeams", challongeRepository.getGroupTeams());//group stage
		model.addAttribute("finalTeams", challongeRepository.getFinalTeams());//bracket stage
		model.addAttribute("nextmatch", challongeRepository.getNextMatch());
		model.addAttribute("page", LocalizePath.build(locale, "contest/$$/main.vm"));
		return LocalizePath.build(locale, "contest/$$/index");
	}

	@RequestMapping("/schedule")
	public String showSchedule(ModelMap model, Locale locale)
	{
		model.addAttribute("groupTeams", challongeRepository.getGroupTeams());//group stage
		model.addAttribute("finalTeams", challongeRepository.getFinalTeams());//bracket stage
		model.addAttribute("schedule", challongeRepository.getSchedule());
		model.addAttribute("schedule_state", challongeRepository.getScheduleState());
		model.addAttribute("nextmatch", challongeRepository.getNextMatch());
		model.addAttribute("page", LocalizePath.build(locale, "contest/$$/schedule.vm"));
		return LocalizePath.build(locale, "contest/$$/index");
	}

	@RequestMapping("/bracket")
	public String showBracket(ModelMap model, Locale locale)
	{
		model.addAttribute("groupTeams", challongeRepository.getGroupTeams());//group stage
		model.addAttribute("groups", challongeRepository.getGroupStage());//group stage

		model.addAttribute("finalTeams", challongeRepository.getFinalTeams());//bracket stage
		model.addAttribute("final", challongeRepository.getFinalStage());//bracket stage
		model.addAttribute("nextmatch", challongeRepository.getNextMatch());
		model.addAttribute("page", LocalizePath.build(locale, "contest/$$/bracket.vm"));
		return LocalizePath.build(locale, "contest/$$/index");
	}
}