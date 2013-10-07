package eu.socialsensor.framework.abstractions.youtube;

import com.google.gdata.data.Person;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the youtube user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class YoutubeStreamUser extends StreamUser {

	public YoutubeStreamUser(String user) {
		super(Source.Type.Youtube.toString(), Operation.NEW_UPDATE);
		if (user == null) return;
		
		id = Source.Type.Youtube+"::"+user;
		
		userid = user;
		username = user;
		
	}

	public YoutubeStreamUser(Person user) {
		super(Source.Type.Youtube.toString(), Operation.NEW_UPDATE);
		if (user == null) return;
		

		id = Source.Type.Youtube+"::"+user.getName();
		
		userid = user.getName();
		username = user.getName();
		
	}
	
}
