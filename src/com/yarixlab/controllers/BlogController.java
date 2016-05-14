package com.yarixlab.controllers;

import com.yarixlab.BlogView;
import com.yarixlab.ContextParameters;
import com.yarixlab.DatabaseService;
import com.yarixlab.TemplateFields;
import freemarker.template.SimpleHash;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class BlogController extends HttpServlet {

    protected static final Logger LOGGER = Logger.getLogger(BlogController.class.getName());
    protected static final String DEFAULT_DATABASE = "test";
    protected static final String DEFAULT_COLLECTION = "posts";

    protected SimpleHash model;
    protected DatabaseService dataService;
    protected BlogView view;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();

        String mongoDatabase = getContextParameter(context,
                ContextParameters.DATA_BASE.toString(),
                DEFAULT_DATABASE);
        String mongoCollection = getContextParameter(context,
                ContextParameters.COLLECTION.toString(),
                DEFAULT_COLLECTION);
        dataService = new DatabaseService(mongoDatabase, mongoCollection);

        String templatePath = getContextParameter(context, ContextParameters.TEMPLATES_PATH.toString(), "");
        String templateName = getConfigParameter(config, ContextParameters.TEMPLATE.toString(), config.getServletName());
        view = new BlogView();
        try {
            view.setTemplatePath(templatePath).setTemplate(templateName);
        } catch (IOException e) {
            LOGGER.info("Controller cannot be initialized.");
            e.printStackTrace();
        }

        model = new SimpleHash();
        model.put(TemplateFields.BLOG_TITLE.toString(), context.getInitParameter(ContextParameters.BLOG_TITLE.toString()));
        model.put(TemplateFields.LATEST_POSTS.toString(), dataService.listLatestPublishedPosts(5));
        model.put(TemplateFields.TOP_TAGS.toString(), dataService.getTopTags(5));
        model.put(TemplateFields.ARCHIVE.toString(), dataService.getArchiveByYearsSafe());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=utf-8");
    }

    @Override
    public void destroy() {
        dataService.disconnect();
    }

    private String getContextParameter(ServletContext context, String parameter, String defaultValue) {
        String parameterValue = context.getInitParameter(parameter);
        return parameterValue == null ? defaultValue : parameterValue;
    }

    private String getConfigParameter(ServletConfig config, String parameter, String defaultValue) {
        String parameterValue = config.getInitParameter(parameter);
        return parameterValue == null ? defaultValue : parameterValue;
    }

    protected String getValue(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }
}
