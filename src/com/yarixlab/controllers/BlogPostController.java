package com.yarixlab.controllers;

import com.yarixlab.DatabaseService;
import com.yarixlab.TemplateFields;
import com.yarixlab.exceptions.NotFoundException;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlogPostController extends BlogController {

    private final static Pattern postPattern = Pattern.compile("/(\\d{4})/(\\d{1,2})/([a-z0-9\\-]+)\\/?$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        Matcher pageMatcher = postPattern.matcher(request.getRequestURI());
        if (!pageMatcher.find()) {
            throw new NotFoundException("Page not found.");
        }

        Integer year = Integer.parseInt(pageMatcher.group(1));
        Integer month = Integer.parseInt(pageMatcher.group(2)) - 1;
        String permalink = pageMatcher.group(3);

        Document post = dataService.findByPermalink(permalink);

        checkValidDate(year, month, post.getDate(DatabaseService.POST_DATE));
        model.put(TemplateFields.POST.toString(), post);
        view.render(response, model);
    }

    private void checkValidDate(Integer year, Integer month, Date pubDate) throws NotFoundException {
        Calendar postDate = Calendar.getInstance();
        postDate.setTime(pubDate);
        if (postDate.get(Calendar.YEAR) != year || postDate.get(Calendar.MONTH) != month) {
            throw new NotFoundException("Post not found.");
        }
    }
}
