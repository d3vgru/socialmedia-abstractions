package eu.socialsensor.framework.abstractions.twitter;

import java.util.Date;
import java.util.HashMap;

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
		super(Source.Type.Twitter.toString(), Operation.NEW_UPDATE);
		if (user == null) return;
		
		id = Source.Type.Twitter + "::" + user.getId();
		
		userid = Long.toString(user.getId());
		username = user.getScreenName();
		name = user.getName();
		
		items = user.getStatusesCount();
		imageUrl = user.getOriginalProfileImageURL();
		profileImage = user.getProfileImageURL();
		
		Date date = user.getCreatedAt();
		if(date != null) {
			createdAt = date.toString();
		}
		location = user.getLocation();
		
		popularity = new HashMap<String, Long>();
		
		popularity.put("followers", (long) user.getFollowersCount());
		popularity.put("friends", (long) user.getFriendsCount());
		
		description = user.getDescription();
	}
	
	public static void main(String[] args) {
		
		
	}
}
