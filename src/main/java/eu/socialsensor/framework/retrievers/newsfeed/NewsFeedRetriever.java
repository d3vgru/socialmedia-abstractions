package eu.socialsensor.framework.retrievers.newsfeed;


public interface NewsFeedRetriever{
	
	public void retrieve(String url);
		
	public void stop();
}
