import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class UserCreationTest extends BaseFunctionalTest {
	
	String userName = "testUserName";
	String email = userName+"@cachos.com";
	String ip = "173.194.42.3";
	int port = 1234;
	
	@Test
	public void testUserCreation(){
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", userName);
		params.put("email", email);
		params.put("ip", ip);
		params.put("port", Integer.toString(port));
		
		
		Response response = callService("/userService/create", params);

		Assert.assertTrue( User.count() == 1);
		
		JsonObject jsonObject = codeOk(response);
		
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("email").getAsString().equals(email));
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("name").getAsString().equals(userName));
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("ip").getAsString().equals(ip));
		Assert.assertTrue(jsonObject.get("body").getAsJsonObject().get("port").getAsInt() == port);
		
	}
}
