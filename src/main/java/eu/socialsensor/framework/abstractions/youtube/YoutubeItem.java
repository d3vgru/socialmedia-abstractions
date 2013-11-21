package eu.socialsensor.framework.abstractions.youtube;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gdata.data.Person;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Source;

/**
 * Class that holds the information regarding the youtube video
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class YoutubeItem extends Item {

	private Logger logger = Logger.getLogger(YoutubeItem.class);
	
	public YoutubeItem(String id, Operation operation) {
		super(Source.Type.Youtube.toString(), operation);
		setId(Source.Type.Youtube+"#"+id);
	}
	
	public YoutubeItem(VideoEntry videoEntry) {
		super(Source.Type.Youtube.toString(), Operation.NEW);
		
		if (videoEntry == null || videoEntry.getId() == null) 
			return;
		
		YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
		//Id
		id = Source.Type.Youtube+"#"+mediaGroup.getVideoId();
		//SocialNetwork Name
		streamId = Source.Type.Youtube.toString();
		//Timestamp of the creation of the video
		publicationTime = mediaGroup.getUploaded().getValue();
		//Title of the video
		title = mediaGroup.getTitle().getPlainTextContent();
		//Description of the video
		MediaDescription desc = mediaGroup.getDescription();
		description = desc==null ? "" : desc.getPlainTextContent();
		//User that uploaded the video
		List<Person> authors = videoEntry.getAuthors();
		if(authors.size()>0) {
			streamUser = new YoutubeStreamUser(authors.get(0));
		}
		else{
			if(mediaGroup.getUploader()!=null){
				streamUser = new YoutubeStreamUser(mediaGroup.getUploader());
			}
		}
		uid = streamUser.getId();
		//Popularity
		YtStatistics statistics = videoEntry.getStatistics();
		if(statistics!=null){
			likes = (int) statistics.getFavoriteCount();
			
		}
		
//		Rating rating = videoEntry.getRating();
//		if(rating != null) {
//			Integer ratings = rating.getNumRaters();
//			popularity.put("ratings", ratings);
//			Float avg = rating.getAverage();
//			popularity.put("avgRating", avg.intValue());
//		}
//		
		//Getting the video
		List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
		MediaPlayer mediaPlayer = mediaGroup.getPlayer();
		
		String videoID = videoEntry.getId().substring(videoEntry.getId().indexOf("video:")+("video:").length());
		List<YouTubeMediaContent> mediaContent = mediaGroup.getYouTubeContents();
		
		String videoURL = null;
		for(YouTubeMediaContent content : mediaContent){
			if(content.getType().equals("application/x-shockwave-flash")) {
				videoURL = content.getUrl();
				break;
			}
		}	
		if (videoURL == null) 
			videoURL = mediaPlayer.getUrl();
		
		URL url = null;
		try {
			url = new URL(videoURL);
		} catch (MalformedURLException e1) {
			logger.error("Video URL is distorted");
		}
		
		int size = 0;
		MediaThumbnail thumbnail = null;
		for(MediaThumbnail thumb : thumbnails) {
			int t_size = thumb.getWidth()*thumb.getHeight();
			if(size < t_size) {
				size = t_size;
				thumbnail = thumb; 
			}
		}
		
		if(thumbnail != null) {
			MediaItem mediaItem = new MediaItem(url);
			
			String mediaId = Source.Type.Youtube + "#"+videoID; 
			String pageUrl = mediaPlayer.getUrl();
			String thumbUrl = thumbnail.getUrl();
			mediaItem.setId(mediaId);
			mediaItem.setType("video");
			mediaItem.setSize(thumbnail.getWidth(), thumbnail.getHeight());
			mediaItem.setPageUrl(pageUrl);
			mediaItem.setThumbnail(thumbUrl);	
			mediaItems.add(mediaItem);
			mediaIds.add(mediaId);
		}

	}
	
	
	public YoutubeItem(VideoEntry videoEntry, Feed itemFeed) {
		
		this(videoEntry);
		
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
	
	}
	
	
}
