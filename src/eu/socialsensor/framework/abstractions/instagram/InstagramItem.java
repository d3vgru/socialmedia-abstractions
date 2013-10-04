package eu.socialsensor.framework.abstractions.instagram;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.net.MalformedURLException;
import java.net.URL;

import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.common.Caption;
import org.jinstagram.entity.common.Comments;
import org.jinstagram.entity.common.ImageData;
import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.users.feed.MediaFeedData;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.MediaItemLight;
import eu.socialsensor.framework.common.domain.Source;



/**
 * Class that holds the information regarding the instagram image
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class InstagramItem extends Item {

	public InstagramItem(String id, Operation operation) {
		super(Source.Type.Instagram.toString(), operation);
		setId(Source.Type.Instagram+"::"+id);
	}
	
	public InstagramItem(MediaFeedData image) throws MalformedURLException {
		super(Source.Type.Instagram.toString(), Operation.NEW_UPDATE);
		
		if(image == null)
			return;
		
		//----photo id
		id = Source.Type.Instagram + "::" + image.getId();
		
		//----photo author
		streamUser = new InstagramStreamUser(image.getUser());
		if(streamUser !=null){
			author = streamUser.getId();
			uid = streamUser.getId();
		}
	
		//----photo publication time
		int createdTime = Integer.parseInt(image.getCreatedTime());
		Date publicationDate = new Date((long) createdTime * 1000);
		publicationTime = publicationDate.getTime();
		
		//----photo title
		Caption caption = image.getCaption();
		String captionText = null;
		if(caption!=null){
			captionText = caption.getText();
			title = captionText;
		}
		
		//----photo tags
		int tIndex=0;
		int tagSize = image.getTags().size();
		String[] tempTags = new String[tagSize];
		
		for(String tag:image.getTags())
			tempTags[tIndex++]=tag;	
		
		//----photo comments
		Comments com = image.getComments();
		int cIndex=0;
		int comSize = com.getComments().size();
		comments = new String[comSize];
		for(CommentData comment : com.getComments())
			comments[cIndex++] = comment.getText();
		
		//----photo content
		Images imageContent = image.getImages();
		ImageData thumb = imageContent.getThumbnail();
		String thumbnail = thumb.getImageUrl();
		
		ImageData standardUrl = imageContent.getStandardResolution();
		String url = standardUrl.getImageUrl();
		
		if(url!=null){
			URL mediaUrl = null;
			try {
				mediaUrl = new URL(url);
			} catch (MalformedURLException e) {
				
				e.printStackTrace();
			}
			MediaItem mediaItem = new MediaItem(mediaUrl);
			
			String mediaId = Source.Type.Instagram + "::"+image.getId(); 
			
			mediaItem.setId(mediaId);
			mediaItem.setThumbnail(thumbnail);
			mediaItem.setType("image");
			
			mediaItem.setRef(id);
			mediaItems.put(mediaUrl, mediaItem);
			mediaIds.add(mediaId);
			MediaItemLight mediaLink = new MediaItemLight(url, thumbnail);
			mediaLinks = new ArrayList<MediaItemLight>();
			mediaLinks.add(mediaLink);
		}
		
		//----photo source
		source = url;
		links = new URL[0];
		
		//----photo location
		if(image.getLocation() != null){
			double latitude = image.getLocation().getLatitude();
			double longitude = image.getLocation().getLongitude();
			
			location = new Location(latitude, longitude);
		}
		
		//----photo popularity
		popularity = new HashMap<String, Integer>();
		popularity.put("comments", com.getComments().size());
		popularity.put("likes", image.getLikes().getCount());
		
	}
	
	public InstagramItem(MediaFeedData image,Feed itemFeed) throws MalformedURLException {
		super(Source.Type.Instagram.toString(), Operation.NEW_UPDATE);
		
		if(image == null)
			return;
		
		//----photo id
		id = Source.Type.Instagram + "::" + image.getId();
		
		//----photo author
		streamUser = new InstagramStreamUser(image.getUser());
		if(streamUser !=null){
			author = streamUser.getId();
			uid = streamUser.getId();
		}
	
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
			
		//----photo publication time
		int createdTime = Integer.parseInt(image.getCreatedTime());
		Date publicationDate = new Date((long) createdTime * 1000);
		publicationTime = publicationDate.getTime();
		
		//----photo title
		Caption caption = image.getCaption();
		String captionText = null;
		if(caption!=null){
			captionText = caption.getText();
			title = captionText;
		}
		
		//----photo tags
		int tIndex=0;
		int tagSize = image.getTags().size();
		String[] tempTags = new String[tagSize];
		
		for(String tag:image.getTags())
			tempTags[tIndex++]=tag;	
		
		//----photo comments
		Comments com = image.getComments();
		int cIndex=0;
		int comSize = com.getComments().size();
		comments = new String[comSize];
		for(CommentData comment : com.getComments())
			comments[cIndex++] = comment.getText();
		
		//----photo content
		Images imageContent = image.getImages();
		ImageData thumb = imageContent.getThumbnail();
		String thumbnail = thumb.getImageUrl();
		
		ImageData standardUrl = imageContent.getStandardResolution();
		if(standardUrl != null){
			Integer width = standardUrl.getImageWidth();
			Integer height = standardUrl.getImageHeight();
		
			String url = standardUrl.getImageUrl();
		
			if(url!=null && (width>150) && (height>150)){
				URL mediaUrl = null;
				try {
					mediaUrl = new URL(url);
				} catch (MalformedURLException e) {
					
					e.printStackTrace();
				}
				MediaItem mediaItem = new MediaItem(mediaUrl);
				
				String mediaId = Source.Type.Instagram + "::"+image.getId(); 
				
				mediaItem.setId(mediaId);
				mediaItem.setThumbnail(thumbnail);
				mediaItem.setType("image");
				mediaItem.setRef(id);
				mediaItem.setDyscoId(feed.getDyscoId());
				mediaItems.put(mediaUrl, mediaItem);
				mediaIds.add(mediaId);
				MediaItemLight mediaLink = new MediaItemLight(url, thumbnail);
				mediaLinks = new ArrayList<MediaItemLight>();
				mediaLinks.add(mediaLink);
			}
			
			//----photo source
			source = url;
			links = new URL[0];
			
			//----photo location
			if(image.getLocation() != null){
				double latitude = image.getLocation().getLatitude();
				double longitude = image.getLocation().getLongitude();
				
				location = new Location(latitude, longitude);
			}
			
			//----photo popularity
			popularity = new HashMap<String, Integer>();
			popularity.put("comments", com.getComments().size());
			popularity.put("likes", image.getLikes().getCount());
			
		}
		
	}
	
}
