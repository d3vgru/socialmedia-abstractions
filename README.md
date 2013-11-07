socialmedia-abstractions
========================


<h3>Social-Media Abstractions contain the necessary classes for mapping a number of social networks to a single representation.</h3>

<h2><u>Getting started</u></h2>
<p>The project includes three set of classes, <strong>Streams</strong>, <strong>Retrievers</strong>, <strong>Abstractions</strong> to be used for retrieving content from several social networks: Twitter, Facebook, Instagram, DailyMotion, Flickr, Twitpic, Google+, Tumblr, Vimeo and Youtube. <strong>Streams</strong> are responsible for setting up of the social stream according to the keys and tokens that the user has provided and creating/closing the connection to the network in order to perform api calls. <strong>Retrievers</strong> are basicaly the wrappers to the social networks APIs, whose job is to perform calls according to the input feed they have received. For example, an input feed maybe a keyword or a set of keywords (<i>keywordsFeed</i>), a social network user to follow (<i>SourceFeed</i>) or the coordinates of the location (<i>LocationFeed</i>). It is important to note that feeds are handled by different threads (each thread perform calls for a one input feed) decreasing thus the time of the retrieval process. Finally, in <strong>Abstractions</strong> the information from the collected content is mapped to a single item representation in order to be stored in the different storages. </p>

<h2><u>Learning more</u></h2>

<p><h4>Stream API Documentation</h4></p>

<ul>
<li>More information regading Twitter API : <a href="http://twitter4j.org/en/">Twitter API</a></li>
<li>More information regading Facebook API : <a href="http://restfb.com/">Facebook API</a></li>
<li>More information regading Instagram API : <a href="https://github.com/sachin-handiekar/jInstagram">Instagram API</a></li>
<li>More information regading Google+ API: <a href="https://developers.google.com/+/quickstart/java">Google+ API</a></li>
<li>More information regading Flickr API: <a href="http://www.flickr.com/services/api/">Flickr API</a></li>
<li>More information regading Tumblr API: <a href="https://github.com/tumblr/jumblr">Tumblr API</a></li>
<li>More information regading Youtube API: <a href="https://developers.google.com/youtube/v3/">Youtube API</a></li>
</ul>
