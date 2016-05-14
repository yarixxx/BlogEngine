package com.yarixlab;

import org.bson.Document;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

public class Requests {

    public static final String POSTS_COUNT = "count";
    public static final String TAGS_COUNT = "count";

    public static final String POST_TITLE = "title";
    public static final String POST_CONTENT = "content";
    public static final String POST_DATE = "date";
    public static final String POST_PERMALINK = "name";
    public static final String POST_TAGS = "tags";
    public static final String POST_STATUS = "status";

    public static List<Document> requestAllTags() {
        Document unwindTags = new Document("$unwind", "$tags");
        Document groupTags = new Document("$group", new Document("_id", "$tags").append(POSTS_COUNT, new Document("$sum", 1)));
        Document sortTags = new Document("$sort", new Document(TAGS_COUNT, -1));

        return asList(unwindTags, groupTags, sortTags);
    }

    public static List<Document> countTags() {
        Document unwindTags = new Document("$unwind", "$tags");
        Document groupTags = new Document("$group", new Document("_id", null).append(TAGS_COUNT, new Document("$sum", 1)));

        return asList(unwindTags, groupTags);
    }

    public static List<Document> takeTags(int skip, int limit) {
        Document unwindTags = new Document("$unwind", "$tags");
        Document groupTags = new Document("$group", new Document("_id", "$tags").append(TAGS_COUNT, new Document("$sum", 1)));
        Document sortTags = new Document("$sort", new Document(TAGS_COUNT, -1));
        Document skipTags = new Document("$skip", skip);
        Document limitTags = new Document("$limit", limit);

        return asList(unwindTags, groupTags, sortTags, skipTags, limitTags);
    }

    public static Document findByPermalink(String permalink) {
        return new Document(POST_PERMALINK, permalink);
    }

    public static Document updateStatus(PostStatus status) {
        return new Document("$set", new Document(POST_STATUS, status.name()));
    }

    public static Document createPost(String permalink, String title, Date date) {
        return newPost(permalink, title, date)
                .append(POST_STATUS, PostStatus.DRAFT.name());
    }

    public static Document updatePost(String newPermalink, String title, Date date, List<String> tags) {
        return new Document("$set", newPost(newPermalink, title, date)
                .append(POST_TAGS, tags));
    }

    public static Document newPost(String newPermalink, String title, Date date) {
        return new Document(POST_TITLE, title)
                .append(POST_PERMALINK, newPermalink)
                .append(POST_DATE, date);
    }

    public static List<Document> createRequestPostsNumberByYears() {
        Document groupYears = new Document("$group",
                new Document("_id", new Document("$year", "$date"))
                        .append(POSTS_COUNT, new Document("$sum", 1)));
        Document sortYears = new Document("$sort", new Document("_id", -1));
        return asList(groupYears, sortYears);
    }

    public static Document findByTag(String tagName) {
        Document request = new Document();
        request.put(POST_TAGS, new Document("$elemMatch", new Document("$eq", tagName)));
        return request;
    }

    public static Document sortByDateDescending() {
        return new Document(Requests.POST_DATE, -1);
    }

    public static Document findByStatus(PostStatus status) {
        return new Document(Requests.POST_STATUS, status.name());
    }
}
