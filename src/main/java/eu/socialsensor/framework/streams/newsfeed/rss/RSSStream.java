package eu.socialsensor.framework.streams.newsfeed.rss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.socialsensor.framework.common.domain.NewsFeedSource;
import eu.socialsensor.framework.retrievers.newsfeed.rss.RSSRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;


public class RSSStream extends Stream{
	
	public static NewsFeedSource SOURCE = NewsFeedSource.RSS;
	private static final String DATE = "date";
	
	private Date dateToRetrieve = null;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	public void open(StreamConfiguration config){
		
		String date = config.getParameter(RSSStream.DATE);
		
		if(date == null){
			try {
				throw new Exception("No specified date to retrieve from");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		
		try {
			dateToRetrieve = (Date) formatter.parse(date);
			
		} catch (ParseException e) {
			System.err.println("ParseException : "+e);
		}
		
		
		nfRetriever = new RSSRetriever(dateToRetrieve,this);
	
		
	}
	
	
}
