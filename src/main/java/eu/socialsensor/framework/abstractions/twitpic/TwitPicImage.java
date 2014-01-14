package eu.socialsensor.framework.abstractions.twitpic;

import com.google.api.client.util.Key;

/**
 * Class that holds the information regarding the twitpic image
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitPicImage {
	@Key
	public String id, message, tags, short_id, type;
	@Key
	public int views, number_of_comments, height, width;
	@Key
	public String timestamp;
	@Key
	public String user_id, location;
	
	@Key
	public TwitPicUser user;
	
}
