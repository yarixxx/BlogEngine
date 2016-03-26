package com.yarixlab;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.yarixlab.exceptions.NotFoundException;
import com.yarixlab.models.TagModel;
import com.yarixlab.models.YearModel;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class DatabaseService {

    public static final String POSTS_COUNT = "count";
    public static final String TAGS_COUNT = "count";
    private static final String CONTENT_EXTENSION = ".html";

    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
    public static final String POST_CONTENT = "content";
    public static final String POST_DATE = "date";
    public static final String POST_PERMALINK = "name";
    public static final String POST_TAGS = "tags";

    final GridFS gridFs;
    final MongoCollection<Document> postsCollection;
    private MongoClient mongoClient;

    public DatabaseService(String mongoDatabase, String mongoCollection) {
        mongoClient = new MongoClient();
        final MongoDatabase blogDatabase = mongoClient.getDatabase(mongoDatabase);
        postsCollection = blogDatabase.getCollection(mongoCollection);
        gridFs = new GridFS(mongoClient.getDB(mongoDatabase));
    }

    public void disconnect() {
        mongoClient.close();
    }

    public Document findByPermalink(String permalink) throws IOException, NotFoundException {
        LOGGER.info("findByPermalink: " + permalink);
        Document post = postsCollection.find(new Document(POST_PERMALINK, permalink)).first();
        if (post == null) {
            throw new NotFoundException("Post not found");
        }
        post.put(POST_CONTENT, findContentByPermalink(permalink));
        return post;
    }

    public List<Document> findByDateDescending(int limit, int page) throws IOException {
        MongoCursor<Document> cursor = postsCollection
                .find()
                .sort(new Document(POST_DATE, -1))
                .skip(page * limit)
                .limit(limit)
                .iterator();
        List<Document> posts = new ArrayList<Document>();
        while (cursor.hasNext()) {
            Document post = cursor.next();
            post.put(POST_CONTENT, findContentByPermalink(post.getString(POST_PERMALINK)));
            posts.add(post);

        }
        return posts;
    }

    public List<Document> findLatestPosts(int limit) {
        MongoCursor<Document> cursor = postsCollection
                .find()
                .sort(new Document(POST_DATE, -1))
                .limit(limit)
                .iterator();
        List<Document> posts = new ArrayList<Document>();
        while (cursor.hasNext()) {
            Document post = cursor.next();
            posts.add(post);

        }
        return posts;
    }

    public List<TagModel> getTopTags(int tagsNumber) {
        final List<TagModel> allTags = new ArrayList<TagModel>();

        this.takeTop10Tags().forEach(new Block<Document>() {
            public void apply(final Document document) {
                allTags.add(processTag(document));
            }
        });

        return allTags.subList(0, allTags.size() < tagsNumber ? allTags.size() : tagsNumber);
    }

    public List<TagModel> getAllTags() {
        final List<TagModel> allTags = new ArrayList<TagModel>();

        this.takeAllTags().forEach(new Block<Document>() {
            public void apply(final Document document) {
                allTags.add(processTag(document));
            }
        });
        return allTags;
    }

    public List<Document> findByTag(String tagCode) throws IOException {
        Document request = new Document();
        request.put(POST_TAGS, new Document("$elemMatch", new Document("$eq", revertTag(tagCode).getTagName())));

        MongoCursor<Document> cursor = postsCollection
                .find(request)
                .sort(new Document(POST_DATE, -1))
                .iterator();
        List<Document> posts = new ArrayList<Document>();
        while (cursor.hasNext()) {
            Document post = cursor.next();
            post.put(POST_CONTENT, findContentByPermalink(post.getString(POST_PERMALINK)));
            posts.add(post);
        }

        return posts;
    }

    public List<YearModel> getArchiveByYears() {
        Document groupYears = new Document("$group",
                new Document("_id", new Document("$year", "$date"))
                        .append(POSTS_COUNT, new Document("$sum", 1)));
        Document sortYears = new Document("$sort", new Document("_id", -1));
        List<Document> aggrList =  asList(groupYears, sortYears);

        final List<YearModel> allYears = new ArrayList<YearModel>();

        postsCollection.aggregate(aggrList).forEach(new Block<Document>() {
            public void apply(final Document document) {
                YearModel yearModel = new YearModel();
                yearModel.setYear(document.getInteger("_id"));
                yearModel.setCount(document.getInteger(POSTS_COUNT));
                allYears.add(yearModel);
            }
        });

        return allYears;
    }

    private AggregateIterable<Document> takeAllTags() {
        Document unwindTags = new Document("$unwind", "$tags");

        Document groupTags = new Document("$group", new Document("_id", "$tags").append(POSTS_COUNT, new Document("$sum", 1)));

        Document sortTags = new Document("$sort", new Document(TAGS_COUNT, -1));

        List<Document> aggrList = asList(unwindTags, groupTags, sortTags);

        return postsCollection.aggregate(aggrList);
    }

    private AggregateIterable<Document> takeTop10Tags() {
        Document unwindTags = new Document("$unwind", "$tags");

        Document groupTags = new Document("$group", new Document("_id", "$tags").append(TAGS_COUNT, new Document("$sum", 1)));

        Document sortTags = new Document("$sort", new Document(TAGS_COUNT, -1));

        Document limitTags = new Document("$limit", 10);

        List<Document> aggrList = asList(unwindTags, groupTags, sortTags, limitTags);

        return postsCollection.aggregate(aggrList);
    }

    private TagModel processTag(Document document) {
        TagModel tagModel = processTag(document.getString("_id"));
        tagModel.setCount(document.getInteger(TAGS_COUNT));
        return tagModel;
    }

    private TagModel processTag(String tagName) {
        TagModel tag = new TagModel();
        tag.setTagName(tagName);
        tag.setTagCode(tagName.replaceAll("\\s+", "-"));
        return tag;
    }

    private TagModel revertTag(String tagCode) {
        TagModel tag = new TagModel();
        tag.setTagCode(tagCode);
        tag.setTagName(tagCode.replaceAll("-", " "));
        return tag;
    }

    private String findContentByPermalink(String permalink) {
        GridFSDBFile postContent = gridFs.findOne(permalink + CONTENT_EXTENSION);
        if (postContent != null) {
            try {
                return getStringFromInputStream(postContent.getInputStream());
            } catch (IOException e) {
                LOGGER.info("Please, check post=" + permalink);
                return "Text for this post is broken.";
            }
        }
        LOGGER.info("Please, check post=" + permalink);
        return "There is no text for this post.";
    }

    private static String getStringFromInputStream(InputStream is) throws IOException {
        try (InputStreamReader source = new InputStreamReader(is);
             BufferedReader bufferedReader = new BufferedReader(source)) {
            StringBuilder builder = new StringBuilder();
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
            }
            return builder.toString();
        }
    }
}
