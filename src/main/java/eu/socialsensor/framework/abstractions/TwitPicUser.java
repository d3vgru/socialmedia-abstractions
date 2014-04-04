package eu.socialsensor.framework.abstractions;

import com.google.api.client.util.Key;

/**
 * Class that holds the information regarding the twitpic image
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitPicUser {
	@Key
	public String id, username, name, bio, avatar_url, timestamp, location;
	@Key
	public int photo_count;
}
