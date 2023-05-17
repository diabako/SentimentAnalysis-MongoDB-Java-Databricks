import com.mongodb.client.MongoClient;
        import com.mongodb.client.MongoClients;
        import com.mongodb.client.MongoDatabase;
        import com.mongodb.client.MongoCollection;
        import com.mongodb.client.model.CreateCollectionOptions;
        import com.mongodb.client.model.TimeSeriesOptions;
        import net.dean.jraw.RedditClient;
        import net.dean.jraw.http.UserAgent;
        import net.dean.jraw.models.Comment;
        import net.dean.jraw.models.Submission;
        import net.dean.jraw.models.SubredditSort;
        import net.dean.jraw.oauth.Credentials;
        import net.dean.jraw.oauth.OAuthHelper;
        import net.dean.jraw.pagination.DefaultPaginator;
        import net.dean.jraw.references.SubmissionReference;
        import net.dean.jraw.tree.CommentNode;
        import net.dean.jraw.tree.RootCommentNode;
        import net.dean.jraw.models.PublicContribution;
        import org.bson.Document;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Timer;
        import java.util.TimerTask;
        import java.util.Iterator;

public class RedditPostsToMongoDB {
    public static void main(String[] args) {
        // Reddit API credentials
        String clientId = "<REDDIT_CLIENT_ID>";
        String clientSecret = "<REDDIT_CLIENT_SECRET>";
        String username = "<REDDIT_USERNAME>";
        String password = "s<REDDIT_PASSWORD>";

        // Set up JRAW with your Reddit API credentials
        UserAgent userAgent = new UserAgent("bot", "com.example.reddit", "v0.1", "your_reddit_username");
        Credentials credentials = Credentials.script(username, password, clientId, clientSecret);
        RedditClient redditClient = OAuthHelper.automatic(new net.dean.jraw.http.OkHttpNetworkAdapter(userAgent), credentials);

        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("<MONGODB_ATLAS_CLUSTER_CONNECTION_STRING>");
        MongoDatabase db = mongoClient.getDatabase("<MONGODB_DATABASE>");

        // Create a time series collection
        String collectionName = "<REDDIT_COLLECTION_NAME>";
        db.createCollection(collectionName, new CreateCollectionOptions()
                .timeSeriesOptions(new TimeSeriesOptions("created")));

        MongoCollection<Document> postsCollection = db.getCollection(collectionName);

        // Define the subreddit you want to fetch posts from, in the blog, we used mongodb as a topic
        String subredditName = "<REDDIT_TOPIC>";

        // Schedule a task to fetch new posts and comments every 2 minutes
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchNewPostsAndComments(redditClient, subredditName, postsCollection);
            }
        }, 0, 120 * 1000);
    }

    public static void fetchNewPostsAndComments(RedditClient redditClient, String subredditName, MongoCollection<Document> postsCollection) {
        // Fetch new posts from the specified subreddit
        DefaultPaginator<Submission> paginator = redditClient.subreddit(subredditName).posts().sorting(SubredditSort.NEW).limit(200).build();

        // Iterate through the fetched posts and store them in the MongoDB time series collection along with their comments
        for (Submission post : paginator.next()) {
            // Get the reddit post ID
            String postId = post.getId();

            // Check if the post already exists in the collection
            Document existingPost = postsCollection.find(new Document("id", postId)).first();

            if (existingPost != null) {
                System.out.println("Post already exists: " + post.getTitle());
                continue;
            }

            // Create a new document for the post and its comments
            Document postDocument = new Document()
                    .append("id", post.getId())
                    .append("title", post.getTitle())
                    .append("author", post.getAuthor())
                    .append("score", post.getScore())
                    .append("permalink", post.getPermalink())
                    .append("url", post.getUrl())
                    .append("description", post.getSelfText())
                    .append("created", post.getCreated())
                    .append("comments", getCommentsAsDocuments(redditClient, post.getId()));

            // Insert the new post document into the collection
            postsCollection.insertOne(postDocument);

            System.out.println("Stored post and comments: " + post.getTitle());
        }
    }





    public static List<Document> getCommentsAsDocuments(RedditClient redditClient, String postId) {
        List<Document> comments = new ArrayList<>();

        // Fetch comments for the post
        SubmissionReference submissionRef = redditClient.submission(postId);
        RootCommentNode root = submissionRef.comments();

        // Iterate through the comments and add them to the list
        Iterator<? extends CommentNode<? extends PublicContribution<?>>> iterator = root.walkTree().iterator();
        while (iterator.hasNext()) {
            CommentNode<? extends PublicContribution<?>> commentNode = iterator.next();
            PublicContribution<?> publicContribution = commentNode.getSubject();

            if (publicContribution instanceof Comment) {
                Comment comment = (Comment) publicContribution;
                Document commentData = new Document()
                        .append("id", comment.getId())
                        .append("author", comment.getAuthor())
                        .append("body", comment.getBody())
                        .append("score", comment.getScore())
                        .append("created", comment.getCreated());

                comments.add(commentData);
            }
        }

        return comments;
    }

}