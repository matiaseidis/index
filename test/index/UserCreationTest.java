package index;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import models.User;

import org.junit.Test;

import play.Play;
import play.libs.WS.HttpResponse;
import play.mvc.Http.Response;

import com.google.gson.JsonObject;


public class UserCreationTest extends BaseFunctionalTest {
	
	String userName = "testUserName";
	String email = userName+"@cachos.com";
	String ip = "173.194.42.3";
	int servlePort = 1234;
	int dimonPort = 10002;
	
	@Test
	public void testUserCreation(){
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", userName);
		params.put("email", email);
		params.put("ip", ip);
		params.put("servlePort", Integer.toString(servlePort));
		params.put("dimonPort", Integer.toString(dimonPort));
		
		
		Response response = callService("/userService/create", params);

		Assert.assertTrue( User.count() == 1);
		
		JsonObject jsonObject = codeOk(response);
		
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("email").getAsString().equals(email));
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("name").getAsString().equals(userName));
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("ip").getAsString().equals(ip));
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("servlePort").getAsInt() == servlePort);
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("dimonPort").getAsInt() == dimonPort);
		
		Map<String, String> siteParams = new HashMap<String, String>();
		siteParams.put("email", email);
		siteParams.put("servlePort", Integer.toString(servlePort));
		siteParams.put("name", userName);
		String newUserServiceUrl = Play.configuration.getProperty("site.service.new.user");
		HttpResponse siteResponse = callSiteService(newUserServiceUrl, siteParams);
		Assert.assertTrue(siteResponse.getJson().getAsJsonObject().get("code").getAsString().equalsIgnoreCase("ok"));
		
		String checkUserServiceUrl = Play.configuration.getProperty("site.service.get.user");
		
		siteResponse = callSiteService(checkUserServiceUrl, siteParams);
		String code = siteResponse.getJson().getAsJsonObject().get("code").getAsString();
		Assert.assertTrue("Code not ok", code.equalsIgnoreCase("ok"));
		
		
	}
}
