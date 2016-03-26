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

public class PostsListController extends BlogController {

    protected final static Pattern pagePattern = Pattern.compile("/page/(\\d{1,5})\\/?$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        int page = -1;

        if ("/".equals(request.getRequestURI())) {
            page = 0;
        }

        Matcher pageMatcher = pagePattern.matcher(request.getRequestURI());
        if (pageMatcher.find()) {
            page = Integer.parseInt(pageMatcher.group(1));
        }

        if (page < 0) {
            throw new NotFoundException("Page not found.");
        }

        List<Document> posts = dataService.findByDateDescending(5, page);
        if (posts.isEmpty()) {
            throw new NotFoundException("There is no posts.");
        }
        model.put(TemplateFields.POSTS.toString(), posts);
        model.put(TemplateFields.PAGE.toString(), page);

        view.render(response, model);
    }
}
