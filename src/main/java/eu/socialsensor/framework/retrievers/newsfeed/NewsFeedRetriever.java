package eu.socialsensor.framework.retrievers.newsfeed;

import java.util.List;

import eu.socialsensor.framework.common.domain.Document;

public interface NewsFeedRetriever {
	
	public List<Document> retrieve(String url);
	
}
