package com.yarixlab.controllers;

import com.yarixlab.exceptions.NotFoundException;
import com.yarixlab.TemplateFields;
import com.yarixlab.models.TagModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AllTagsController extends BlogController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        List<TagModel> tags = dataService.getAllTags();
        if (tags.isEmpty()) {
            throw new NotFoundException("There is no tags.");
        }
        model.put(TemplateFields.ALL_TAGS.toString(), tags);
        view.render(response, model);
    }
}
