package eu.socialsensor.framework.abstractions.tumblr;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the tumblr user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrStreamUser extends StreamUser{
	
	public TumblrStreamUser(String user) {
		super(Source.Type.Tumblr.toString(), Operation.NEW);
		
		username = user;
		name = user;
	}
}
