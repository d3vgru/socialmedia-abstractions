package eu.socialsensor.framework.retrievers.youtube;

import java.net.URL;
import java.util.List;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;

import eu.socialsensor.framework.abstractions.youtube.YoutubeStreamUser;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.retrievers.MediaRetriever;

/**
 * The retriever that implements the Youtube simplified retriever
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class YtMediaRetriever implements MediaRetriever {
	private final String activityFeedUserUrlPrefix = "http://gdata.youtube.com/feeds/api/users/";
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
				
				if(videoURL == null) {
					videoURL = "https://youtube.googleapis.com/v/"+id;
				}
				
				if(videoURL != null) {
					MediaPlayer mediaPlayer = mediaGroup.getPlayer();
					YtStatistics statistics = entry.getStatistics();
					
					Long publicationTime = entry.getPublished().getValue();
					
					String mediaId = "Youtube#" + mediaGroup.getVideoId();
					URL url = new URL(videoURL);
				
					String title = mediaGroup.getTitle().getPlainTextContent();
		
					MediaDescription desc = mediaGroup.getDescription();
					String description = desc==null ? "" : desc.getPlainTextContent();
					
					//url
					MediaItem mediaItem = new MediaItem(url);
					//description
					mediaItem.setDescription(description);
					//id
					mediaItem.setId(mediaId);
					//SocialNetwork Name
					mediaItem.setStreamId("Youtube");
					//Type 
					mediaItem.setType("video");
					//Time of publication
					mediaItem.setPublicationTime(publicationTime);
					//PageUrl
					String pageUrl = mediaPlayer.getUrl();
					mediaItem.setPageUrl(pageUrl);
					//Thumbnail
					MediaThumbnail thumb = null;
					int size = 0;
					for(MediaThumbnail thumbnail : thumbnails) {
						int t_size = thumbnail.getHeight() * thumbnail.getWidth();
						if(t_size > size) {
							thumb = thumbnail;
							size = t_size;
						}
					}
					//Title
					mediaItem.setTitle(title);
					//Popularity
					if(statistics!=null){
						mediaItem.setLikes(statistics.getFavoriteCount());
						mediaItem.setViews(statistics.getViewCount());
					}
					Rating rating = entry.getRating();
					if(rating != null) {
						mediaItem.setRatings(rating.getAverage());
					}
					//Size
					if(thumb!=null) {
						mediaItem.setSize(thumb.getWidth(), thumb.getHeight());
						mediaItem.setThumbnail(thumb.getUrl());
					}
					else {
						String thumbnail = "http://i.ytimg.com/vi/" + mediaGroup.getVideoId() + "/hqdefault.jpg";
						mediaItem.setThumbnail(thumbnail);
					}
					
					String uploader = mediaGroup.getUploader();
					UserProfileEntry userProfile = retrieveUser(uploader);
					if(userProfile != null) {
						YoutubeStreamUser user = new YoutubeStreamUser(userProfile);
						mediaItem.setUser(user);
						mediaItem.setUserId(user.getId());
					}
					return mediaItem;
				}
			}
			else {
				System.out.println("Extry is null");
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
	
	public UserProfileEntry retrieveUser(String channel) throws Exception {
		URL profileUrl = new URL(activityFeedUserUrlPrefix + channel);
		UserProfileEntry userProfile = service.getEntry(profileUrl , UserProfileEntry.class);
		
		return userProfile;
	}
}
