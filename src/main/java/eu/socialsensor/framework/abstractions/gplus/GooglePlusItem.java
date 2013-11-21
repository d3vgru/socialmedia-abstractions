package eu.socialsensor.framework.abstractions.gplus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
		setId(Source.Type.GooglePlus+"#"+id);
	}
	
	
	public GooglePlusItem(Activity activity) {
		
		super(Source.Type.GooglePlus.toString(), Operation.NEW);
		
		if(activity == null || activity.getId() == null) return;
		
		//Id
		id = Source.Type.GooglePlus + "#" + activity.getId();
		//SocialNetwork Name
		streamId = Source.Type.GooglePlus.toString();
		//Timestamp of the creation of the post
		publicationTime =  activity.getPublished().getValue();
		//Title of the post
		title = activity.getTitle();
		//User that made the post
		Actor actor = activity.getActor();
		if(actor != null) {
			streamUser = new GooglePlusStreamUser(actor);
			uid = streamUser.getId();
		}
		//Location
		if(activity.getGeocode() != null){
			
			String locationInfo = activity.getGeocode();
			String[] parts = locationInfo.split(" ");
			double latitude = Double.parseDouble(parts[0]);
			double longitude = Double.parseDouble(parts[1]);
			
			location = new Location(latitude, longitude,activity.getPlaceName());
		}
		//Popularity
		if(activity.getObject().getPlusoners() != null)
			likes = activity.getObject().getPlusoners().getTotalItems().intValue();
			
		if(activity.getObject().getResharers() != null)
			shares = activity.getObject().getResharers().getTotalItems().intValue();
			
		
		//Media Items - WebPages in a post
	
		webPages = new ArrayList<WebPage>();
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
			    		
			    		String mediaId = Source.Type.GooglePlus + "#"+attachment.getId(); 
			    		String thumbUrl = image.getUrl();
			    		mediaItem.setId(mediaId);
			    		mediaItem.setType("video");
			    		mediaItem.setPageUrl(pageURL);
			    		mediaItem.setRef(id);
			    		mediaItem.setThumbnail(thumbUrl);
			    		
			    		mediaItems.add(mediaItem);			
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
		    			
		    			String mediaId = Source.Type.GooglePlus + "#"+attachment.getId(); 
		    			
		        		mediaItem.setId(mediaId);
		        		mediaItem.setThumbnail(thumnailUrl);
		        		mediaItem.setType("image");
		        		mediaItem.setPageUrl(pageURL);
		        		mediaItem.setSize(width,height);
		        		mediaItem.setRef(id);
		        		mediaItems.add(mediaItem);
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
		    				
		    				String mediaId = Source.Type.GooglePlus + "#"+attachment.getId(); 
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
			        		mediaItems.add(mediaItem);		
			        		mediaIds.add(mediaId);		
				        		
		    			}
			    		
		    		}
		    	}
		    	else if(type.equals("article")) {		
		    		
		    		webPages = new ArrayList<WebPage>();
		    		String link = attachment.getUrl();
					if (link != null) {
						
						WebPage webPage = new WebPage(link, id);
						webPage.setStreamId(streamId);
						webPages.add(webPage);
					}
		    	}
			}
		}

	}
	
	public GooglePlusItem(Activity activity, Feed itemFeed) {
		this(activity);
		
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
		
		
	}
}
