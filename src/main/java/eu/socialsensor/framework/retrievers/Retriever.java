package eu.socialsensor.framework.retrievers;

import java.util.List;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;

public interface Retriever {
	/**
	 * Retrieve a general feed that is inserted into the system
	 * @param feed
	 * @return
	 */
	public List<Item> retrieve(Feed feed);
	
	/**
	 * Stops the retriever
	 * @param 
	 * @return
	 */
	public void stop();
}
