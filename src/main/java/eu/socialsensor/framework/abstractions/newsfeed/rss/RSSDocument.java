package eu.socialsensor.framework.abstractions.newsfeed.rss;

import java.util.UUID;

import org.jsoup.Jsoup;

import com.sun.syndication.feed.synd.SyndEntry;

import eu.socialsensor.framework.common.domain.Document;
import eu.socialsensor.framework.common.domain.NewsFeedSource;

public class RSSDocument extends Document{
	
	public RSSDocument(SyndEntry rssEntry) {
		super(NewsFeedSource.RSS.toString(), Operation.NEW);
		
		if(rssEntry == null)
			return;
		//Id
		id = UUID.randomUUID().toString();
		//The source that the document was retrieved from
		sourceId = rssEntry.getAuthor();
		//Document's title
		title = rssEntry.getTitle();
		//Document's content - Extract text content from html structure
		content = extractDocumentContent(rssEntry.getDescription().getValue());
		//Document's time of publication
		publicationTime = rssEntry.getPublishedDate().getTime();
		//The url where the document can be found
		url = rssEntry.getLink();

	}
	
	private String extractDocumentContent(String htmlContent){
		org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
		
		String content = doc.body().text();
		
		return content;
	}
}
