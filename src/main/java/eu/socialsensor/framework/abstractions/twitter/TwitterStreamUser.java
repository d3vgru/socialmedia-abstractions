package eu.socialsensor.framework.abstractions.twitter;

import java.util.Date;

import twitter4j.User;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the twitter user
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitterStreamUser extends StreamUser {

	public TwitterStreamUser(User user) {
		super(Source.Type.Twitter.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = Source.Type.Twitter + "#" + user.getId();
		//The id of the user in the network
		userid = Long.toString(user.getId());
		//The name of the user
		name = user.getName();
		//The username of the user
		username = user.getScreenName();
		//streamId
		streamId = Source.Type.Twitter.toString();
		//The description of the user
		description = user.getDescription();
		//Profile picture of the user
		profileImage = user.getProfileImageURL();
		//Statuses of the user
		items = user.getStatusesCount();
		//Creation date of user's profile
		Date date = user.getCreatedAt();
		if(date != null) {
			createdAt = date.toString();
		}
		//Location
		location = user.getLocation();
		//Followers of the user
		followers = (long) user.getFollowersCount();
		//Friends of the user
		friends =  (long) user.getFriendsCount();
		
		
	}
	
}
