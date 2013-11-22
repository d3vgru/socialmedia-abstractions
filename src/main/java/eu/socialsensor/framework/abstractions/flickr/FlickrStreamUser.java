package eu.socialsensor.framework.abstractions.flickr;

import java.util.Date;

import com.aetrion.flickr.people.User;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the flickr user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FlickrStreamUser extends StreamUser {

	public FlickrStreamUser(User user) {
		super(Source.Type.Flickr.toString(), Operation.NEW);
		if (user == null) return;
		
		id = Source.Type.Flickr+"::"+user.getId();
		
		userid = user.getId();
		username = user.getUsername();
		name = user.getRealName();
		
		items = user.getPhotosCount();
	
		imageUrl = user.getBuddyIconUrl();
		Date d = user.getPhotosFirstDate();
		if(d != null) {
			createdAt = d.toString();
		}
		
		location = user.getLocation();
	}


}
