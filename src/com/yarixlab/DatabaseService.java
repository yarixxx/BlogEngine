package com.yarixlab;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.yarixlab.exceptions.IncorrectPermalinkException;
import com.yarixlab.exceptions.NotFoundException;
import com.yarixlab.models.TagModel;
import com.yarixlab.models.YearModel;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseService {

    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
    public static final String CONTENT_EXTENSION = ".html";

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
        shouldHavePermalink(permalink);

        Document post = postsCollection.find(Requests.findByPermalink(permalink)).first();
        if (post.get(Requests.POST_TAGS) == null) {
            post.put(Requests.POST_TAGS, Collections.emptyList());
        }
        post.put(Requests.POST_CONTENT, findContentByPermalink(permalink));
        return post;
    }

    public boolean hasPermalink(String permalink) {
        Document post = postsCollection.find(new Document(Requests.POST_PERMALINK, permalink)).first();
        return post != null;
    }

    public void shouldHavePermalink(String permalink) throws NotFoundException {
        if (!hasPermalink(permalink)) {
            throw new NotFoundException("Post not found: " + permalink);
        }
    }

    public void shouldHaveNoPermalink(String permalink) throws NotFoundException {
        if (hasPermalink(permalink)) {
            throw new NotFoundException("Post already exists: " + permalink);
        }
    }

    public List<Document> showPageByDateDescending(int limit, int page, PostStatus status) {
        return findByDateDescending(page, limit, Requests.findByStatus(status));
    }

    public List<Document> showPageByDateDescending(int limit, int page) throws IOException {
        return findByDateDescending(page, limit, new Document());
    }

    public List<Document> findByDateDescending(int position, int limit, Document request) {
        MongoCursor<Document> cursor = postsCollection
                .find(request)
                .sort(Requests.sortByDateDescending())
                .skip(position)
                .limit(limit)
                .iterator();
        List<Document> posts = new ArrayList<Document>();
        while (cursor.hasNext()) {
            Document post = cursor.next();
            post.put(Requests.POST_CONTENT, findContentByPermalink(post.getString(Requests.POST_PERMALINK)));
            posts.add(post);

        }
        return posts;
    }

    public List<Document> listLatestPublishedPosts(int limit) {
        MongoCursor<Document> cursor = findLatestPosts(limit, PostStatus.PUBLISHED);
        return DataConverter.cursorToList(cursor);
    }

    public long getTotalPosts(PostStatus status) {
        if (status == PostStatus.ALL) {
            return postsCollection.count();
        }
        return postsCollection.count(Requests.findByStatus(status));
    }

    private MongoCursor<Document> findLatestPosts(int limit, PostStatus status) {
        return postsCollection
                .find(Requests.findByStatus(status))
                .sort(Requests.sortByDateDescending())
                .limit(limit)
                .iterator();
    }

    public List<TagModel> getTopTags(int tagsNumber) {
        final List<TagModel> allTags = new ArrayList<>();

        MongoCursor<Document> cursor = this.takeTags(tagsNumber, 0).iterator();
        while (cursor.hasNext()) {
            allTags.add(DataConverter.createTagFromDocument(cursor.next()));
        }
        return allTags;
    }

    public List<Document> getTags(int skip, int number) {
        MongoCursor<Document> cursor = takeTags(number, skip).iterator();
        return DataConverter.cursorToList(cursor);
    }

    public List<TagModel> getAllTags() {
        final List<TagModel> allTags = new ArrayList<>();

        MongoCursor<Document> cursor = this.takeAllTags().iterator();
        while (cursor.hasNext()) {
            allTags.add(DataConverter.createTagFromDocument(cursor.next()));
        }
        return allTags;
    }

    public List<Document> findByTag(String tagCode) throws IOException {
        MongoCursor<Document> cursor = postsCollection
                .find(Requests.findByTag(DataConverter.createTagByCode(tagCode).getTagName()))
                .sort(Requests.sortByDateDescending())
                .iterator();
        List<Document> posts = new ArrayList<Document>();
        while (cursor.hasNext()) {
            Document post = cursor.next();
            post.put(Requests.POST_CONTENT, findContentByPermalink(post.getString(Requests.POST_PERMALINK)));
            posts.add(post);
        }

        return posts;
    }

    public List<YearModel> getArchiveByYearsSafe() {
        try {
            return getArchiveByYears();
        } catch (MongoCommandException exception) {
            LOGGER.throwing(DatabaseService.class.getName(), "getArchiveByYearsSafe", exception);
        }
        return Collections.emptyList();
    }

    private List<YearModel> getArchiveByYears() throws MongoException {
        final List<YearModel> allYears = new ArrayList<>();

        MongoCursor<Document> cursor = postsCollection.aggregate(Requests.createRequestPostsNumberByYears()).iterator();
        while (cursor.hasNext()) {
            allYears.add(DataConverter.convertToYear(cursor.next()));
        }
        return allYears;
    }

    public List<Document> getRestArchiveByYears() {
        MongoCursor<Document> cursor = postsCollection.aggregate(Requests.createRequestPostsNumberByYears()).iterator();
        return DataConverter.cursorToList(cursor);
    }

    private AggregateIterable<Document> takeAllTags() {
        return postsCollection.aggregate(Requests.requestAllTags());
    }

    public AggregateIterable<Document> countTags() {
        return postsCollection.aggregate(Requests.countTags());
    }

    private AggregateIterable<Document> takeTags(int limit, int skip) {
        return postsCollection.aggregate(Requests.takeTags(skip, limit));
    }

    public void createPost(String permalink, String title, String date) throws IncorrectPermalinkException, NotFoundException {
        shouldHaveNoPermalink(permalink);
        postsCollection.insertOne(
                    Requests.createPost(title, permalink, DataConverter.convertToDate(date)));
    }

    public void updatePost(String permalink, String newPermalink, String title, String date, String allTags)
            throws IncorrectPermalinkException, NotFoundException {
        boolean isRename = !newPermalink.equals(permalink);
        if (isRename) {
            shouldHavePermalink(permalink);
            shouldHaveNoPermalink(newPermalink);
        } else {
            shouldHavePermalink(permalink);
        }

        postsCollection.updateOne(
                Requests.findByPermalink(permalink),
                Requests.updatePost(
                        newPermalink,
                        title,
                        DataConverter.convertToDate(date),
                        DataConverter.convertTags(allTags)));;
    }

    public void publishPost(String permalink) throws NotFoundException {
        shouldHavePermalink(permalink);
        postsCollection.updateOne(
                Requests.findByPermalink(permalink),
                Requests.updateStatus(PostStatus.PUBLISHED));
    }

    public void revokePost(String permalink) throws NotFoundException {
        shouldHavePermalink(permalink);
        postsCollection.updateOne(
                Requests.findByPermalink(permalink),
                Requests.updateStatus(PostStatus.DRAFT));
    }

    private String findContentByPermalink(String permalink) {
        GridFSDBFile postContent = gridFs.findOne(permalink + CONTENT_EXTENSION);
        if (postContent != null) {
            try {
                return DataConverter.getStringFromInputStream(postContent.getInputStream());
            } catch (IOException e) {
                LOGGER.info("Please, check post=" + permalink);
                return "Text for this post is broken.";
            }
        }
        LOGGER.info("Please, check post=" + permalink);
        return "There is no text for this post.";
    }

    public void saveContentByPermalink(String permalink, BufferedReader content) throws IOException {
        String htmlContent = DataConverter.getStringFromBufferedReader(content);
        InputStream fileStream = new ByteArrayInputStream(htmlContent.getBytes());
        gridFs.remove(permalink + CONTENT_EXTENSION);
        gridFs.createFile(fileStream, permalink + CONTENT_EXTENSION).save();
    }
}
