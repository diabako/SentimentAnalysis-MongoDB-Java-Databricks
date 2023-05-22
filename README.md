# [DRAFT] Leveraging MongoDB Atlas and Databricks To Perform Reddit Posts Sentiment Analysis - Part 1

This guide will take you through the process of extracting posts from Reddit, storing them into a MongoDB Atlas time series collection, performing sentiment analysis on the posts using a pre-trained Textblob model in Databricks, and visualizing the results using MongoDB Atlas Charts. 

## Prerequisites
1. A Reddit account
2. A MongoDB Atlas account
3. A Databricks account
4. A Java Maven setup

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
- In the Security tab, add a new IP Allowlist for your laptop's current IP address.

### 3. Setup Java Maven Project
Create a new Java Maven project on your local machine. 
- You can either clone the git repository:

```bash
gh repo clone diabako/SentimentAnalysis-MongoDB-Java-Databricks
```

- Or manually create a maven project using your favorite IDE. In the git repository, you will find the source code and pom.xml file with the dependencies needed for this project.

### 4. Configure Reddit and MongoDB in the Java Maven Project
- Configure Reddit API:

```java
// Reddit API credentials
String clientId = "<REDDIT_CLIENT_ID>";
String clientSecret = "<REDDIT_CLIENT_SECRET>";
String username = "<REDDIT_USERNAME>";
String password = "<REDDIT_PASSWORD>";

// Set up JRAW with your Reddit API credentials
UserAgent userAgent = new UserAgent("bot", "com.example.reddit", "v0.1", username);
Credentials credentials = Credentials.script(username, password, clientId, clientSecret);
RedditClient redditClient = OAuthHelper.automatic(new net.dean.jraw.http.OkHttpNetworkAdapter(userAgent), credentials);
```

- Configure MongoDB Atlas:

```java
// Connect to MongoDB
MongoClient mongoClient = MongoClients.create("<MONGODB_ATLAS_CLUSTER_CONNECTION_STRING>");
MongoDatabase db = mongoClient.getDatabase("<MONGODB_DATABASE>");
```

### 5. Load Data into MongoDB Atlas
- Create a time series collection via the Java helper:

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

dbname = 'YOUR DATABASE NAME'
collectionname = 'YOUR COLLECTION NAME'
sentiment_collectionname = 'NAME OF THE NEW COLLECTION FOR RESULTS'
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

### 8. Use Textblob for Sentiment Analysis
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
Use MongoDB Atlas Charts to visualize the sentiment analysis results. You can create various types of charts to understand the sentiment of the Reddit data over time. For example, in this project, we created a chart to visualize the average post sentiment score over time. Here are the steps to build that chart:

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

## Enhancements 

In part 2, I will be working on building a real time version of this project using kafka which would also us to add more social media platforms as well as avoiding some limitations I encountered around the Reddit API rate limit and MongoDB V6 time series collection.
