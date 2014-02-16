package eu.socialsensor.framework.abstractions.tumblr;

import com.tumblr.jumblr.types.Blog;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the tumblr user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrStreamUser extends StreamUser{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2097707570740315050L;

	public TumblrStreamUser(Blog blog) {
		super(SocialNetworkSource.Tumblr.toString(), Operation.NEW);
		
		//Id
		id = SocialNetworkSource.Tumblr + "#"+blog.getName();
		//The id of the user in the network
		userid = blog.getName();
		//The name of the blog
		name = blog.getName();
		//streamId
		streamId = SocialNetworkSource.Tumblr.toString();
		//The description of the blog
		blog.getDescription();
		//Profile picture of the blog
		profileImage = blog.avatar();
		//Likes of the blog
		//likes = blog.getLikeCount();
		//Posts of the blog
		items = blog.getPostCount();
		
	}
}
