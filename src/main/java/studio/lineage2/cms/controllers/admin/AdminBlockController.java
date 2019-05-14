package studio.lineage2.cms.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import studio.lineage2.cms.model.Block;
import studio.lineage2.cms.service.BlockService;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 Created by iRock
 29.10.2015
 */
@Controller
@RequestMapping("/admin/blocks")
public class AdminBlockController
{
	@Autowired
	private BlockService blockService;

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String show(ModelMap model, Locale locale)
	{
		model.addAttribute("block", new Block());
		model.addAttribute("adminmenu", true);
		model.addAttribute("pool_amount", blockService.getVar("pool_amount"));
		model.addAttribute("pool_donate", blockService.getVar("pool_donate"));
		List<Block> blockLists = blockService.findAll();
		Collections.reverse(blockLists);
		model.addAttribute("blockList", blockLists);
		model.addAttribute("container", LocalizePath.build(locale, "cp/$$/admin/block/create.vm"));
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/block.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "add", method = { RequestMethod.POST })
	public @ResponseBody String add(ModelMap model, @ModelAttribute Block block)
	{
		if(block.getTitle().isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Введите название");

		blockService.save(block);
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Блок успешно добавлен", "setTimeout(function(){location.reload(true);}, 1500);");
	}

	@RequestMapping(value = "remove/{id}", method = { RequestMethod.GET })
	public String remove(ModelMap model, @PathVariable long id, Locale locale)
	{
		blockService.delete(id);
		return show(model, locale);
	}

	@RequestMapping(value = "edit/{id}", method = { RequestMethod.GET })
	public String edit(ModelMap model, @PathVariable long id, Locale locale)
	{
		model.addAttribute("block", blockService.findOne(id));
		model.addAttribute("adminmenu", true);
		model.addAttribute("pool_amount", blockService.getVar("pool_amount"));
		model.addAttribute("pool_donate", blockService.getVar("pool_donate"));
		List<Block> blockLists = blockService.findAll();
		Collections.reverse(blockLists);
		model.addAttribute("blockList", blockLists);
		model.addAttribute("container", LocalizePath.build(locale, "cp/$$/admin/block/edit.vm"));
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/admin/block.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	@RequestMapping(value = "edit", method = { RequestMethod.POST })
	public @ResponseBody String edit(ModelMap model, long id, @ModelAttribute Block block, Locale locale)
	{
		if(block.getTitle().isEmpty())
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Введите название");

		Block new_block = blockService.findOne(id);
		new_block.setType(block.getType());
		new_block.setTitle(block.getTitle());
		new_block.setText(block.getText());
		new_block.setLang(block.getLang());
		new_block.setSrc(block.getSrc());
		new_block.setBackground(block.getBackground());
		blockService.save(new_block);
		return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Блок успешно изменен", "setTimeout(function(){location.reload(true);}, 1500);");
	}

	@RequestMapping(value = "setparam", method = { RequestMethod.POST })
	public @ResponseBody String setparam(ModelMap model, String param1, String param2, String param3, @RequestParam Map<String,String> params, Locale locale)
	{
		boolean result = false;
		if(param1!=null) result = blockService.setVar(param1, params.get(param1));
		if(param2!=null) result = blockService.setVar(param2, params.get(param2));
		if(param3!=null) result = blockService.setVar(param3, params.get(param3));
		if(result)
			return JsonMessage.generateMessage(JsonMessage.JsonType.success, "Параметры успешно сохранены", "setTimeout(function(){location.reload(true);}, 1500);");
		else
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "Ошибка при сохранении параметров", "setTimeout(function(){location.reload(true);}, 1500);");
	}
}