package eu.socialsensor.framework.abstractions.vimeo;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.socialsensor.framework.common.domain.MediaItem;

/**
 * Class that holds the information regarding the vimeo media item
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class VimeoMediaItem extends MediaItem {
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public VimeoMediaItem(VimeoVideo video) throws Exception {
		super(new URL("http://vimeo.com/moogaloop.swf?clip_id="+video.id));
		
		this.setId("Vimeo::"+video.id);
		this.setTitle(video.title);
		
		String tags = video.tags;
		if(tags != null) {
			this.setTags(tags.split(","));
		}
		this.setDescription(video.description);
		this.setPageUrl(video.url);
		this.setThumbnail(video.thumbnail_large);
		this.setSize(video.width, video.height);
		
		this.setStreamId("Vimeo");
		this.setType("video");
		
		Map<String, Integer> popularity = new HashMap<String, Integer>();
		popularity.put("comments", video.stats_number_of_comments);
		popularity.put("likes", video.stats_number_of_likes);
		popularity.put("views", video.stats_number_of_plays);		
	
		this.setPopularity(popularity);
		
		try {
			Date date = formatter.parse(video.upload_date);
			this.setPublicationTime(date.getTime());
		}
		catch(Exception e) {}
	}
}
