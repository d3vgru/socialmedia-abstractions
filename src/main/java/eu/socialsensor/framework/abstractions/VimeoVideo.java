package eu.socialsensor.framework.abstractions;

import com.google.api.client.util.Key;
/**
 * Class that holds the information regarding the vimeo video
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class VimeoVideo {
	
	@Key
	public int id;
	@Key
	public String title, url, thumbnail_large, description, tags;
	@Key
	public int stats_number_of_comments, stats_number_of_likes, stats_number_of_plays;
	@Key
	public String upload_date;
	@Key
	public int user_id;
	@Key
	public int height, width;
    //"user_name": "Thomas EID", 
    //"user_portrait_large": "http://b.vimeocdn.com/ps/104/806/1048064_100.jpg", 
    //"user_url": "http://vimeo.com/user1739776", 
}
