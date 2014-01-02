package eu.socialsensor.framework.abstractions.socialmedia.youtube;

import com.google.gdata.data.Person;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the youtube user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class YoutubeStreamUser extends StreamUser {

	public YoutubeStreamUser(String user) {
		super(SocialNetworkSource.Youtube.toString(), Operation.NEW);
		if (user == null) return;
		//Id
		id = SocialNetworkSource.Youtube+"#"+user;
		//The name of the user
		username = user;
		//streamId
		streamId = SocialNetworkSource.Youtube.toString();
	}

	public YoutubeStreamUser(Person user) {
		super(SocialNetworkSource.Youtube.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = SocialNetworkSource.Youtube+"#"+user.getName();
		//The id of the user in the network
		userid = user.getName();
		//The name of the user
		username = user.getName();
		//streamId
		streamId = SocialNetworkSource.Youtube.toString();
		//The link to the user's profile
		linkToProfile = user.getUri();
	}
	
}
