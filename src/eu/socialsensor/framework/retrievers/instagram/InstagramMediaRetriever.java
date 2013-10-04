package eu.socialsensor.framework.retrievers.instagram;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jinstagram.Instagram;
import org.jinstagram.InstagramOembed;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.common.Caption;
import org.jinstagram.entity.common.ImageData;
import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.media.MediaInfoFeed;
import org.jinstagram.entity.oembed.OembedInformation;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;

import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.retrievers.MediaRetriever;


/**
 * The retriever that implements the Instagram simplified retriever
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class InstagramMediaRetriever  implements MediaRetriever {

	private Instagram instagram = null;
	private InstagramOembed instagramOembed = null;
	
	public InstagramMediaRetriever(String secret, String token) {
		Token instagramToken = new Token(token,secret); 
		this.instagram = new Instagram(instagramToken);
		this.instagramOembed = new InstagramOembed();
	}
	
	private String getMediaId(String url) {
		try {
			OembedInformation info = instagramOembed.getOembedInformation(url);
			return info.getMediaId();
		} catch (InstagramException e) {

		}
		return null;
	}
	
	public MediaItem getMediaItem(String sid) {
		try {
			String id = getMediaId("http://instagram.com/p/"+sid);
			
			MediaInfoFeed mediaInfo = instagram.getMediaInfo(id);
			if(mediaInfo != null) {
				MediaFeedData mediaData = mediaInfo.getData();
				Images images = mediaData.getImages();
				
				ImageData standardUrl = images.getStandardResolution();
				String url = standardUrl.getImageUrl();
				
				MediaItem mediaItem = new MediaItem(new URL(url));
				
				ImageData thumb = images.getThumbnail();
				String thumbnail = thumb.getImageUrl();
				
				String mediaId = "Instagram::" + mediaData.getId();
				List<String> tags = mediaData.getTags();
				
				Caption caption = mediaData.getCaption();
				String title = caption.getText();
				
				Long publicationTime = new Long(Long.parseLong(mediaData.getCreatedTime()));
				
				mediaItem.setId(mediaId);
				mediaItem.setTags(tags.toArray(new String[tags.size()]));
				
				mediaItem.setThumbnail(thumbnail);
				mediaItem.setPublicationTime(publicationTime);
				mediaItem.setTitle(title);
				
				ImageData standard = images.getStandardResolution();
				if(standard!=null) {
					int height = standard.getImageHeight();
					int width = standard.getImageWidth();
					mediaItem.setSize(width, height);
				}
				
				
				
				mediaItem.setType("image");
				mediaItem.setStreamId("Instagram");
				
				// Image geo-location
				org.jinstagram.entity.common.Location geoLocation = mediaData.getLocation();
				if(geoLocation != null) {
					double latitude = geoLocation.getLatitude();
					double longitude = geoLocation.getLongitude();
					
					Location location = new Location(latitude, longitude);
					location.setName(geoLocation.getName());
					mediaItem.setLocation(location);
				}
				
				// Image popularity
				Map<String, Integer> popularity = new HashMap<String, Integer>();	
				popularity.put("comments", mediaData.getComments().getCount());
				popularity.put("likes", mediaData.getLikes().getCount());
				
				mediaItem.setPopularity(popularity);
				
				return mediaItem;
			}
		} catch (Exception e) {
		
		} 
//		catch (MalformedURLException e) {
//		
//		}
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String instagramToken = "342704836.5b9e1e6.503a35185da54224adaa76161a573e71";
		String instagramSecret = "e53597da6d7749d2a944651bbe6e6f2a";
		
		InstagramMediaRetriever retriever = new InstagramMediaRetriever(instagramSecret,
				instagramToken);
		
		MediaItem mediaItem = retriever.getMediaItem("brxPNgt3OS");
		System.out.println(mediaItem.toJSONString());
	}

}
