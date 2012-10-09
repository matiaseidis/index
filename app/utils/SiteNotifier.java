package utils;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.ManyToOne;

import models.User;
import models.Video;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

public class SiteNotifier {

	private static final String SITE_URL = Play.configuration
			.getProperty("site.services.url.base");
	private static final String SITE_NEW_USER_SERVICE = SITE_URL
			+ Play.configuration.getProperty("site.service.new.user");
	private static final String SITE_NEW_VIDEO_SERVICE = SITE_URL
			+ Play.configuration.getProperty("site.service.new.video");

	public void notifyNewUser(User user) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("email", user.email);
		params.put("servlePort", Integer.toString(user.servlePort));
		params.put("name", user.name);

		WSRequest request = WS.url(SITE_NEW_USER_SERVICE);
		request.setParameters(params);
		HttpResponse response = request.post();
		if (response.getStatus() == 200) {
			play.Logger.info("new user %s - %s notified to site", user.name,
					user.email);
		} else {
			// TODO queue for retry
			play.Logger.info("unable to notify new user %s - %s to site",
					user.name, user.email);
		}

	}

	public void notifyNewVideo(Video video) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("videoId", video.videoId);
		params.put("fileName", video.fileName);
		params.put("sharedByEmail", video.addedBy.email);

		WSRequest request = WS.url(SITE_NEW_VIDEO_SERVICE);
		request.setParameters(params);
		HttpResponse response = request.post();
		if (response.getStatus() == 200) {
			play.Logger.info("new video %s - %s notified to site",
					video.videoId, video.fileName);
		} else {
			// TODO queue for retry
			play.Logger.info("unable to notify new video %s - %s to site",
					video.videoId, video.fileName);
		}

	}

}
