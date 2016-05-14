package com.yarixlab.controllers;

import com.yarixlab.PostStatus;
import com.yarixlab.exceptions.IncorrectPermalinkException;
import com.yarixlab.exceptions.NotFoundException;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class AdminPostController extends BlogController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        if ("/admin-rest/tags.json".equals(request.getRequestURI())) {
            showTags(request, response);
        } else if ("/admin-rest/list-by-status.json".equals(request.getRequestURI())) {
            showPostsByStatus(request, response);
        } else if ("/admin-rest/calendar.json".equals(request.getRequestURI())) {
            showCalendar(request, response);
        }
    }

    private void showCalendar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Writer writer = response.getWriter();
        writer.write((new Document("calendar", dataService.getRestArchiveByYears())).toJson());
        writer.close();
    }

    private void showPostsByStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Writer writer = response.getWriter();

        int position = Integer.parseInt(getValue(request.getParameter("position"), "0"));
        int limit = Integer.parseInt(getValue(request.getParameter("limit"), "10"));
        String status = getValue(request.getParameter("status"), "ALL");

        if (PostStatus.DRAFT.name().equals(status)) {
            writer.write(new Document("posts", dataService.showPageByDateDescending(limit, position, PostStatus.DRAFT))
                    .append("total", dataService.getTotalPosts(PostStatus.DRAFT)).toJson());
        } else if (PostStatus.PUBLISHED.name().equals(status)) {
            writer.write(new Document("posts", dataService.showPageByDateDescending(limit, position, PostStatus.PUBLISHED))
                    .append("total", dataService.getTotalPosts(PostStatus.PUBLISHED)).toJson());
        } else if (PostStatus.ALL.name().equals(status)) {
            writer.write(new Document("posts", dataService.showPageByDateDescending(limit, position))
                    .append("total", dataService.getTotalPosts(PostStatus.ALL)).toJson());
        }
        writer.close();
    }

    private void showTags(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Writer writer = response.getWriter();

        int position = Integer.parseInt(getValue(request.getParameter("position"), "0"));
        int limit = Integer.parseInt(getValue(request.getParameter("limit"), "10"));

        writer.write(new Document("tags", dataService.getTags(position, limit))
                .append("total", dataService.countTags().first())
                .toJson());
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        Writer writer = response.getWriter();

        LOGGER.info("doPost=" + request.getRequestURI());
        if ("/admin-rest/savePost.json".equals(request.getRequestURI())) {
            savePost(request);
            writer.write("Updated");
        } else if ("/admin-rest/createPost.json".equals(request.getRequestURI())) {
            createPost(request);
            writer.write("Created");
        } else if ("/admin-rest/publish.json".equals(request.getRequestURI())) {
            publishPost(request);
            writer.write("Published");
        } else if ("/admin-rest/revoke.json".equals(request.getRequestURI())) {
            revokePost(request);
            writer.write("Revoked");
        } else {
            throw new NotFoundException("Page not found.");
        }

        writer.close();
    }

    private void revokePost(HttpServletRequest request) throws NotFoundException {
        String name = request.getParameter("name");
        dataService.revokePost(name);
    }

    private void publishPost(HttpServletRequest request) throws NotFoundException {
        String name = request.getParameter("name");
        dataService.publishPost(name);
    }

    private void createPost(HttpServletRequest request) throws IncorrectPermalinkException, IOException, NotFoundException {
        String name = request.getParameter("name");
        String title = request.getParameter("title");
        String timestamp = request.getParameter("timestamp");
        dataService.createPost(name, title, timestamp);
        dataService.saveContentByPermalink(name, request.getReader());
    }

    private void savePost(HttpServletRequest request) throws IncorrectPermalinkException, IOException, NotFoundException {
        String oldName = request.getParameter("oldName");
        String name = request.getParameter("name");
        String title = request.getParameter("title");
        String allTags = request.getParameter("allTags");
        String timestamp = request.getParameter("timestamp");
        LOGGER.info("savePost=" + oldName + " " + name + " " + title + " " + timestamp);
        dataService.updatePost(oldName, name, title, timestamp, allTags);
        dataService.saveContentByPermalink(name, request.getReader());
    }
}
