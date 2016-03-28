package com.yarixlab.controllers;


import com.yarixlab.exceptions.NotFoundException;
import com.yarixlab.TemplateFields;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagController extends BlogController {

    private final static Pattern tagPattern = Pattern.compile("/tag/([a-zA-Z0-9\\-]+)\\/?$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        Matcher tagMatcher = tagPattern.matcher(request.getRequestURI());
        if (!tagMatcher.find()) {
            throw new NotFoundException("Page not found.");
        }
        String tag = tagMatcher.group(1);

        List<Document> posts = dataService.findByTag(tag);
        if (posts.isEmpty()) {
            throw new NotFoundException("There is no posts for this tag.");
        }

        model.put(TemplateFields.TAG.toString(), tag);
        model.put(TemplateFields.POSTS.toString(), posts);
        view.render(response, model);
    }
}
