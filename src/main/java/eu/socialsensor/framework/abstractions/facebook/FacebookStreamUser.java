package eu.socialsensor.framework.abstractions.facebook;

import com.restfb.types.Location;
import com.restfb.types.Page;
import com.restfb.types.User;


import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;

/**
 * Class that holds the information regarding the facebook user
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FacebookStreamUser extends StreamUser {

	
	public FacebookStreamUser(User user) {
		super(Source.Type.Facebook.toString(), Operation.NEW);
		
		if (user == null) return;
		
		//Id
		id = Source.Type.Facebook+"#"+user.getId();
		//The id of the user in the network
		userid = user.getId();
		//The name of the user
		if(user.getMiddleName() != null)
			name = user.getFirstName()+" "+user.getMiddleName()+" "+user.getLastName();
		else
			name = user.getFirstName()+" "+user.getLastName();
		//The username of the user
		username = user.getUsername();
		//streamId
		streamId =  Source.Type.Facebook.toString();
		//The description of the user
		description = user.getAbout();
		//The link to the user's profile
		linkToProfile = user.getLink(); 
		//Last time user's profile was updated
		if(user.getUpdatedTime() != null)
			lastUpdated = user.getUpdatedTime().getTime();
		//Location
		if(user.getLocation()!= null)
			location = user.getLocation().getName();
		//Is the user a verified user
		//isVerified = user.getVerified();
		
	}

	public FacebookStreamUser(Page page) {
		super(Source.Type.Facebook.toString(), Operation.NEW);
		if (page == null) return;
		
		//Id
		id = Source.Type.Facebook+"#"+page.getId();
		//The id of the page in the network
		userid = page.getId();
		//The name of the page
		name = page.getName();
		//The username of the page
		username = page.getUsername();
		//The name of the Social Network
		streamId = Source.Type.Facebook.toString();
		//The description of the page
		description = page.getAbout();
		//Link to the page
		linkToProfile = page.getLink();
		//Avatar of the page
		//TO DO : MISSING FROM THE API
		profileImage = page.getPicture();
		//Likes of the page
		//likes = page.getLikes();
		//Number of people talking about the page
		followers = page.getTalkingAboutCount();
		//Location 
		Location loc = page.getLocation();
		if(loc != null) {
			location = loc.getCity();
		}
		//Category of the page
		//category = new Category(page.getCategory());
		
	}
	
}
