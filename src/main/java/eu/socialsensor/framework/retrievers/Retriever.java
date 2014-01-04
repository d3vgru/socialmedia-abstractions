package eu.socialsensor.framework.retrievers;


import eu.socialsensor.framework.common.domain.Feed;

public interface Retriever {
	/**
	 * Retrieve a general feed that is inserted into the system
	 * @param feed
	 * @return
	 */
	public Integer retrieve(Feed feed);
	
	/**
	 * Stops the retriever
	 * @param 
	 * @return
	 */
	public void stop();
}
