package eu.socialsensor.framework.abstractions.flickr;

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
		
		//Id
		id = Source.Type.Flickr+"#"+user.getId();
		//The id of the user in the network
		userid = user.getId();
		//The name of the user
		name = user.getRealName();
		//The username of the user
		username = user.getUsername();
		//streamId
		streamId = Source.Type.Flickr.toString();
		//Profile picture of the user
		profileImage = user.getBuddyIconUrl();
		//Location
		location = user.getLocation();
	}


}
