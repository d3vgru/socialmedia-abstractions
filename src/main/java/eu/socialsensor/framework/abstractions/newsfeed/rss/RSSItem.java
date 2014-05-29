package eu.socialsensor.framework.abstractions.newsfeed.rss;

import java.util.UUID;

import org.jsoup.Jsoup;

import com.sun.syndication.feed.synd.SyndEntry;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.NewsFeedSource;

public class RSSItem extends Item{
	
	public RSSItem(SyndEntry rssEntry) {
		super(NewsFeedSource.RSS.toString(), Operation.NEW);
		
		if(rssEntry == null || rssEntry.getLink() == null)
			return;
		//Id
		id = rssEntry.getLink();
		//Document's title
		title = rssEntry.getTitle();
		//Document's content - Extract text content from html structure
		if(rssEntry.getDescription()!=null)
			description = extractDocumentContent(rssEntry.getDescription().getValue());
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
