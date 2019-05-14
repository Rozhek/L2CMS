package studio.lineage2.cms.controllers;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import studio.lineage2.cms.model.Account;
import studio.lineage2.cms.model.Player;
import studio.lineage2.cms.model.User;
import studio.lineage2.cms.model.UserIp;
import studio.lineage2.cms.model.UserItemLog;
import studio.lineage2.cms.model.UserItemType;
import studio.lineage2.cms.repository.GameServerRepository;
import studio.lineage2.cms.service.AccountService;
import studio.lineage2.cms.service.BlockService;
import studio.lineage2.cms.service.LocalizedMessageService;
import studio.lineage2.cms.service.PlayerService;
import studio.lineage2.cms.service.UserIpService;
import studio.lineage2.cms.service.UserItemsService;
import studio.lineage2.cms.repository.UserRepository;
import studio.lineage2.cms.utils.LocalizePath;
import studio.lineage2.cms.xmlrpc.JsonMessage;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 Created by iRock
 04.11.2015
 */
@Controller
public class PayController
{
	@Value("${domain}")
	private String domain;

	@Value("${freekassaMerchantId}")
	private int freekassaMerchantId;

	@Value("${freekassaSecretKey1}")
	private String freekassaSecretKey1;

	@Value("${freekassaSecretKey2}")
	private String freekassaSecretKey2;

	@Value("${unitpayPublicKey}")
	private String unitpayPublicKey;

	@Value("${unitpaySecretKey}")
	private String unitpaySecretKey;

	@Value("${unitpayDonateRate}")
	private float unitpayDonateRate;

	@Value("${g2aMerchantId}")
	private String g2aMerchantId;

	@Value("${g2aSecretKey1}")
	private String g2aSecretKey1;

	@Value("${g2aSecretKey2}")
	private String g2aSecretKey2;

	@Value("${server.proxy.cloudflare}")
	private boolean CFEnabled;

	@Autowired
	private UserItemsService userItemsService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountService accountService;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private UserIpService userIpService;

	@Autowired
	private BlockService blockService;

	@Autowired
	private GameServerRepository gameServerRepository;

	@Autowired
	private LocalizedMessageService localizedMessage;

	private static final List<String> unitPayIps = new ArrayList<>(Arrays.asList("31.186.100.49", "178.132.203.105", "52.29.152.23", "52.19.56.234"));

