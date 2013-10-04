package eu.socialsensor.framework.abstractions.twitpic;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.socialsensor.framework.common.domain.MediaItem;

/**
 * Class that holds the information regarding the twitpic media item
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitPicMediaItem extends MediaItem {

	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static String urlBase = "http://d3j5vwomefv46c.cloudfront.net/photos/large/";
	private static String thumbBase = "http://d3j5vwomefv46c.cloudfront.net/photos/thumb/";
	private static String pageBase = "http://twitpic.com/";
	
	public TwitPicMediaItem(TwitPicImage image) throws Exception {
		super(new URL(urlBase + image.id + "." + image.type));
		
		this.setId("Twitpic::"+image.id);
		this.setThumbnail(thumbBase + image.id + "." + image.type);
		this.setPageUrl(pageBase + image.short_id);
		
		this.setType("image");
		this.setStreamId("Twitpic");
		
		this.setTitle(image.message);
		if(image.tags != null) {
			this.setTags(image.tags.split(","));
		}
		
		try {
			Date date = formatter.parse(image.timestamp);
			this.setPublicationTime(date.getTime());
		}
		catch(Exception e){}
		
		Map<String, Integer> popularity = new HashMap<String, Integer>();
		popularity.put("comments", image.number_of_comments);
		popularity.put("views", image.views);
		this.setPopularity(popularity);
		
		this.setSize(image.width, image.height);
		
	}

	
}
