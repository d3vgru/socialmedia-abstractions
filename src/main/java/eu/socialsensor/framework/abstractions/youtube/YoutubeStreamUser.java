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
		super(Source.Type.Youtube.toString(), Operation.NEW);
		if (user == null) return;
		//Id
		id = Source.Type.Youtube+"#"+user;
		//The name of the user
		username = user;
		//streamId
		streamId = Source.Type.Youtube.toString();
	}

	public YoutubeStreamUser(Person user) {
		super(Source.Type.Youtube.toString(), Operation.NEW);
		if (user == null) return;
		
		//Id
		id = Source.Type.Youtube+"#"+user.getName();
		//The id of the user in the network
		userid = user.getName();
		//The name of the user
		username = user.getName();
		//streamId
		streamId = Source.Type.Youtube.toString();
		//The link to the user's profile
		//link = user.getUri();
	}
	
}
