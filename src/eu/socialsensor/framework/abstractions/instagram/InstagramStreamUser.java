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
		super(Source.Type.Instagram.toString(), Operation.NEW_UPDATE);
		if (user == null) return;
		
		id = Source.Type.Instagram + "::" + user.getId();
		
		userid ="";
		userid += user.getId();
		username = user.getUserName();
		name = user.getFullName();
		
	}
}
