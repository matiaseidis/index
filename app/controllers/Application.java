package controllers;

import java.util.List;

import models.Video;
import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
    	List<Video> videos = Video.all().fetch();
        render(videos);
    }

}