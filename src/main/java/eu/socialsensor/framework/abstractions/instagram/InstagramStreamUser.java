package eu.socialsensor.framework.abstractions.instagram;

import org.jinstagram.entity.common.User;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the instagram user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class InstagramStreamUser extends StreamUser {
	
	public InstagramStreamUser(User user) {
		super(Source.Type.Instagram.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = Source.Type.Instagram + "#" + user.getId();
		//The id of the user in the network
		Long userLId = user.getId();
		userid = userLId.toString();
		//The name of the user
		name = user.getFullName();
		//The username of the user
		username = user.getUserName();
		//streamId
		streamId = Source.Type.Instagram.toString();
		//The description of the user
		description = user.getBio();
		//Profile picture of the user
		profileImage = user.getProfilePictureUrl();
		//The link to the user's profile
		linkToProfile = user.getWebsiteUrl();
	}
}
