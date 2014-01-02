package eu.socialsensor.framework.abstractions.socialmedia.gplus;

import com.google.api.services.plus.model.Activity.Actor;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the google plus user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class GooglePlusStreamUser extends StreamUser {
	
	public GooglePlusStreamUser(Actor actor) {
		super(SocialNetworkSource.GooglePlus.toString(), Operation.NEW);
		if (actor == null) return;
		
		//Id
		id = SocialNetworkSource.GooglePlus + "#"+actor.getId();
		//The id of the user in the network
		userid = actor.getId();
		//The name of the user
		name = actor.getDisplayName();
		//The username of the user
		username = actor.getDisplayName();
		//streamId
		streamId = SocialNetworkSource.GooglePlus.toString();
		//Profile picture of the user
		profileImage = actor.getImage().getUrl();
		//The link to the user's profile
		linkToProfile = actor.getUrl();
		
	}


}
