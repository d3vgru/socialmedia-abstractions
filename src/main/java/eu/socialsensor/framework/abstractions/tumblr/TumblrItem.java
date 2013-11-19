package eu.socialsensor.framework.abstractions.tumblr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.Video;
import com.tumblr.jumblr.types.VideoPost;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.MediaItemLight;
import eu.socialsensor.framework.common.domain.Source;

/**
 * Class that holds the information regarding the tumblr post
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrItem extends Item{
	private Logger logger = Logger.getLogger(TumblrItem.class);
	
	public TumblrItem(String id, Operation operation) {
		super(Source.Type.Tumblr.toString(), operation);
		setId(Source.Type.Tumblr + "::" + id);
	}

	public TumblrItem(Post post) throws MalformedURLException{
		
		super(Source.Type.Tumblr.toString(), Operation.NEW);
		
		if(post == null || post.getId() == null)
			return;
		
		id = Source.Type.Tumblr + "::" + post.getId();
		
		source = "Tumblr";
		
		publicationTime = post.getTimestamp()*1000;
		
		tags = new String[post.getTags().size()];
		int i=0;
	
		for(String tag : post.getTags()){
			tags[i] = tag;
			i++;
		}
		
		streamUser = new TumblrStreamUser(post.getBlogName());
		author = post.getBlogName();
		mediaLinks = new ArrayList<MediaItemLight>();
		mediaIds = new ArrayList<String>();
		mediaItems = new HashMap<URL,MediaItem>();
		Set<URL> mlinks = new HashSet<URL>();
		String pageURL = post.getPostUrl();
		
		int num = 0;
		
		if(post.getType().equals("photo")){
			PhotoPost phPost;
			phPost = (PhotoPost) post;
		
			List<Photo> photos = phPost.getPhotos();
			if(photos == null)
				return;
			try{
				
				for(Photo photo : photos){
					num++;
					List<PhotoSize> allSizes = photo.getSizes();
					String photoUrl = allSizes.get(0).getUrl();
					String thumbnail = allSizes.get(allSizes.size()-3).getUrl();
					
					if(photoUrl != null){
						
						URL url = new URL(photoUrl);
						
						MediaItem mediaItem = new MediaItem(url);
					
						String mediaId = Source.Type.Tumblr + "::"+post.getId()+"_"+num; 
						
						mediaItem.setId(mediaId);
						mediaItem.setThumbnail(thumbnail);
						mediaItem.setType("image");
						mediaItem.setPageUrl(pageURL);
						mediaItem.setRef(id);
						
						mediaIds.add(mediaId);
						mlinks.add(url);	
						mediaItems.put(url, mediaItem);	
						
						MediaItemLight mediaLink = new MediaItemLight(photoUrl, thumbnail);	
						mediaLinks.add(mediaLink);		
						
					}
				}
			}catch (MalformedURLException e1) {
				logger.error("Photo URL is distorted : "+e1);
			}catch (Exception e2){
				logger.error("Exception : "+e2);
			}
		}
		else if(post.getType().equals("video")){
			VideoPost vidPost;
			vidPost = (VideoPost) post;
			List<Video> videos = vidPost.getVideos();
	
			String embedCode = videos.get(0).getEmbedCode();
		
			String postfix = "";
			String prefix = "src=";
			String compl = "";
			String prefix_id = "embed/";
			String postfix_id = "?";
			
			int index;
			int startIndex_id = embedCode.indexOf(prefix_id);
			
			String videoIdUrl ;
			String videoThumbnail;
			String videoUrl = null;
			
			if(embedCode.contains("youtube")){
				postfix = "frameborder";
				index = embedCode.lastIndexOf(prefix);
				videoIdUrl = embedCode.substring(startIndex_id+prefix_id.length(),embedCode.indexOf(postfix_id));
				videoUrl = embedCode.substring(index+prefix.length(), embedCode.indexOf(postfix));
				videoUrl = videoUrl.substring(1, videoUrl.length()-1);
				
				System.out.println("VideoIdUrl : "+videoIdUrl);
				videoThumbnail = "http://img.youtube.com/vi/"+videoIdUrl+"/0.jpg";
				System.out.println("videoThumbnail : "+videoThumbnail);
			}
			else if(embedCode.contains("dailymotion")){
				postfix ="width";
				index = embedCode.lastIndexOf(prefix);
				videoUrl = embedCode.substring(index+prefix.length(), embedCode.indexOf(postfix));
				videoUrl = videoUrl.substring(1, videoUrl.length()-1);
				
				StringBuffer str = new StringBuffer(videoUrl);
				String thumb = "thumbnail";
				
				str.insert(videoUrl.indexOf("/video/"),thumb);
				
				videoThumbnail = str.toString();
				System.out.println("videoThumbnail : "+videoThumbnail);
			}
			else{
				return;
			}
		
			if(videoUrl == null)
				return;
		
			URL url = null;
			try {
				url = new URL(videoUrl);
			} catch (MalformedURLException e1) {
				logger.error("Video URL is distorted : "+e1);
			}
			num++;
			
			MediaItem mediaItem = new MediaItem(url);
			
			String mediaId = Source.Type.Tumblr + "::"+post.getId()+"_"+num; 
			
			mediaItem.setId(mediaId);
			mediaItem.setType("video");
			mediaItem.setPageUrl(pageURL);
			mediaItem.setRef(mediaId);
			mediaItem.setDyscoId(feed.getDyscoId());
			mediaItem.setThumbnail(videoThumbnail);
			mlinks.add(url);	
			mediaItems.put(url, mediaItem);	
			MediaItemLight mediaLink = new MediaItemLight(videoUrl, null);				
			mediaLinks.add(mediaLink);			
			mediaIds.add(mediaId);	
		
		}
		
		popularity = new HashMap<String,Integer>();
		popularity.put("shares", 0);
	}
	
	public TumblrItem(Post post,Feed itemFeed) throws MalformedURLException{
		
		super(Source.Type.Tumblr.toString(), Operation.NEW);
		
		if(post == null || post.getId() == null)
			return;
		
		id = Source.Type.Tumblr + "::" + post.getId();
		
		source = "Tumblr";
		
		publicationTime = post.getTimestamp()*1000;
		
		tags = new String[post.getTags().size()];
		int i=0;
	
		for(String tag : post.getTags()){
			tags[i] = tag;
			i++;
		}
		
		streamUser = new TumblrStreamUser(post.getBlogName());
		author = post.getBlogName();
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
		
		mediaLinks = new ArrayList<MediaItemLight>();
		mediaIds = new ArrayList<String>();
		mediaItems = new HashMap<URL,MediaItem>();
		Set<URL> mlinks = new HashSet<URL>();
		String pageURL = post.getPostUrl();
		
		int num = 0;
		
		if(post.getType().equals("photo")){
			PhotoPost phPost;
			phPost = (PhotoPost) post;
		
			List<Photo> photos = phPost.getPhotos();
			if(photos == null)
				return;
			try{
				for(Photo photo : photos){
					num++;
					
					List<PhotoSize> allSizes = photo.getSizes();
					String photoUrl = allSizes.get(0).getUrl();
					String thumbnail = allSizes.get(allSizes.size()-1).getUrl();
					
					if(photoUrl != null){
						
						URL url = new URL(photoUrl);
						
						MediaItem mediaItem = new MediaItem(url);
					
						String mediaId = Source.Type.Tumblr + "::"+post.getId()+"_"+num; 
						
						mediaItem.setId(mediaId);
						mediaItem.setThumbnail(thumbnail);
						mediaItem.setType("image");
						mediaItem.setPageUrl(pageURL);
						mediaItem.setRef(id);
						
						mediaItem.setDyscoId(feed.getDyscoId());
						mediaIds.add(mediaId);
						mlinks.add(url);	
						mediaItems.put(url, mediaItem);	
						
						MediaItemLight mediaLink = new MediaItemLight(photoUrl, thumbnail);	
						mediaLinks.add(mediaLink);		
						
					}
				}
			}catch (MalformedURLException e1) {
				logger.error("Photo URL is distorted : "+e1);
			}catch (Exception e2){
				logger.error("Exception : "+e2);
			}
		}
		else if(post.getType().equals("video")){
			VideoPost vidPost;
			vidPost = (VideoPost) post;
			List<Video> videos = vidPost.getVideos();
		
			String embedCode = videos.get(0).getEmbedCode();
		
			String postfix = "";
			String prefix = "src=";
			String compl = "";
			String prefix_id = "embed/";
			String postfix_id = "?";
			
			int index;
			int startIndex_id = embedCode.indexOf(prefix_id);
			
			String videoIdUrl ;
			String videoThumbnail;
			String videoUrl = null;
			
			if(embedCode.contains("youtube")){
				postfix = "frameborder";
				index = embedCode.lastIndexOf(prefix);
				videoIdUrl = embedCode.substring(startIndex_id+prefix_id.length(),embedCode.indexOf(postfix_id));
				videoUrl = embedCode.substring(index+prefix.length(), embedCode.indexOf(postfix));
				videoUrl = videoUrl.substring(1, videoUrl.length()-1);
				
				
				videoThumbnail = "http://img.youtube.com/vi/"+videoIdUrl+"/0.jpg";
				
			}
			else if(embedCode.contains("dailymotion")){
				postfix ="width";
				index = embedCode.lastIndexOf(prefix);
				videoUrl = embedCode.substring(index+prefix.length(), embedCode.indexOf(postfix));
				videoUrl = videoUrl.substring(1, videoUrl.length()-1);
				
				StringBuffer str = new StringBuffer(videoUrl);
				String thumb = "thumbnail";
				
				str.insert(videoUrl.indexOf("/video/"),thumb);
				
				videoThumbnail = str.toString();
				
			}
			else{
				return;
			}
		
			if(videoUrl == null)
				return;
		
			URL url = null;
			try {
				url = new URL(videoUrl);
			} catch (MalformedURLException e1) {
				logger.error("Video URL is distorted : "+e1);
			}
			num++;
			
			MediaItem mediaItem = new MediaItem(url);

			String mediaId = Source.Type.Tumblr + "::"+post.getId()+"_"+num; 
			
			mediaItem.setId(mediaId);
			mediaItem.setType("video");
			mediaItem.setPageUrl(pageURL);
			mediaItem.setRef(mediaId);
			mediaItem.setDyscoId(feed.getDyscoId());
			mediaItem.setThumbnail(videoThumbnail);
			mlinks.add(url);	
			mediaItems.put(url, mediaItem);	
			MediaItemLight mediaLink = new MediaItemLight(videoUrl, null);				
			mediaLinks.add(mediaLink);			
			mediaIds.add(mediaId);	
		
		}

		popularity = new HashMap<String,Integer>();
		popularity.put("shares", 0);
	}
}
