package eu.socialsensor.framework.abstractions.facebook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.Place;
import com.restfb.types.Post;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.WebPage;

/**
 * Class that holds the information regarding the facebook post
 * @author ailiakop
 * @author ailiakop@iti.gr
 *
 */
public class FacebookItem extends Item {
	
	public FacebookItem(String id, Operation operation) {
		super(Source.Type.Facebook.toString(), operation);
		setId(Source.Type.Facebook+"#"+id);
	}
	
	public FacebookItem(Post post) {
		
		super(Source.Type.Facebook.toString(), Operation.NEW);
		
		if (post == null || post.getId() == null) return;
		
		//Id
		id = Source.Type.Facebook+"#"+post.getId();
		//SocialNetwork Name
		streamId = Source.Type.Facebook.toString();
		//Timestamp of the creation of the post
		publicationTime = post.getCreatedTime().getTime();
		//Message that post contains
		String msg = post.getMessage();
		if(msg!=null) {
			title = msg.subSequence(0, 100)+"...";
			description = post.getDescription();
		}
		//All the text inside the post
		text = msg; 
		//User that posted the post
		CategorizedFacebookType user = post.getFrom();
		if (user != null) {
			streamUser = new FacebookStreamUser(user);
			uid = streamUser.getId();
		}
		//Location 
		Place place = post.getPlace();
		if(place != null) {
			String placeName = place.getName();
			com.restfb.types.Location loc = place.getLocation();
			if(loc != null) {
				Double latitude = loc.getLatitude();
				Double longitude = loc.getLongitude();
		
				location = new Location(latitude, longitude, placeName);
			}
		}
		//Popularity of the post
		if(post.getLikesCount() != null)
			likes = post.getLikesCount().intValue();
		if(post.getSharesCount() != null)
			shares = post.getSharesCount().intValue();
		
		//Media Items - WebPages in a post
		
		String type = post.getType();
		
		if(type.equals("photo")) {
		
			String picture = post.getPicture();
			
			try {
				if (picture != null) { 
					URL p_url = null;
					StringBuilder b = new StringBuilder(picture);
					int index = picture.lastIndexOf("_s.");
					if(index>0) {
						b.replace(index, index+3, "_n." );
						picture = picture.replaceAll("_s.", "_n.");
						p_url = new URL(picture);
					
						if(p_url != null){
							
							String mediaId = Source.Type.Facebook+"#"+post.getId();
							//url
							MediaItem mediaItem = new MediaItem(p_url);
							
							//id
							mediaItem.setId(mediaId);
							//SocialNetwork Name
							mediaItem.setStreamId(streamId);
							//Reference
							mediaItem.setRef(id);
							//Type 
							mediaItem.setType("image");
							//Time of publication
							mediaItem.setPublicationTime(publicationTime);
							//PageUrl
							String pageUrl = post.getLink();
							mediaItem.setPageUrl(pageUrl);
							//Thumbnail
							String thumbnail = post.getPicture();
							mediaItem.setThumbnail(thumbnail);
							//Title
							mediaItem.setTitle(title);
							//Tags
							mediaItem.setTags(tags);
							//Popularity
							mediaItem.setLikes(likes);
							mediaItem.setShares(shares);
							
							//Store mediaItems and their ids 
							mediaItems.add(mediaItem);
							mediaIds.add(mediaId);
						}
					}
				}
			} catch (MalformedURLException e) { 
				e.printStackTrace();
			}
		}
		else if(type.equals("link")) {
			webPages = new ArrayList<WebPage>();
			String picture = post.getPicture(); ///!!!!
			if (picture != null) { 
				
				URL p_url = null;
				StringBuilder b = new StringBuilder(picture);
				int index = picture.lastIndexOf("_s.");
				if(index>0) {
					b.replace(index, index+3, "_n." );
					picture = picture.replaceAll("_s.", "_n.");
					try {
						p_url = new URL(picture);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					if(p_url != null){
						
						String mediaId = Source.Type.Facebook+"#"+post.getId();
						MediaItem mediaItem = new MediaItem(p_url);
						mediaItem.setId(mediaId);
						mediaItem.setType("image");
						mediaItem.setRef(id);
						String pageUrl = post.getLink();
						mediaItem.setPageUrl(pageUrl);

						// TODO: Cannot take media size. This needs a separate request. 
						
						String thumbnail = post.getPicture();
						mediaItem.setThumbnail(thumbnail);
						
						mediaItems.add(mediaItem);	
						mediaIds.add(mediaId);
					}
				}
			}
			
			String link = post.getLink();
			if (link != null) {
				
				WebPage webPage = new WebPage(link, id);
				webPage.setStreamId(streamId);
				webPages.add(webPage);
			}
		}
		else if(type.equals("video")) {
			
			String url = post.getSource();
			String picture = post.getPicture();
			if(picture!=null){
				
				URL videoUrl = null;
				try {
					videoUrl = new URL(url);
					String mediaId = Source.Type.Facebook+"#"+post.getId();
					MediaItem mediaItem = new MediaItem(videoUrl);
					mediaItem.setId(mediaId);
					mediaItem.setType("video");
					mediaItem.setThumbnail(post.getPicture());
					mediaItem.setRef(id);
					String pageUrl = post.getLink();
					mediaItem.setPageUrl(pageUrl);
					
					mediaItems.add(mediaItem);
					mediaIds.add(mediaId);
				} catch (MalformedURLException e) {
					
				}
			}
			
		}
	
	}
    
	public FacebookItem(Post post, Feed itemFeed) {
		
		this(post);

		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();

	}
	
	public FacebookItem(Comment comment,Post post) {
		super(Source.Type.Facebook.toString(), Operation.NEW);
		
		if (comment == null) return;
		
		//Id
		id = Source.Type.Facebook+"::"+comment.getId();
		//Reference to the original post
		reference = Source.Type.Facebook+"#"+post.getId();
		//SocialNetwork Name
		streamId = Source.Type.Facebook.toString();
		//Timestamp of the creation of the post
		publicationTime = comment.getCreatedTime().getTime();
		//Message that post contains
		String msg = comment.getMessage();
		if(msg!=null) {
			title = msg.subSequence(0, 100)+"...";
			description = "Comment";
		}
		//All the text inside the comment
		text = msg; 
		//User that posted the post
		CategorizedFacebookType user = comment.getFrom();
		if (user != null) {
			streamUser = new FacebookStreamUser(user);
			uid = streamUser.getId();
		}
		
		//Popularity of the post
		if(comment.getLikeCount() != null)
			likes = comment.getLikeCount().intValue();
	
	}
}
