package eu.socialsensor.framework.retrievers.socialmedia;

import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;

/**
 * The interface that represents the retriever for all wrappers
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public interface SocialMediaRetriever extends Retriever {
	
	
	
	/**
	 * Retrieves a keywords feed that contains certain keywords
	 * in order to retrieve relevant multimedia content
	 * @param feed
	 * @return
	 * @throws Exception
	 */
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed) throws Exception;
	
	/**
	 * Retrieves a user feed that contains the user/users in 
	 * order to retrieve multimedia content posted by them
	 * @param feed
	 * @return
	 * @throws Exception
	 */
	public Integer retrieveUserFeeds(SourceFeed feed) throws Exception;
	
	/**
	 * Retrieves a location feed that contains the coordinates of the location
	 * that the retrieved multimedia content must come from.
	 * @param feed
	 * @return
	 * @throws Exception
	 */
	public Integer retrieveLocationFeeds(LocationFeed feed) throws Exception;

	public StreamUser getStreamUser(String uid);
	
	public MediaItem getMediaItem(String id);
	
}
