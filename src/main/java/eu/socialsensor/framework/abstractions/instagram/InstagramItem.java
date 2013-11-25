package eu.socialsensor.framework.abstractions.instagram;

import java.util.Date;
import java.net.MalformedURLException;
import java.net.URL;

import org.jinstagram.entity.common.Caption;
import org.jinstagram.entity.common.ImageData;
import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.users.feed.MediaFeedData;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Source;


/**
 * Class that holds the information regarding the instagram image
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class InstagramItem extends Item {

	public InstagramItem(String id, Operation operation) {
		super(Source.Type.Instagram.toString(), operation);
		setId(Source.Type.Instagram+"#"+id);
	}
	
	public InstagramItem(MediaFeedData image) throws MalformedURLException {
		super(Source.Type.Instagram.toString(), Operation.NEW);
		
		if(image == null || image.getId() == null)
			return;
		
		//Id
		id = Source.Type.Instagram + "#" + image.getId();
		//SocialNetwork Name
		streamId =  Source.Type.Instagram.toString();
		//Timestamp of the creation of the photo
		int createdTime = Integer.parseInt(image.getCreatedTime());
		Date publicationDate = new Date((long) createdTime * 1000);
		publicationTime = publicationDate.getTime();
		//Title of the photo
		Caption caption = image.getCaption();
		String captionText = null;
		if(caption!=null){
			captionText = caption.getText();
			title = captionText;
		}
		//Tags
		int tIndex=0;
		int tagSize = image.getTags().size();
		String[] tempTags = new String[tagSize];
		
		for(String tag:image.getTags())
			tempTags[tIndex++]=tag;	
		//User that posted the photo
        if(image.getUser() !=null){
                streamUser = new InstagramStreamUser(image.getUser());
                uid = streamUser.getId();
        }
		//Location
		if(image.getLocation() != null){
			double latitude = image.getLocation().getLatitude();
			double longitude = image.getLocation().getLongitude();
			
			location = new Location(latitude, longitude);
		}
		//Popularity
		likes = image.getLikes().getCount();
		
		//Getting the photo
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
				
				//url
				MediaItem mediaItem = new MediaItem(mediaUrl);
				
				String mediaId = Source.Type.Instagram + "#"+image.getId(); 
				
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
				mediaItem.setPageUrl(image.getLink());
				//Thumbnail
				mediaItem.setThumbnail(thumbnail);
				//Title
				mediaItem.setTitle(title);
				//Tags
				mediaItem.setTags(tags);
				//Popularity
				mediaItem.setLikes(likes);
				mediaItem.setComments(image.getComments().getCount());
				//Location
				mediaItem.setLocation(location);

				mediaItems.add(mediaItem);
				mediaIds.add(mediaId);
			
			}
			
		}

	}
	
	public InstagramItem(MediaFeedData image,Feed itemFeed) throws MalformedURLException {
		this(image);
		
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
	
	}
	
	public InstagramItem(MediaFeedData image,InstagramStreamUser user,Feed itemFeed) throws MalformedURLException {
		this(image);
		
		//User that posted the post
		streamUser = user;
		uid = streamUser.getId();
		//Feed that retrieved the post
		feed = itemFeed;
		feedType = itemFeed.getFeedtype().toString();
	
	}
	
}
