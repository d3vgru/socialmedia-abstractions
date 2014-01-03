package eu.socialsensor.framework.abstractions.flickr;

import com.flickr4java.flickr.people.User;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the flickr user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FlickrStreamUser extends StreamUser {

	public FlickrStreamUser(User user) {
		super(SocialNetworkSource.Flickr.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = SocialNetworkSource.Flickr+"#"+user.getId();
		//The id of the user in the network
		userid = user.getId();
		//The name of the user
		name = user.getRealName();
		//The username of the user
		username = user.getUsername();
		//streamId
		streamId = SocialNetworkSource.Flickr.toString();
		//Profile picture of the user
		profileImage = user.getBuddyIconUrl();
		items = user.getPhotosCount();
		
		pageUrl = user.getProfileurl();
		
		//Location
		location = user.getLocation();
	}


}
