package eu.socialsensor.framework.abstractions.youtube;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gdata.data.Category;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.extensions.Rating;
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
import eu.socialsensor.framework.common.domain.MediaItemLight;
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
		setId(Source.Type.Youtube+"::"+id);
	}
	
	public YoutubeItem(VideoEntry videoEntry) {
		super(Source.Type.Youtube.toString(), Operation.NEW_UPDATE);
		
		if (videoEntry == null || videoEntry.getId() == null) 
			return;
		
		String videoID = videoEntry.getId().substring(videoEntry.getId().indexOf("video:")+("video:").length());
		
		YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
		
		List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
		Set<URL> mlinks = new HashSet<URL>();
		mediaLinks = new ArrayList<MediaItemLight>();
		MediaPlayer mediaPlayer = mediaGroup.getPlayer();
		
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
		
		id = Source.Type.Youtube+"::"+mediaGroup.getVideoId();
		source = "";
		
		List<String> cList = new ArrayList<String>();
		for(Category category : videoEntry.getCategories()) {
			String label = category.getLabel();
			if(label!= null) {
				cList.add(label);
			}
		}
		categories = cList.toArray(new String[cList.size()]);
		
		List<Person> authors = videoEntry.getAuthors();
		if(authors.size()>0) {
			streamUser = new YoutubeStreamUser(authors.get(0));
		}
		else{
			streamUser = new YoutubeStreamUser(mediaGroup.getUploader());
		}
		author = streamUser.getUserId();
		uid = streamUser.getId();
		
		
		
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
		
		if(thumbnail != null && !thumbnail.getUrl().contains("sddefault") && videoID != null) {
			MediaItem mediaItem = new MediaItem(url);
			
			String mediaId = Source.Type.Youtube + "::"+videoID; 
			
			String pageUrl = mediaPlayer.getUrl();
			String thumbUrl = thumbnail.getUrl();
			mediaItem.setId(mediaId);
			mediaItem.setType("video");
			mediaItem.setSize(thumbnail.getWidth(), thumbnail.getHeight());
			mediaItem.setPageUrl(pageUrl);
			mediaItem.setThumbnail(thumbUrl);
			
			mlinks.add(url);		
			mediaItems.put(url, mediaItem);	
			MediaItemLight mediaLink = new MediaItemLight(videoURL, thumbUrl);
			mediaLinks.add(mediaLink);
			mediaIds.add(mediaId);
		}
		
		links = new URL[0];
		
		DateTime pubDate = mediaGroup.getUploaded();
		publicationTime = pubDate.getValue();

		title = mediaGroup.getTitle().getPlainTextContent();
		
		MediaDescription desc = mediaGroup.getDescription();
		description = desc==null ? "" : desc.getPlainTextContent();
		
		// Set popularity
		popularity = new HashMap<String, Integer>();
		YtStatistics statistics = videoEntry.getStatistics();
		if(statistics!=null){
			Long views = statistics.getViewCount();
			if(views!=null)
				popularity.put("views", views.intValue());
			Long favorites = statistics.getFavoriteCount();
			if(favorites!=null)
				popularity.put("favorites", favorites.intValue());
		}
		Rating rating = videoEntry.getRating();
		if(rating != null) {
			Integer ratings = rating.getNumRaters();
			popularity.put("ratings", ratings);
			Float avg = rating.getAverage();
			popularity.put("avgRating", avg.intValue());
		}
		
	}
	
	
	public YoutubeItem(VideoEntry videoEntry, Feed itemFeed) {
		super(Source.Type.Youtube.toString(), Operation.NEW_UPDATE);
		
		if (videoEntry == null) 
			return;
		
		String videoID = videoEntry.getId().substring(videoEntry.getId().indexOf("video:")+("video:").length());
		
		YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
		
		List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
		Set<URL> mlinks = new HashSet<URL>();
		mediaLinks = new ArrayList<MediaItemLight>();
		MediaPlayer mediaPlayer = mediaGroup.getPlayer();
		
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
		
		id = Source.Type.Youtube+"::"+mediaGroup.getVideoId();
		source = "";
		
		List<String> cList = new ArrayList<String>();
		for(Category category : videoEntry.getCategories()) {
			String label = category.getLabel();
			if(label!= null) {
				cList.add(label);
			}
		}
		categories = cList.toArray(new String[cList.size()]);
		
		List<Person> authors = videoEntry.getAuthors();
		if(authors.size()>0) {
			streamUser = new YoutubeStreamUser(authors.get(0));
		}
		else{
			streamUser = new YoutubeStreamUser(mediaGroup.getUploader());
		}
		author = streamUser.getUserId();
		uid = streamUser.getId();
		
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
		
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
			
			String mediaId = Source.Type.Youtube + "::"+videoID; 
			
			String pageUrl = mediaPlayer.getUrl();
			String thumbUrl = thumbnail.getUrl();
			mediaItem.setId(mediaId);
			mediaItem.setType("video");
			mediaItem.setSize(thumbnail.getWidth(), thumbnail.getHeight());
			mediaItem.setPageUrl(pageUrl);
			mediaItem.setThumbnail(thumbUrl);
			mediaItem.setDyscoId(feed.getDyscoId());
			mlinks.add(url);		
			mediaItems.put(url, mediaItem);	
			MediaItemLight mediaLink = new MediaItemLight(videoURL, thumbUrl);
			mediaLinks.add(mediaLink);
			mediaIds.add(mediaId);
		}
		
		links = new URL[0];
		
		DateTime pubDate = mediaGroup.getUploaded();
		publicationTime = pubDate.getValue();

		title = mediaGroup.getTitle().getPlainTextContent();
		
		MediaDescription desc = mediaGroup.getDescription();
		description = desc==null ? "" : desc.getPlainTextContent();
		
		// Set popularity
		popularity = new HashMap<String, Integer>();
		YtStatistics statistics = videoEntry.getStatistics();
		if(statistics!=null){
			Long views = statistics.getViewCount();
			if(views!=null)
				popularity.put("views", views.intValue());
			Long favorites = statistics.getFavoriteCount();
			if(favorites!=null)
				popularity.put("favorites", favorites.intValue());
		}
		Rating rating = videoEntry.getRating();
		if(rating != null) {
			Integer ratings = rating.getNumRaters();
			popularity.put("ratings", ratings);
			Float avg = rating.getAverage();
			popularity.put("avgRating", avg.intValue());
		}
		
	}
	
	
}
