package eu.socialsensor.framework.abstractions.gplus;

import com.google.api.services.plus.model.Activity.Actor;


import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the google plus user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class GooglePlusStreamUser extends StreamUser {
	
	public GooglePlusStreamUser(Actor actor) {
		super(Source.Type.GooglePlus.toString(), Operation.NEW_UPDATE);
		if (actor == null) return;
		
		id = Source.Type.GooglePlus + "::"+actor.getId();
		
		userid = actor.getId();
		username = actor.getDisplayName();
		name = actor.getDisplayName();
	
		imageUrl = actor.getImage().getUrl();
		
	}


}
