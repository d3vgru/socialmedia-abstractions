package eu.socialsensor.framework.abstractions.newsfeed.rss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.jsoup.Jsoup;

import com.sun.syndication.feed.synd.SyndEntry;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.NewsFeedSource;

public class RSSDocument extends Item{
	
	public RSSDocument(SyndEntry rssEntry) {
		super(NewsFeedSource.RSS.toString(), Operation.NEW);
		
		if(rssEntry == null)
			return;
		//Id
		id = UUID.randomUUID().toString();
		//The source that the document was retrieved from
		streamId = rssEntry.getAuthor();
		//Document's title
		title = rssEntry.getTitle();
		//Document's content - Extract text content from html structure
		description = extractDocumentContent(rssEntry.getDescription().getValue());
		//Document's time of publication
		publicationTime = rssEntry.getPublishedDate().getTime();
		//The url where the document can be found
		URL url = null;
		try {
			url = new URL(rssEntry.getLink());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(url != null){
			links = new URL[1];
			links[0] = url;
		}

	}
	
	private String extractDocumentContent(String htmlContent){
		org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
		
		String content = doc.body().text();
		
		return content;
	}
}
