package eu.socialsensor.framework.abstractions.tumblr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.List;

import org.apache.log4j.Logger;

import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.Video;
import com.tumblr.jumblr.types.VideoPost;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.WebPage;

/**
 * Class that holds the information regarding the tumblr post
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrItem extends Item{
	private Logger logger = Logger.getLogger(TumblrItem.class);
	
	public TumblrItem(String id, Operation operation) {
		super(Source.Type.Tumblr.toString(), operation);
		setId(Source.Type.Tumblr + "#" + id);
	}

	public TumblrItem(Post post) throws MalformedURLException{
		
		super(Source.Type.Tumblr.toString(), Operation.NEW);
		
		if(post == null || post.getId() == null)
			return;
		
		//Id
		id = Source.Type.Tumblr + "#" + post.getId();
		//SocialNetwork Name
		streamId = Source.Type.Tumblr.toString();
		//Timestamp of the creation of the post
		publicationTime = post.getTimestamp()*1000;
		//Tags
		tags = new String[post.getTags().size()];
		int i=0;
		for(String tag : post.getTags()){
			tags[i++] = tag;
		}
		//User that made the post
		streamUser = new TumblrStreamUser(post.getBlogName());
		uid = streamUser.getId();
		
		//Media Items - WebPages in a post
		String pageURL = post.getPostUrl();
		
		int number = 0;
		if(post.getType().equals("photo")){
			PhotoPost phPost;
			phPost = (PhotoPost) post;
		
			List<Photo> photos = phPost.getPhotos();
			if(photos == null)
				return;
			try{
				for(Photo photo : photos){
					number++;
					
					List<PhotoSize> allSizes = photo.getSizes();
					String photoUrl = allSizes.get(0).getUrl();
					String thumbnail = allSizes.get(allSizes.size()-1).getUrl();
					
					if(photoUrl != null){
						
						URL url = new URL(photoUrl);
						
						MediaItem mediaItem = new MediaItem(url);
					
						String mediaId = Source.Type.Tumblr + "#"+post.getId()+"_"+number; 
						
						mediaItem.setId(mediaId);
						mediaItem.setThumbnail(thumbnail);
						mediaItem.setType("image");
						mediaItem.setPageUrl(pageURL);
						mediaItem.setRef(id);

						mediaIds.add(mediaId);
						mediaItems.add(mediaItem);
					
					}
				}
			}catch (MalformedURLException e1) {
				logger.error("Photo URL is distorted : "+e1);
			}catch (Exception e2){
				logger.error("Exception : "+e2);
			}
		}
		else if(post.getType().equals("video")){
			VideoPost vidPost = (VideoPost) post;
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
			number++;
		
			MediaItem mediaItem = new MediaItem(url);

			String mediaId = Source.Type.Tumblr + "#"+post.getId()+"_"+number; 
			
			mediaItem.setId(mediaId);
			mediaItem.setType("video");
			mediaItem.setPageUrl(pageURL);
			mediaItem.setRef(mediaId);
			mediaItem.setThumbnail(videoThumbnail);
			mediaItems.add(mediaItem);
			mediaIds.add(mediaId);	
		
		}
		else if(post.getType().equals("link")) {		
    		
    		webPages = new ArrayList<WebPage>();
    		LinkPost linkPost = (LinkPost) post;
    		String link = linkPost.getLinkUrl();
			if (link != null) {
				WebPage webPage = new WebPage(link, id);
				webPage.setStreamId(streamId);
				webPages.add(webPage);
			}
    	}
		
		
		
	}
	
	public TumblrItem(Post post,Feed itemFeed) throws MalformedURLException{
		this(post);
		
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
		
	}
}
