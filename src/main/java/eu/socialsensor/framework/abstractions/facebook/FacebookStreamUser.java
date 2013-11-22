package eu.socialsensor.framework.abstractions.facebook;

import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;
import com.restfb.types.CategorizedFacebookType;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the facebook user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FacebookStreamUser extends StreamUser {

	
	public FacebookStreamUser(CategorizedFacebookType user) {
		super(Source.Type.Facebook.toString(), Operation.NEW);
		if (user == null) return;
		
		id = Source.Type.Facebook+"::"+user.getId();
		
		userid = user.getId();
		name = user.getName();
		
	}

	public FacebookStreamUser(User user) {
		super(Source.Type.Facebook.toString(), Operation.NEW);
		if (user == null) return;
		
		id = Source.Type.Facebook+"::"+user.getId();
		
		userid = user.getId();
		name = user.getName();
		username = user.getUsername();

		NamedFacebookType loc = user.getLocation();
		if(loc != null) {
			location = loc.getName();
		}
	}
	
}
