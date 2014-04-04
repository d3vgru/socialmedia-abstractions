package eu.socialsensor.framework.abstractions;

import org.jinstagram.entity.common.User;
import org.jinstagram.entity.users.basicinfo.UserInfoData;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the instagram user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class InstagramStreamUser extends StreamUser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6507843124403291759L;

	public InstagramStreamUser(User user) {
		super(SocialNetworkSource.Instagram.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = SocialNetworkSource.Instagram + "#" + user.getId();
		//The id of the user in the network
		Long userLId = user.getId();
		userid = userLId.toString();
		//The name of the user
		name = user.getFullName();
		//The username of the user
		username = user.getUserName();
		//streamId
		streamId = SocialNetworkSource.Instagram.toString();
		//The description of the user
		description = user.getBio();
		//Profile picture of the user
		profileImage = user.getProfilePictureUrl();
		//The link to the user's profile
		pageUrl = "http://instagram.com/" + username;
	}
	

	public InstagramStreamUser(UserInfoData user) {
		super(SocialNetworkSource.Instagram.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = SocialNetworkSource.Instagram + "#" + user.getId();
		//The id of the user in the network
		Long userLId = user.getId();
		userid = userLId.toString();
		//The name of the user
		name = user.getFullName();
		//The username of the user
		username = user.getUsername();
		//streamId
		streamId = SocialNetworkSource.Instagram.toString();
		//The description of the user
		description = user.getBio();
		//Profile picture of the user
		profileImage = user.getProfile_picture();
		//The link to the user's profile
		pageUrl = "http://instagram.com/" + username;
	}
}
