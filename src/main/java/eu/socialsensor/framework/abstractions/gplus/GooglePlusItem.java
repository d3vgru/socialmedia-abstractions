package eu.socialsensor.framework.abstractions.gplus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.api.client.util.DateTime;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.Activity.Actor;
import com.google.api.services.plus.model.Activity.PlusObject.Attachments;
import com.google.api.services.plus.model.Activity.PlusObject.Attachments.Embed;
import com.google.api.services.plus.model.Activity.PlusObject.Attachments.FullImage;
import com.google.api.services.plus.model.Activity.PlusObject.Attachments.Image;
import com.google.api.services.plus.model.Activity.PlusObject.Attachments.Thumbnails;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.MediaItemLight;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.WebPage;

/**
 * Class that holds the information regarding the google plus activity
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class GooglePlusItem extends Item {
	//private Logger logger = Logger.getLogger(GooglePlusItem.class);
	
	public GooglePlusItem(String id, Operation operation) {
		super(Source.Type.GooglePlus.toString(), operation);
		setId(Source.Type.GooglePlus+"::"+id);
	}
	
	
	public GooglePlusItem(Activity activity) {
		
		super(Source.Type.GooglePlus.toString(), Operation.NEW);
		
		if(activity == null || activity.getId() == null) return;
		
		id = Source.Type.GooglePlus + "::" + activity.getId();
		
		source = "GooglePlus";
		
		Actor actor = activity.getActor();
	
		if(actor != null) {
			streamUser = new GooglePlusStreamUser(actor);
			uid = streamUser.getId();
			author = actor.getDisplayName();
		}
		
		DateTime datePosted = activity.getPublished();
		publicationTime = datePosted.getValue();
		
		title = activity.getTitle();
		
		tags = null;
	
		webPages = new ArrayList<WebPage>();
		mediaLinks = new ArrayList<MediaItemLight>();
		Set<URL> urls = new HashSet<URL>();
		String pageURL = activity.getUrl();
		
		for(Attachments attachment : activity.getObject().getAttachments()){
			
			String type = attachment.getObjectType();
			
			if(attachment != null && attachment.getId()!=null){
				if(type.equals("video")) {
					Image image = attachment.getImage();
					if(image != null){
						Embed embed = attachment.getEmbed();
			    		
			    		String videoUrl = embed.getUrl();
			    		
						URL mediaUrl = null;
			    		try {	
			    			mediaUrl = new URL(embed.getUrl());
			    		} catch (MalformedURLException e1) {
			    			e1.printStackTrace();
			    		}
			    		
			    		MediaItem mediaItem = new MediaItem(mediaUrl);
			    		
			    		String mediaId = Source.Type.GooglePlus + "::"+attachment.getId(); 
			    		String thumbUrl = image.getUrl();
			    		mediaItem.setId(mediaId);
			    		mediaItem.setType("video");
			    		mediaItem.setPageUrl(pageURL);
			    		mediaItem.setRef(id);
			    		mediaItem.setThumbnail(thumbUrl);
			    		
			    		mediaItems.put(mediaUrl, mediaItem);	
			    		MediaItemLight mediaLink = new MediaItemLight(mediaUrl.toString(), null);				
			    		mediaLinks.add(mediaLink);			
			    		mediaIds.add(mediaId);		
			    		
					}
		    		
		    	}	
		    	else if(type.equals("photo")) {		
		    		
		    		FullImage image = attachment.getFullImage();
	    			String imageUrl = image.getUrl();
		    		Image thumbnail = attachment.getImage();

		    		if(thumbnail != null){
		    			URL mediaUrl = null;
			    		try {
			    			mediaUrl = new URL(imageUrl);
			    		} catch (MalformedURLException e2) {
			    			e2.printStackTrace();
			    		}
		    			//String thumbnail = attachment.getImage().getUrl();
		    			String thumnailUrl = thumbnail.getUrl();
		    			MediaItem mediaItem = new MediaItem(mediaUrl);
		    			
		    			String mediaId = Source.Type.GooglePlus + "::"+attachment.getId(); 
		    			
		        		mediaItem.setId(mediaId);
		        		mediaItem.setThumbnail(thumnailUrl);
		        		mediaItem.setType("image");
		        		mediaItem.setPageUrl(pageURL);
		        		mediaItem.setDyscoId(feed.getDyscoId());
		        		Long width = image.getWidth();
		        		Long height = image.getHeight();
		        		if(width != null && height != null) {
		        			mediaItem.setSize(width.intValue(), height.intValue());
		        		}
		        		mediaItem.setRef(id);
		        		
		        		mediaItems.put(mediaUrl, mediaItem);	
		        		MediaItemLight mediaLink = new MediaItemLight(image.getUrl(), thumnailUrl);				
		        		mediaLinks.add(mediaLink);			
		        		
		        		mediaIds.add(mediaId);		
		        		
		    		}
		    		
		    	}
		    	else if(type.equals("album")) {		
		    		
		    		for(Thumbnails image : attachment.getThumbnails()){
		    			//logger.info("image of album : "+image.getImage().getUrl());
		    			com.google.api.services.plus.model.Activity.PlusObject.Attachments.Thumbnails.Image thumbnail = image.getImage();
		    			
		    			if(thumbnail != null){
		    				URL mediaUrl = null;
			    			try {
			    				mediaUrl = new URL(image.getImage().getUrl());
			    			} catch (MalformedURLException e3) {
				    			e3.printStackTrace();
				    		}
			    			
			    			
		    				MediaItem mediaItem = new MediaItem(mediaUrl);
		    				
		    				String mediaId = Source.Type.GooglePlus + "::"+attachment.getId(); 
		    				String thumbnailUrl = thumbnail.getUrl();
		    				mediaItem.setId(mediaId);
			        		mediaItem.setThumbnail(thumbnailUrl);
			        		mediaItem.setType("image");
			        		mediaItem.setPageUrl(pageURL);
			        		Long width = image.getImage().getWidth();
			        		Long height = image.getImage().getHeight();
			        		if(width != null && height != null) {
			        			mediaItem.setSize(width.intValue(), height.intValue());
			        		}
			        		mediaItem.setRef(id);
			        		
			        		mediaItems.put(mediaUrl, mediaItem);	
			        		MediaItemLight mediaLink = new MediaItemLight(image.getUrl(), null);				
			        		mediaLinks.add(mediaLink);			
			        		
			        		mediaIds.add(mediaId);		
				        		
		    			}
			    		
		    		}
		    	}
		    	/*else if(type.equals("article")) {		
		    		
		    		webPages = new ArrayList<WebPage>();
		    		String link = attachment.getUrl();
					try {
						if (link != null) {
							urls.add(new URL(link));
							
							WebPage webPage = new WebPage(link, id);
							webPage.setStreamId(streamId);
							webPages.add(webPage);
						}
					} catch (MalformedURLException e) { 
						e.printStackTrace();
					}
		    	}*/
			}
			}
			
			
		
		links = urls.toArray(new URL[urls.size()]);
		
		if(activity.getGeocode() != null){
			
			String locationInfo = activity.getGeocode();
			String[] parts = locationInfo.split(" ");
			double latitude = Double.parseDouble(parts[0]);
			double longitude = Double.parseDouble(parts[1]);
			
			location = new Location(latitude, longitude,activity.getPlaceName());
		}
		
		popularity = new HashMap<String, Integer>();
		if(activity.getObject().getPlusoners() != null)
			popularity.put("Plusoners", activity.getObject().getPlusoners().getTotalItems().intValue());
		
		if(activity.getObject().getResharers() != null)
			popularity.put("Resharers", activity.getObject().getResharers().getTotalItems().intValue());
		
	}
	
	public GooglePlusItem(Activity activity, Feed itemFeed) {
		
		super(Source.Type.GooglePlus.toString(), Operation.NEW);
		if(activity == null || activity.getId() == null) return;
		
		id = Source.Type.GooglePlus + "::" + activity.getId();
		
		source = "GooglePlus";
		
		Actor actor = activity.getActor();
	
		if(actor != null) {
			streamUser = new GooglePlusStreamUser(actor);
			uid = streamUser.getId();
			author = actor.getDisplayName();
		}
		
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
		
		DateTime datePosted = activity.getPublished();
		publicationTime = datePosted.getValue();
		
		title = activity.getTitle();
		
		tags = null;
	
		webPages = new ArrayList<WebPage>();
		mediaLinks = new ArrayList<MediaItemLight>();
		Set<URL> urls = new HashSet<URL>();
		String pageURL = activity.getUrl();
		
		for(Attachments attachment : activity.getObject().getAttachments()){
			
			String type = attachment.getObjectType();
			if(attachment !=null && attachment.getId()!=null){
				if(type.equals("video")) {
		    		
					Image image = attachment.getImage();
					Embed embed = attachment.getEmbed();
					
					if(embed != null){
				
			    		String videoUrl = embed.getUrl();
			    		
						URL mediaUrl = null;
			    		try {	
			    			mediaUrl = new URL(videoUrl);
			    		} catch (MalformedURLException e1) {
			    			return;
			    		}
			    		
			    		MediaItem mediaItem = new MediaItem(mediaUrl);
			    		
			    		String mediaId = Source.Type.GooglePlus + "::"+attachment.getId(); 
			    		String thumbUrl = image.getUrl();
			    		mediaItem.setId(mediaId);
			    		mediaItem.setType("video");
			    		mediaItem.setDyscoId(feed.getDyscoId());
			    		mediaItem.setPageUrl(pageURL);
			    		mediaItem.setRef(id);
			    		mediaItem.setThumbnail(thumbUrl);
			    		
			    		mediaItems.put(mediaUrl, mediaItem);	
			    		MediaItemLight mediaLink = new MediaItemLight(mediaUrl.toString(), null);				
			    		mediaLinks.add(mediaLink);			
			    		mediaIds.add(mediaId);		
				    	
					}
		    	}	
		    	else if(type.equals("photo")) {		
		    		
	    			FullImage image = attachment.getFullImage();
	    			String imageUrl = image.getUrl();
		    		Image thumbnail = attachment.getImage();
		    		
		    		Integer width = image.getWidth().intValue();
					Integer height = image.getHeight().intValue();
					
		    		if(thumbnail != null && (width > 250 && height > 250)){
		    			URL mediaUrl = null;
			    		try {
			    			mediaUrl = new URL(imageUrl);
			    		} catch (MalformedURLException e2) {
			    			return;
			    		}
		    			
		    			String thumnailUrl = thumbnail.getUrl();
		    			MediaItem mediaItem = new MediaItem(mediaUrl);
		    			
		    			String mediaId = Source.Type.GooglePlus + "::"+attachment.getId(); 
		    			
		        		mediaItem.setId(mediaId);
		        		mediaItem.setThumbnail(thumnailUrl);
		        		mediaItem.setType("image");
		        		mediaItem.setPageUrl(pageURL);
		        		mediaItem.setDyscoId(feed.getDyscoId());
		        		mediaItem.setSize(width,height);
		        		mediaItem.setRef(id);
		        		
		        		mediaItems.put(mediaUrl, mediaItem);	
		        		MediaItemLight mediaLink = new MediaItemLight(image.getUrl(), thumnailUrl);				
		        		mediaLinks.add(mediaLink);			
		        		
		        		mediaIds.add(mediaId);		
		        		
		    		}
		    		
		    		
		    	}
		    	else if(type.equals("album")) {		
		    		
		    		for(Thumbnails image : attachment.getThumbnails()){
		    			
		    			com.google.api.services.plus.model.Activity.PlusObject.Attachments.Thumbnails.Image thumbnail = image.getImage();
		    			
		    			if(thumbnail != null && image.getImage().getWidth()>250 && image.getImage().getHeight()>250){
		    				URL mediaUrl = null;
			    			try {
			    				mediaUrl = new URL(image.getImage().getUrl());
			    			} catch (MalformedURLException e3) {
				    			return;
				    		}
			    			
			    			
		    				MediaItem mediaItem = new MediaItem(mediaUrl);
		    				
		    				String mediaId = Source.Type.GooglePlus + "::"+attachment.getId(); 
		    				String thumbnailUrl = thumbnail.getUrl();
		    				mediaItem.setId(mediaId);
			        		mediaItem.setThumbnail(thumbnailUrl);
			        		mediaItem.setType("image");
			        		mediaItem.setPageUrl(pageURL);
			        		Long width = image.getImage().getWidth();
			        		Long height = image.getImage().getHeight();
			        		if(width != null && height != null) {
			        			mediaItem.setSize(width.intValue(), height.intValue());
			        		}
			        		mediaItem.setRef(id);
			        		
			        		mediaItems.put(mediaUrl, mediaItem);	
			        		MediaItemLight mediaLink = new MediaItemLight(image.getUrl(), null);				
			        		mediaLinks.add(mediaLink);			
			        		
			        		mediaIds.add(mediaId);		
				        		
		    			}
			    		
		    		}
		    	}
		    	/*else if(type.equals("article")) {		
		    		
		    		webPages = new ArrayList<WebPage>();
		    		String link = attachment.getUrl();
					try {
						if (link != null) {
							urls.add(new URL(link));
							
							WebPage webPage = new WebPage(link, id);
							webPage.setStreamId(streamId);
							webPages.add(webPage);
						}
					} catch (MalformedURLException e) { 
						e.printStackTrace();
					}
		    	}*/
			}
		}
			
		
		links = urls.toArray(new URL[urls.size()]);
		
		if(activity.getGeocode() != null){
			
			String locationInfo = activity.getGeocode();
			String[] parts = locationInfo.split(" ");
			double latitude = Double.parseDouble(parts[0]);
			double longitude = Double.parseDouble(parts[1]);
			
			location = new Location(latitude, longitude,activity.getPlaceName());
		}
		
		popularity = new HashMap<String, Integer>();
		if(activity.getObject().getPlusoners() != null)
			popularity.put("Plusoners", activity.getObject().getPlusoners().getTotalItems().intValue());
		
		if(activity.getObject().getResharers() != null)
			popularity.put("Resharers", activity.getObject().getResharers().getTotalItems().intValue());
		
	}
}