	@RequestMapping(value="/account/donate", method = {
			RequestMethod.GET,
			RequestMethod.HEAD
	})
	public String show(ModelMap model, Locale locale)
	{
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!(object instanceof User))
		{
			return "redirect:/login";
		}
		User user = (User) object;
		List<Account> accounts = accountService.findAllByUserId(user.getId());
		model.addAttribute("accounts", accounts);
		List<Player> players = new ArrayList<>();
		for(Account acc : accounts)
			players.addAll(playerService.findAllByAccountId(acc.getId()));
		model.addAttribute("players", players);
		model.addAttribute("donatemenu", true);
		model.addAttribute("unitpayEnabled", !unitpaySecretKey.isEmpty());
		model.addAttribute("g2aEnabled", !g2aSecretKey2.isEmpty());
		model.addAttribute("page", LocalizePath.build(locale, "cp/$$/account/donate.vm"));
		return LocalizePath.build(locale, "cp/$$/index");
	}

	public String init(User user, String payType, String quanity, String account, Locale locale)
	{
		if(!user.getUsername().equals(account))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("account.change_danger", locale));
		}

		if(quanity ==null || !quanity.matches("[0-9]{1,4}"))
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("item.count_invalid", locale));
		}

		if(Integer.parseInt(quanity)<1)
		{
			return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("item.count_invalid", locale));
		}

		switch(payType)
		{
			/*case "free-kassa":
				String md5 = freekassaMerchantId + ":" + quanity + ":" + freekassaSecretKey1 + ":" + user.getId();
				md5 = md5(md5);
				return "redirect:http://www.free-kassa.ru/merchant/cash.php?m=" + freekassaMerchantId + "&oa=" + Integer.parseInt(quanity)*6.2f + "&o=" + user.getId() + "&s=" + md5;*/
			case "unitpay":
				return JsonMessage.generateMessage(JsonMessage.JsonType.success, localizedMessage.getMessage("message.donate_redirect", locale), "setTimeout(function(){location.href=\"https://unitpay.ru/pay/" + unitpayPublicKey + "?sum=" + Integer.parseInt(quanity) * unitpayDonateRate + "&account=" + user.getId() + "&desc=" + localizedMessage.getMessage("message.donate_desc", locale, new String[]{quanity,domain}) +"\";}, 1500);");
			case "g2a":
				return JsonMessage.generateMessage(JsonMessage.JsonType.danger, "G2A service is not connected.");
				/*G2AOrder answer = g2aCreatePayment(Integer.parseInt(quanity), 0.13f, user.getUsername());
				if(answer.getStatus().equals("ok"))
					return JsonMessage.generateMessage(JsonMessage.JsonType.success, answer.getStatus(), "https://checkout.test.pay.g2a.com/index/gateway?token="+answer.getToken());
				else
					return JsonMessage.generateMessage(JsonMessage.JsonType.danger, answer.getStatus());*/

		}
		return JsonMessage.generateMessage(JsonMessage.JsonType.danger, localizedMessage.getMessage("simplemsg.error", locale));
	}

	public G2AOrder g2aCreatePayment(int quanity, float price, String username) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("Content-Type", "application/json");
		map.put("Authorization", g2aSecretKey1+";"+sha256(g2aSecretKey1+g2aSecretKey2+g2aMerchantId));
		headers.setAll(map);

		float total = (float)quanity * price;
		String strAmount = String.format(Locale.US, "%.2f", total);
		String strPrice = String.format(Locale.US, "%.2f", price);
		String curr = "EUR";
		Map<String, String> req_payload = new HashMap<>();
		req_payload.put("api_hash", g2aSecretKey1);
		req_payload.put("hash", sha256("1" + strAmount + curr + g2aSecretKey2));
		req_payload.put("order_id", "1");
		req_payload.put("amount", strAmount);
		req_payload.put("currency", curr);
		req_payload.put("email", username);
		req_payload.put("url_failure", "https://"+domain+"/account/donate/failed");
		req_payload.put("url_ok", "https://"+domain+"/account/donate/success");
		req_payload.put("items", "[{\"sku\":\"1-credit\",\"name\":\"Credit\",\"amount\":\""+strAmount+"\",\"type\":\"credits\",\"qty\":"+quanity+",\"price\":\""+strPrice+"\",\"id\":\"1\",\"url\":\"https://"+domain+"/account/donate\"}]");
		req_payload.put("cart_type", "digital");
		/*
		api_hash=38be425a-63d0-4c46-8733-3e9ff662d62d
		&hash=ac0945d82b8589959b5f4ffafcc1a6c5983e82b8b4094c377a7b9c43d4a432bc   {userOrderId}{amount}{currency}{ApiSecret}
		&order_id=2845
		&amount=15
		&currency=EUR
		&email=stefan@my-test-store.com
		&url_failure=http://my-test-store.com/order/fail
		&url_ok=http://my-test-store.com/order/success
		&items=[{"sku":"450","name":"Test Item","amount":"15","type":"item_type","qty":"1","price":15,"id":"5619","url":"http://example.com/products/item/example-item-name-5619"}]
		*/
		HttpEntity<?> request = new HttpEntity<>(req_payload, headers);
		String url = "https://checkout.test.pay.g2a.com/index/createQuote";
		ResponseEntity<?> response;
		try
		{
			response = new RestTemplate().postForEntity(url, request, String.class);
		}
		catch(RestClientException e){
			return new G2AOrder(e.getMessage(),"");
		}
		Gson gson = new Gson();
		return gson.fromJson(response.getBody().toString(), G2AOrder.class);
	}

	private class G2AOrder{
		String status, token;
		private G2AOrder(String status, String token){
			this.status = status;
			this.token = token;
		}
		private String getStatus(){ return status;}
		private String getToken(){ return token;}
	}

	@RequestMapping(value = "/pay/checkfreekassa", method = { RequestMethod.GET })
	public
	@ResponseBody
	String checkfreekassa(String MERCHANT_ID, String AMOUNT, String MERCHANT_ORDER_ID, String SIGN)
	{
		String md5 = md5(MERCHANT_ID + ":" + AMOUNT + ":" + freekassaSecretKey2 + ":" + MERCHANT_ORDER_ID);
		if(!md5.equalsIgnoreCase(SIGN))
		{
			return "NO";
		}
		Long user = Long.parseLong(MERCHANT_ORDER_ID);
		UserItemLog itemLog = userItemsService.addUserItem(user, UserItemType.MONEY.getName(), UserItemType.MONEY.getId(), Long.parseLong(AMOUNT), "logging.donate", AMOUNT + " " + UserItemType.MONEY.getName());
		if(itemLog!=null)
		{
			userIpService.save(new UserIp(user, "", Calendar.getInstance().getTimeInMillis(), itemLog.getId(), true, ""));
		}
		return "YES";
	}

	@RequestMapping(value = "/pay/checkunitpay", method = { RequestMethod.GET })
	public
	@ResponseBody
	String checkunitpay(HttpServletRequest request)
	{
		//check ip block
		String ip = CFEnabled? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
		if(!unitPayIps.contains(ip))
			return "{\"error\": {\"message\": \"Запрос получен с неизвестного сервера\"}}";


		Map<String, String[]> params = request.getParameterMap();
		//params validation block
		String sign, method, account_param, ordersum_param;
		int test;
		if(params.get("method") == null || (method = params.get("method")[0]) == null)
			return "{\"error\": {\"message\": \"Неизвестный запрос\"}}";
		if(params.get("params[signature]") == null || (sign = params.get("params[signature]")[0]) ==null)
			return "{\"error\": {\"message\": \"Неверная электронная подпись\"}}";
		if(params.get("params[account]") == null || (account_param = params.get("params[account]")[0]) == null)
			return "{\"error\": {\"message\": \"Неверный аккаунт\"}}";
		if(params.get("params[orderSum]") == null || (ordersum_param = params.get("params[orderSum]")[0]) == null)
			return "{\"error\": {\"message\": \"Отсутствует сумма поступления\"}}";
		if(params.get("params[test]") == null || params.get("params[test]")[0] == null) test = 0;
		else test = Integer.parseInt(params.get("params[test]")[0]);

		//check sign block
		StringBuilder checkSign = new StringBuilder();
		checkSign.append(method).append("{up}");
		for(Map.Entry<String, String[]> param : params.entrySet())
		{
			if(param.getKey().equalsIgnoreCase("method") || param.getKey().equalsIgnoreCase("params[signature]") || param.getKey().equalsIgnoreCase("params[sign]"))
			{
				continue;
			}
			checkSign.append(param.getValue()[0]).append("{up}");
		}
		checkSign.append(unitpaySecretKey);
		if(!sign.equals(sha256(checkSign.toString())))
		{
			return "{\"error\": {\"message\": \"Электронная подпись не верна\"}}";
		}

		//check for test block
		if(test == 1)
			return "{\"result\": {\"message\": \"Тестовый запрос успешно обработан\"}}";

		//check payment params
		long account;
		double orderSum;
		try
		{
			account = Integer.parseInt(account_param);
			orderSum = Double.parseDouble(ordersum_param);
		}
		catch(Exception e){
			return "{\"error\": {\"message\": \"Неверный формат параметров запроса\"}}";
		}

		//check sum
		long count = (long)Math.floor(orderSum/unitpayDonateRate);
		if(count < 1)
			return "{\"error\": {\"message\": \"Неверная сумма\"}}";

		//try to processing querry
		try
		{
			switch(method)
			{
				case "error":
					return "{\"error\": {\"message\": \"Unitpay отменил операцию\"}}";
				case "check":
					if(userRepository.findOne(account)!=null)
						return "{\"result\": {\"message\": \"Проверка успешно выполнена\"}}";
					return "{\"error\": {\"message\": \"Пользователь не найден\"}}";
				case "pay":
					count += countBonus(count);
					UserItemLog itemLog = userItemsService.addUserItem(account, UserItemType.MONEY.getName(), UserItemType.MONEY.getId(), count, "logging.donate", count + " " + UserItemType.MONEY.getName());
					countPrizepool(orderSum);
					if(itemLog!=null)
					{
						userIpService.save(new UserIp(account, "", Calendar.getInstance().getTimeInMillis(), itemLog.getId(), true, ""));
						return "{\"result\": {\"message\": \"Оплата успешно произведена\"}}";
					}
					return "{\"error\": {\"message\": \"Оплата произведена\"}}";
				default:
					return "{\"error\": {\"message\": \"Неизвестный запрос\"}}";
			}
		}
		catch(Exception e){
			return "{\"error\": {\"message\": \"Не удалось выполнить обработку в БД\"}}";
		}
	}

	@RequestMapping(value = "/success", method = { RequestMethod.GET })
	public String success(ModelMap model, Locale locale, String account) {
		model.addAttribute("messagetitle", localizedMessage.getMessage("message.donate_success_title", locale));
		model.addAttribute("messagetext", localizedMessage.getMessage("message.donate_success_message", locale));
		return LocalizePath.build(locale, "cp/$$/message");
	}

	@RequestMapping(value = "/failed", method = { RequestMethod.GET })
	public String failed(ModelMap model, Locale locale, String account) {
		model.addAttribute("messagetitle", localizedMessage.getMessage("message.donate_failed_title", locale));
		model.addAttribute("messagetext", localizedMessage.getMessage("message.donate_failed_message", locale));
		return LocalizePath.build(locale, "cp/$$/message");
	}

	private static String md5(String st)
	{
		MessageDigest messageDigest;
		byte[] digest = new byte[0];
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(st.getBytes());
			digest = messageDigest.digest();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		BigInteger bigInt = new BigInteger(1, digest);
		String md5Hex = bigInt.toString(16);
		while(md5Hex.length() < 32)
		{
			md5Hex = "0" + md5Hex;
		}
		return md5Hex;
	}

	private static String sha256(String st)
	{
		MessageDigest messageDigest;
		byte[] digest = new byte[0];
		try
		{
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.reset();
			messageDigest.update(st.getBytes());
			digest = messageDigest.digest();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		BigInteger bigInt = new BigInteger(1, digest);
		String sha256Hex = bigInt.toString(16);
		while(sha256Hex.length() < 32)
		{
			sha256Hex = "0" + sha256Hex;
		}
		return sha256Hex;
	}


	public void countPrizepool(double sum)
	{
		long percent = blockService.getVar("pool_donate");
		if(percent < 1)
			return;
		long value = (long)(sum * percent / 100);
		if(value < 1)
			return;
		blockService.setVar("pool_amount", blockService.getVar("pool_amount") + value);
	}


	public long countBonus(long sum)
	{
		if (sum >= 300 && sum < 1000)
			return Math.round(sum * 3 / 100);
		else if (sum >= 1000 && sum < 2000)
			return Math.round(sum * 5 / 100);
		else if (sum >= 2000 && sum < 3500)
			return Math.round(sum * 10 / 100);
		else if (sum >= 3500 && sum < 7000)
			return Math.round(sum * 15 / 100);
		else if (sum >= 7000)
			return Math.round(sum * 20 / 100);
		else return 0;
	}
}