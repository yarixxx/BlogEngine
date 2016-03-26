package com.yarixlab;

import com.yarixlab.exceptions.InvalidTemplateException;
import com.yarixlab.exceptions.NotFoundException;
import freemarker.template.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class BlogView {
    private static final String TEMPLATES_EXTENSION = ".ftl";

    protected Configuration configuration;
    protected Template template;

    public BlogView() {
        configuration = new Configuration();
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
    }

    public BlogView setTemplatePath(String templatePath) throws IOException {
        File path = new File(templatePath);
        configuration.setDirectoryForTemplateLoading(path);
        return this;
    }

    public BlogView setTemplate(String templateName) throws IOException {
        template = configuration.getTemplate(templateName + TEMPLATES_EXTENSION);
        return this;
    }

    public void render(HttpServletResponse response, SimpleHash model)
            throws IOException, NotFoundException, InvalidTemplateException {
        PrintWriter writer = response.getWriter();
        if (template == null) {
            throw new NotFoundException("Template not found.");
        }

        try {
            template.process(model, writer);
        } catch (TemplateException e) {
            writer.close();
            throw new InvalidTemplateException("Template is invalid.");
        }
    }
}
