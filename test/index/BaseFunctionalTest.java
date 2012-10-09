package index;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.After;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Ignore
public class BaseFunctionalTest extends FunctionalTest {
	
	public static final String CHUNK_SEPARATOR = Play.configuration.getProperty("chunk.separator");
	public static final String CHUNK_FOR_REGISTER_SEPARATOR = Play.configuration.getProperty("chunk.registration.separator");
	
	@Before
    public void setUp() {
        Fixtures.deleteDatabase();
    }
	
	@After
    public void cleanUp() {
        Fixtures.deleteDatabase();
    }

	protected Response callService(String url, Map<String, String> params) {

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");

		Response response = POST(url, params);
		assertIsOk(response);
		assertContentType("application/json", response);
		assertCharset("utf-8", response);

		return response;
	}
	
	protected HttpResponse callSiteService(String serviceUrl, Map<String, String> params) {

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		
		String siteUrlBase = Play.configuration.getProperty("site.services.url.base");
		
		HttpResponse response = WS.url(siteUrlBase+serviceUrl).setParameters(params).post();
		
//		Response response = POST(url, params);
		
		int status = response.getStatus();
		assertTrue("status: "+status, status == 200);
		assertEquals("application/json; charset=utf-8", response.getContentType());

		return response;
	}

	protected JsonObject codeOk(Response response) {

		JsonElement jsonResponse = new JsonParser().parse(response.out.toString());
		JsonObject jsonObject = jsonResponse.getAsJsonObject();
		
		String error = "Response code not OK - ";
		if(!jsonObject.get("code").getAsString().equals("OK")){
			error+=jsonObject.get("body").getAsString();
		}
		Assert.assertTrue(error, jsonObject.get("code").getAsString().equals("OK"));
		return jsonObject;
	}

}
