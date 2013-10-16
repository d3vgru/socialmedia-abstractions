package eu.socialsensor.framework.retrievers.youtube;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;

import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.retrievers.MediaRetriever;

/**
 * The retriever that implements the Youtube simplified retriever
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class YtMediaRetriever implements MediaRetriever {

	private static String entryUrlPrefix = "http://gdata.youtube.com/feeds/api/videos/";
	
	private YouTubeService service;
	
	public YtMediaRetriever(String clientId, String developerKey) {
		this.service = new YouTubeService(clientId, developerKey);
	}
	
	public MediaItem getMediaItem(String id) {
		URL entryUrl;
		try {
			entryUrl = new URL(entryUrlPrefix + id);
			VideoEntry entry = service.getEntry(entryUrl, VideoEntry.class);
			if(entry != null) {
				YouTubeMediaGroup mediaGroup = entry.getMediaGroup();
				List<YouTubeMediaContent> mediaContent = mediaGroup.getYouTubeContents();
				List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
				
				String videoURL = null;
				for(YouTubeMediaContent content : mediaContent) {
					if(content.getType().equals("application/x-shockwave-flash")) {
						videoURL = content.getUrl();
						break;
					}
				}
				
				if(videoURL != null) {
					MediaPlayer mediaPlayer = mediaGroup.getPlayer();
					YtStatistics statistics = entry.getStatistics();
					
					Long publicationTime = entry.getPublished().getValue();
					
					String mediaId = "Youtube::" + mediaGroup.getVideoId();
					URL url = new URL(videoURL);
				
					String title = mediaGroup.getTitle().getPlainTextContent();
		
					MediaDescription desc = mediaGroup.getDescription();
					String description = desc==null ? "" : desc.getPlainTextContent();
					
					MediaItem mediaItem = new MediaItem(url);
					mediaItem.setId(mediaId);
					mediaItem.setTitle(title);
					mediaItem.setDescription(description);
					mediaItem.setPublicationTime(publicationTime);
					
					mediaItem.setType("video");
					mediaItem.setStreamId("Youtube");
					
					// Set popularity
					Map<String, Integer> popularity = new HashMap<String, Integer>();
					if(statistics!=null){
						Long views = statistics.getViewCount();
						if(views!=null)
							popularity.put("views", views.intValue());
						Long favorites = statistics.getFavoriteCount();
						if(favorites!=null)
							popularity.put("favorites", favorites.intValue());
					}
					Rating rating = entry.getRating();
					if(rating != null) {
						Integer ratings = rating.getNumRaters();
						popularity.put("ratings", ratings);
						Float avg = rating.getAverage();
						popularity.put("avgRating", avg.intValue());
					}
					mediaItem.setPopularity(popularity);
					
					// Set thumbnail
					MediaThumbnail thumb = null;
					int size = 0;
					for(MediaThumbnail thumbnail : thumbnails) {
						int t_size = thumbnail.getHeight() * thumbnail.getWidth();
						if(t_size > size) {
							thumb = thumbnail;
							size = t_size;
						}
					}
					
					mediaItem.setSize(thumb.getWidth(), thumb.getHeight());
					mediaItem.setThumbnail(thumb.getUrl());
					
					mediaItem.setPageUrl(mediaPlayer.getUrl());
					
					return mediaItem;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} 
//		catch (IOException e) {
//			//e.printStackTrace();
//		} catch (ServiceException e) {
//			//e.printStackTrace();
//		}
	
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		YtMediaRetriever retriever = new YtMediaRetriever("manosetro", "AI39si6DMfJRhrIFvJRv0qFubHHQypIwjkD-W7tsjLJArVKn9iR-QoT8t-UijtITl4TuyHzK-cxqDDCkCBoJB-seakq1gbt1iQ");

		MediaItem mediaItem = retriever.getMediaItem("1h9j0Hk3Xdg");
		System.out.println(mediaItem.toJSONString());
	}
}
