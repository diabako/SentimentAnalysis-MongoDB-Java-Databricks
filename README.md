# [DRAFT] Leveraging MongoDB Atlas and Databricks To Perform Reddit Posts Sentiment Analysis - Part 1

A few months back, [Databricks](https://www.databricks.com/) and [MongoDB Inc](https://www.mongodb.com/) announced a seamless integration between the two platforms. Recognizing the potential of these powerful tools, I decided to conduct an experiment with a specific focus on sentiment analysis of social media data. 

The primary objective of this project was to explore and illustrate how MongoDB Atlas and Databricks can be used together for sentiment analysis, specifically with the aim to understand developers' sentiment towards MongoDB, using Reddit as our primary data source. 

This guide will take you through the process of extracting posts from Reddit, storing them into a MongoDB Atlas Time Series collection, performing sentiment analysis on the posts using a pre-trained Textblob model in Databricks, and visualizing the results using MongoDB Atlas Charts. 

## Prerequisites
1. [A Reddit account](https://www.reddit.com/register/)
2. [A MongoDB Atlas account](https://www.mongodb.com/cloud/atlas/register?utm_source=google&utm_campaign=search_gs_pl_evergreen_atlas_general_prosp-brand_gic-null_emea-gb_ps-all_desktop_eng_lead&utm_term=free%20mongo%20db&utm_medium=cpc_paid_search&utm_ad=e&utm_ad_campaign_id=1718986510&adgroup=150907563834&cq_cmp=1718986510&gad=1&gclid=Cj0KCQjwwISlBhD6ARIsAESAmp5wwR447q7_0POXPrUzdISfe81byfDYsshj56cYYHl3G4xmdPQP-REaAsgPEALw_wcB)
3. [A Databricks account](https://www.databricks.com/try-databricks#account)
4. [A Java Maven setup](https://www.mongodb.com/developer/languages/java/java-setup-crud-operations/?utm_campaign=javainsertingdocuments&utm_source=facebook&utm_medium=organic_social)

## High-Level Steps
1. Create a Reddit App for data extraction
2. Set up a MongoDB Atlas cluster
3. Create a Java Maven project
4. Configure Reddit and MongoDB in the Java Maven project
5. Load data into MongoDB Atlas
6. Set up a Databricks workspace and notebook
7. Load data from MongoDB into Databricks using PyMongo
8. Use Textblob for sentiment analysis
9. Store sentiment analysis results back into MongoDB Atlas
10. Visualize sentiment analysis results using MongoDB Atlas Charts

## Detailed Steps

### High Level Architecture

<img width="428" alt="Screenshot 2023-07-01 at 14 35 09" src="https://github.com/diabako/SentimentAnalysis-MongoDB-Java-Databricks/assets/84781155/89a4d739-bc37-4f96-9eff-a62a4d7d26ec">

### 1. Create a Reddit App for Data Extraction
- Create a Reddit Account if you don’t have one already. Make sure to save your username and password as it will be needed.
- Create a Reddit Application: Reddit requires you to create an application in order to use their API.
  - Navigate to https://www.reddit.com/prefs/apps.
  - Click on "Create App" or "Create Another App".
  - Fill in the details: name, App type (choose script), description, about URL, redirect URI (http://localhost:8000 is fine).
  - Click "Create App".
- After creating the app, you will see the app details which include `client_id` and `client_secret`. These will be needed to authenticate your requests.

### 2. Setup MongoDB Atlas Cluster
- For the MongoDB cluster, we will be using a M0 Free Tier MongoDB Cluster from MongoDB Atlas. If you don't have one already, check out the [Get Started with an M0 Cluster blog post](https://www.mongodb.com/developer/products/atlas/free-atlas-cluster/).
- Make sure to use MongoDB V5+
- In the Security tab, add a new IP Allowlist for your laptop's current IP address.

### 3. Setup Java Maven Project
Create a new Java Maven project on your local machine. 
- You can either clone the git repository:

```bash
git clone https://github.com/diabako/SentimentAnalysis-MongoDB-Java-Databricks.git
```

- Or manually create a maven project using your favorite IDE. In the git repository, you will find the source code and pom.xml file with the dependencies needed for this project.

### 4. Configure Reddit and MongoDB in the Java Maven Project
- Configure Reddit API:

```java
// Reddit API credentials
// clientId: This is the unique identifier for your application. Obtain it by creating a new application at: https://www.reddit.com/prefs/apps
String clientId = "<REDDIT_CLIENT_ID>";

// clientSecret: This is the secret key for your application. You get it at the same place where you obtained your clientId.
String clientSecret = "<REDDIT_CLIENT_SECRET>";

// username: This is your personal Reddit username.
String username = "<REDDIT_USERNAME>";

// password: This is your personal Reddit password.
String password = "<REDDIT_PASSWORD>";

// Set up JRAW with your Reddit API credentials
// UserAgent: This is used to identify the application making the request. It's a string made up of 'platform:app ID:version string (by /u/Reddit username)'
UserAgent userAgent = new UserAgent("bot", "com.example.reddit", "v0.1", username);

// Credentials: This bundles your username, password, clientId, and clientSecret into a format that JRAW can use.
Credentials credentials = Credentials.script(username, password, clientId, clientSecret);

// RedditClient: This is your primary interface to Reddit's API. It takes care of network operations, rate limiting, and so on.
RedditClient redditClient = OAuthHelper.automatic(new net.dean.jraw.http.OkHttpNetworkAdapter(userAgent), credentials);
```

- Configure MongoDB Atlas:

```java
// Connect to MongoDB. More details on how to connect to MongoDB can be found [here](https://www.mongodb.com/developer/languages/java/java-setup-crud-operations/?utm_campaign=javainsertingdocuments&utm_source=facebook&utm_medium=organic_social#connecting-with-java).
MongoClient mongoClient = MongoClients.create("<MONGODB_ATLAS_CLUSTER_CONNECTION_STRING>");
MongoDatabase db = mongoClient.getDatabase("<MONGODB_DATABASE>");
```

### 5. Load Data into MongoDB Atlas
- Create a time series collection via the Java helper. More details on MongoDB Time Series collection and how to create and query them can be found [here](https://www.mongodb.com/developer/products/mongodb/new-time-series-collections/#how-to-create-a-time-series-collection):

```java
String collectionName = "<REDDIT_COLLECTION_NAME>";
db.createCollection(collectionName, new CreateCollectionOptions()
        .timeSeriesOptions(new TimeSeriesOptions("created")));

MongoCollection<Document> postsCollection = db.getCollection(collectionName);
```

- Extract and store posts and comments from Reddit:

```java
public static void fetchNewPostsAndComments(RedditClient redditClient, String subredditName, MongoCollection<Document> postsCollection) {
    // Code to fetch and store posts
}

public static List<Document> getCommentsAsDocuments(RedditClient redditClient, String postId) {
    // Code to fetch and store comments
}
```
For a detailed walkthrough of the code, please refer to the source code in the repository.

### 6. Set Up Databricks
- Create a new Databricks workspace or use an existing one.
- In the Databricks workspace, create a new cluster and configure it as per your requirements.
- Create a new notebook in Databricks and configure it to use the Python programming language.
- Install the PyMongo package in your Databricks notebook using the following command:

```python
!pip install pymongo
```
- Install the TextBlob package in your Databricks notebook using the following command:

```python
!pip install textblob
```

- Make sure to retrieve your Databricks cluster IP and add it to MongoDB Atlas Allowlist.

### 7. Load Data from MongoDB into Databricks using PyMongo
- Create a connection string to your MongoDB Atlas cluster:

```python
import pymongo
from pymongo import MongoClient

//dbname is the MONGODB_DATABASE created in step 4
dbname = 'YOUR DATABASE NAME'

//collectionname is the collection created in step 4 and which contains your raw reddit posts
collectionname = 'YOUR COLLECTION NAME'

//sentiment_collectionname is the new collection that will contains the results of the reddit posts sentiment analysis
sentiment_collectionname = 'NAME OF THE NEW COLLECTION FOR RESULTS'

//uri is the MongoDB Atlas connection string 
uri = 'MONGODB ATLAS CONNECTION STRING'

# Connect to the MongoDB Atlas cluster
client = MongoClient(uri)
```

- Create a collection object and load data into a DataFrame:

```python
# Replace <collectionname> with the name of your collection.
collection = client[dbname]['<collectionname>']

# Create a collection object for the result
sentiment_collection = client[dbname][sentiment_collectionname]

```

### 8. Use [Textblob](https://textblob.readthedocs.io/en/dev/index.html) for Sentiment Analysis
- Define a function to perform sentiment analysis on a single Reddit post using Textblob:

```python
def get_sentiment(text):
   from textblob import TextBlob
   blob = TextBlob(text)
   return blob.sentiment.polarity
```

- Perform the sentiment analysis on the data:

```python
# Apply the get_sentiment function to the title, description, and comment columns of the DataFrame to calculate the sentiment score for each Reddit post
df['title_sentiment'] = df['title'].apply(get_sentiment)
df['description_sentiment'] = df['description'].apply(get_sentiment)
df['comment_sentiment'] = df['comments'].apply(lambda comments: sum([get_sentiment(comment['body']) for comment in comments])/len(comments) if comments else None)
```

### 9. Store Sentiment Analysis Results
- After running the sentiment analysis, store the results back into a separate collection in your MongoDB Atlas cluster:

```python
# Convert the DataFrame to a list of dictionaries
results = df.to_dict(orient='records')

# Insert the sentiment analysis results into the new collection
sentiment_collection = client[dbname]['<sentiment_collection>']
sentiment_collection.insert_many(results)
```

For the full code of the sentiment analysis in Databricks, please refer to the file sentiment analysis code in this repository.

### 10. Visualize Sentiment Analysis Results
Use [MongoDB Atlas Charts](https://www.mongodb.com/products/charts) to visualize the sentiment analysis results. You can create various types of charts to understand the sentiment of the Reddit data over time. For example, in this project, we created a chart to visualize the average post sentiment score over time. Here are the steps to build that chart:

- First, log in to MongoDB Atlas and navigate to your cluster.
- Click on the **"Charts"** button.

If you haven't used Atlas Charts before, you might need to set it up first. Just follow the on-screen instructions to get started.

- After setting up Charts, click on the **"Dashboard"** tab, then click on the **"New Dashboard"** button to create a new dashboard. Give it a suitable name related to your project.
- Once your new dashboard is created, click on the **"Add Chart"** button.
- In the **"Data Source"** dropdown, select your sentiment analysis results collection.
- Now, let's create the chart:
   - In the **"Chart Type"** dropdown, select **"Continuous Line"** chart.
   - In the **"X-Axis"** field, select **"created"** from the list and set its type to **"Time Field"**.
   - In the **"Y-Axis"** field, select **"sentiment_score"** (or the appropriate field from your sentiment analysis results). 
   - Finally, for **"Title"**, you can set it to **"Average Sentiment Score"**.

- After you've set up the chart, click on the **"Save"** button. You should see the following chart:

![Average Sentiment Score Over Time](https://github.com/diabako/SentimentAnalysis-MongoDB-Java-Databricks/assets/84781155/f0339964-2462-4c5c-a95f-3089a80ec9c6)

After carefully orchestrating the sentiment analysis using MongoDB Atlas and Databricks, the resulting visualization paints an interesting picture of sentiments towards MongoDB over time. The above line chart shows the sentiment scores, ranging from -1 to 1, plotted over a time axis. A score of 1 represents a very positive sentiment, while -1 signifies a very negative sentiment.

The fluctuating line in the chart captures the essence of public sentiment, showing the highs and lows in the developers' perception towards MongoDB. These shifts in sentiment can be strategically correlated with various events such as marketing campaigns or the release of new MongoDB versions. For example, a noticeable uptick in sentiment could indicate positive reception to a new version release or a successful marketing campaign, while a downturn could point towards the need for improvement or issue resolution.

This can be a powerful tool for MongoDB Inc., offering actionable insights into the effectiveness of their strategies and the impact of their initiatives on the developer community over time.

## Enhancements 

In part 2, I will be working on building a real time version of this project using kafka which would also us to add more social media platforms as well as avoiding some limitations I encountered around the Reddit API rate limit and MongoDB V6 time series collection.
