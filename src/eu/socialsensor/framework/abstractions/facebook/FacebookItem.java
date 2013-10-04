package eu.socialsensor.framework.abstractions.facebook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Place;
import com.restfb.types.Post;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.MediaItemLight;
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
		setId(Source.Type.Facebook+"::"+id);
	}
	
	public FacebookItem(Post post) {
		
		super(Source.Type.Facebook.toString(), Operation.NEW_UPDATE);
		
		if (post == null) return;
		
		id = Source.Type.Facebook+"::"+post.getId();
		
		NamedFacebookType application = post.getApplication();
		if(application != null)
			source = application.getName();
		
		author = null;
		CategorizedFacebookType user = post.getFrom();
		if (user != null) {
			streamUser = new FacebookStreamUser(user);
			uid = streamUser.getId();
			author = streamUser.getUserId();
			//author = user.getName();
		}
		
		Date pubDate = post.getCreatedTime();
		publicationTime = pubDate.getTime();
		
		Set<URL> ulinks = new HashSet<URL>();
		Set<URL> mlinks = new HashSet<URL>();
		
		String type = post.getType();
		
		if(type.equals("photo")) {
		
			String picture = post.getPicture();
			
			try {
				if (picture != null) { 
					
					URL p_url = new URL(picture);
					
					String mediaId = Source.Type.Facebook+"::"+post.getId();
					MediaItem mediaItem = new MediaItem(p_url);
					mediaItem.setId(mediaId);
					mediaItem.setType("image");
					mediaItem.setRef(id);
					String pageUrl = post.getLink();
					mediaItem.setPageUrl(pageUrl);
					
					// TODO: Cannot take media size. This needs a separate request. 
					
					String thumbnail = picture;
					mediaItem.setThumbnail(thumbnail);
					StringBuilder b = new StringBuilder(picture);
					int index = picture.lastIndexOf("_s.");
					if(index>0) {
						b.replace(index, index+3, "_n." );
						picture = picture.replaceAll("_s.", "_n.");
						p_url = new URL(picture);
					}
					mediaLinks = new ArrayList<MediaItemLight>();
					mediaLinks.add(new MediaItemLight(picture, thumbnail));
					
					mlinks.add(p_url);
					mediaItems.put(p_url, mediaItem);	
					mediaIds.add(mediaId);
				}
			} catch (MalformedURLException e) { 
				e.printStackTrace();
			}
		}
		else if(type.equals("link")) {
			webPages = new ArrayList<WebPage>();
			String picture = post.getPicture(); 
			if (picture != null) { 
				
				URL p_url = null;
				try {
					p_url = new URL(picture);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String mediaId = Source.Type.Facebook+"::"+post.getId();
				MediaItem mediaItem = new MediaItem(p_url);
				mediaItem.setId(mediaId);
				mediaItem.setType("image");
				mediaItem.setRef(id);
				String pageUrl = post.getLink();
				mediaItem.setPageUrl(pageUrl);
				
				String thumbnail = picture;
				mediaItem.setThumbnail(thumbnail);
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
				}
				mediaLinks = new ArrayList<MediaItemLight>();
				mediaLinks.add(new MediaItemLight(picture, thumbnail));
				
				mlinks.add(p_url);
				mediaItems.put(p_url, mediaItem);	
				mediaIds.add(mediaId);
			}
			
			String link = post.getLink();
			try {
				if (link != null) {
					ulinks.add(new URL(link));
					
					WebPage webPage = new WebPage(link, id);
					webPage.setStreamId(streamId);
					webPage.setDate(new Date(publicationTime));
					webPages.add(webPage);
				}
			} catch (MalformedURLException e) { 
				e.printStackTrace();
			}
		}
		else if(type.equals("video")) {
			
			String url = post.getSource();
			String picture = post.getPicture();
			if(picture!=null){
				
				URL videoUrl = null;
				try {
					videoUrl = new URL(url);
					String mediaId = Source.Type.Facebook+"::"+post.getId();
					MediaItem mediaItem = new MediaItem(videoUrl);
					mediaItem.setId(mediaId);
					mediaItem.setType("video");
					mediaItem.setThumbnail(picture);
					mediaItem.setRef(id);
					String pageUrl = post.getLink();
					mediaItem.setPageUrl(pageUrl);
					mediaItem.setDyscoId(feed.getDyscoId());
					
					String thumbnail = post.getPicture();
					
					mediaLinks = new ArrayList<MediaItemLight>();
					mediaLinks.add(new MediaItemLight(thumbnail, thumbnail));
					
					mlinks.add(videoUrl);
					mediaItems.put(videoUrl, mediaItem);	
					mediaIds.add(mediaId);
				} catch (MalformedURLException e) {
					
				}
			}
		}
		
		links = ulinks.toArray(new URL[ulinks.size()]);
		//mediaLinks = mlinks.toArray(new URL[mlinks.size()]);
		
		String msg = post.getMessage();
		
		if(msg!=null && msg.length() > 100) {
			title = msg.subSequence(0, 100)+"...";
			description = msg;
		}
		else {
			title = msg;
			description = post.getDescription();
		}
		
		
		popularity = new HashMap<String, Integer>();
		
		Long likes = post.getLikesCount();
		if(likes != null)
			popularity.put("likes", likes.intValue());
		
		Long shares = post.getSharesCount();
		if(shares != null)
			popularity.put("shares", shares.intValue());	
		if(post.getComments()!=null){
			Long numberOfComments = post.getComments().getCount();
			if(numberOfComments != null)
				popularity.put("comments", numberOfComments.intValue());
		}
		
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
		
		
	}
    
	public FacebookItem(Post post,Feed itemFeed) {
		
		super(Source.Type.Facebook.toString(), Operation.NEW_UPDATE);
		
		if (post == null) return;
		
		id = Source.Type.Facebook+"::"+post.getId();
		
		NamedFacebookType application = post.getApplication();
		if(application != null)
			source = application.getName();
		
		author = null;
		CategorizedFacebookType user = post.getFrom();
		if (user != null) {
			streamUser = new FacebookStreamUser(user);
			uid = streamUser.getId();
			author = streamUser.getUserId();
			//author = user.getName();
		}
			
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
		
		
		Set<URL> ulinks = new HashSet<URL>();
		Set<URL> mlinks = new HashSet<URL>();
		
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
							
							String mediaId = Source.Type.Facebook+"::"+post.getId();
							MediaItem mediaItem = new MediaItem(p_url);
							mediaItem.setId(mediaId);
							mediaItem.setType("image");
							mediaItem.setRef(id);
							String pageUrl = post.getLink();
							mediaItem.setPageUrl(pageUrl);
							mediaItem.setDyscoId(feed.getDyscoId());
							// TODO: Cannot take media size. This needs a separate request. 
							
							String thumbnail = post.getPicture();
							mediaItem.setThumbnail(thumbnail);
							
							mediaLinks = new ArrayList<MediaItemLight>();
							mediaLinks.add(new MediaItemLight(picture, thumbnail));
							
							mlinks.add(p_url);
							mediaItems.put(p_url, mediaItem);	
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
						
						String mediaId = Source.Type.Facebook+"::"+post.getId();
						MediaItem mediaItem = new MediaItem(p_url);
						mediaItem.setId(mediaId);
						mediaItem.setType("image");
						mediaItem.setRef(id);
						String pageUrl = post.getLink();
						mediaItem.setPageUrl(pageUrl);
						mediaItem.setDyscoId(feed.getDyscoId());
						// TODO: Cannot take media size. This needs a separate request. 
						
						String thumbnail = post.getPicture();
						mediaItem.setThumbnail(thumbnail);
						
						mediaLinks = new ArrayList<MediaItemLight>();
						mediaLinks.add(new MediaItemLight(picture, thumbnail));
						
						mlinks.add(p_url);
						mediaItems.put(p_url, mediaItem);	
						mediaIds.add(mediaId);
					}
				}
			}
			
			String link = post.getLink();
			try {
				if (link != null) {
					ulinks.add(new URL(link));
					
					WebPage webPage = new WebPage(link, id);
					webPage.setStreamId(streamId);
					webPages.add(webPage);
				}
			} catch (MalformedURLException e) { 
				e.printStackTrace();
			}
		}
		else if(type.equals("video")) {
			
			String url = post.getSource();
			String picture = post.getPicture();
			if(picture!=null){
				
				URL videoUrl = null;
				try {
					videoUrl = new URL(url);
					String mediaId = Source.Type.Facebook+"::"+post.getId();
					MediaItem mediaItem = new MediaItem(videoUrl);
					mediaItem.setId(mediaId);
					mediaItem.setType("video");
					mediaItem.setThumbnail(post.getPicture());
					mediaItem.setRef(id);
					String pageUrl = post.getLink();
					mediaItem.setPageUrl(pageUrl);
					mediaItem.setDyscoId(feed.getDyscoId());
					
					String thumbnail = post.getPicture();
					
					mediaLinks = new ArrayList<MediaItemLight>();
					mediaLinks.add(new MediaItemLight(thumbnail, thumbnail));
					
					mlinks.add(videoUrl);
					mediaItems.put(videoUrl, mediaItem);	
					mediaIds.add(mediaId);
				} catch (MalformedURLException e) {
					
				}
			}
			
		}
		
		Date pubDate = post.getCreatedTime();
		publicationTime = pubDate.getTime();
		
		links = ulinks.toArray(new URL[ulinks.size()]);
		//mediaLinks = mlinks.toArray(new URL[mlinks.size()]);
		
		String msg = post.getMessage();
		
		if(msg!=null && msg.length() > 100) {
			title = msg.subSequence(0, 100)+"...";
			description = msg;
		}
		else {
			title = msg;
			description = post.getDescription();
		}
		
		popularity = new HashMap<String, Integer>();
		
		Long likes = post.getLikesCount();
		if(likes != null)
			popularity.put("likes", likes.intValue());
		
		Long shares = post.getSharesCount();
		if(shares != null)
			popularity.put("shares", shares.intValue());	
		if(post.getComments()!=null){
			Long numberOfComments = post.getComments().getCount();
			if(numberOfComments != null)
				popularity.put("comments", numberOfComments.intValue());
		}
		
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
		
		
	}
	
	public FacebookItem(Comment comment) {
		super(Source.Type.Facebook.toString(), Operation.NEW_UPDATE);
		if (comment == null) return;
		
		id = Source.Type.Facebook+"::"+comment.getId();
		
		author = null;
		CategorizedFacebookType user = comment.getFrom();
		if (user != null) {
			streamUser = new FacebookStreamUser(user);
			uid = streamUser.getId();
			author = streamUser.getUserId();
		}
		
		Date pubDate = comment.getCreatedTime();
		publicationTime = pubDate.getTime();

		title = comment.getMessage();
		description = "comment";
		
		links = new URL[0];
		mediaLinks = new ArrayList<MediaItemLight>();
		
		popularity = new HashMap<String, Integer>();
		
		Long likes = comment.getLikeCount();
		if(likes != null)
			popularity.put("likes", likes.intValue());
		
	}
}
