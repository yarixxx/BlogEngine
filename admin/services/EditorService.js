'use strict';

blogAdminApp.service('editorService', EditorService);

function EditorService() {
    this.postEditorId = 'postEditor';
};

EditorService.prototype.init = function(postEditorId, postEditorWrapper) {
    this.postEditorId = postEditorId;
    this.postEditorWrapper = postEditorWrapper;
};

EditorService.prototype.showEditor = function() {
    var content = document.getElementById(this.postEditorId).value;
    tinymce.init({
        selector: '#' + this.postEditorId,
        plugins: 'code',
        preformatted: true,
        apply_source_formatting: true,
        min_height: 300,
        toolbar: 'code bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image',
        menubar: false,
        statusbar: false,
        content_css: [ '/admin/admin-styles.css', 'https://fonts.googleapis.com/icon?family=Material+Icons']
    });
    tinymce.activeEditor.setContent(content);
    return content;
};


EditorService.prototype.showRawEditor = function() {
    var content = tinyMCE.activeEditor.getContent();
    var container = document.getElementById(this.postEditorWrapper);
    container.innerHTML = '<textarea id="' + this.postEditorId + '">' + content + '</textarea>';
    return content;
};

EditorService.prototype.getContent = function() {
    return !!tinyMCE.activeEditor ? tinyMCE.activeEditor.getContent() : document.getElementById(this.postEditorId).value;
};