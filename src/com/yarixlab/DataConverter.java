package com.yarixlab;

import com.mongodb.client.MongoCursor;
import com.yarixlab.models.TagModel;
import com.yarixlab.models.YearModel;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataConverter {

    public static List<String> convertTags(String tags) {
        return Arrays.asList(tags.split("\\s*,\\s*"));
    }

    public static TagModel createTagFromDocument(Document document) {
        TagModel tagModel = createTagByName(document.getString("_id"));
        tagModel.setCount(document.getInteger(Requests.TAGS_COUNT));
        return tagModel;
    }

    public static TagModel createTagByName(String tagName) {
        TagModel tag = new TagModel();
        tag.setTagName(tagName);
        tag.setTagCode(tagName.replaceAll("\\s+", "-"));
        return tag;
    }

    public static TagModel createTagByCode(String tagCode) {
        TagModel tag = new TagModel();
        tag.setTagCode(tagCode);
        tag.setTagName(tagCode.replaceAll("-", " "));
        return tag;
    }

    public static String getStringFromBufferedReader(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(reader.readLine());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            builder.append(System.lineSeparator()).append(line);
        }
        return builder.toString();
    }

    public static String getStringFromInputStream(InputStream is) throws IOException {
        try (InputStreamReader source = new InputStreamReader(is);
             BufferedReader bufferedReader = new BufferedReader(source)) {
            return DataConverter.getStringFromBufferedReader(bufferedReader);
        }
    }

    public static Date convertToDate(String timestamp) {
        return new Date(Long.parseLong(timestamp));
    }

    public static YearModel convertToYear(Document document) {
        YearModel yearModel = new YearModel();
        yearModel.setYear(document.getInteger("_id"));
        yearModel.setCount(document.getInteger(Requests.POSTS_COUNT));
        return yearModel;
    }


    public static List<Document> cursorToList(MongoCursor<Document> cursor) {
        List<Document> documents = new ArrayList<>();
        while (cursor.hasNext()) {
            documents.add(cursor.next());
        }
        return documents;
    }

}
