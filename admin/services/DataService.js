'use strict';

blogAdminApp.service('dataService', BlogEngineService);

function BlogEngineService($http) {
    this.http = $http;
};

BlogEngineService.prototype.createPost = function(newPost) {
    return this.http.post('/admin-rest/createPost.json', '', {
        params: newPost
    });
};

BlogEngineService.prototype.savePost = function(post, content) {
    console.log('savePost', post);
    return this.http.post('/admin-rest/savePost.json', content, {
        params: post
    });
};

BlogEngineService.prototype.publishPost = function(name) {
    return this.http.post('/admin-rest/publish.json', '', {
        params: {
            name: name
        }
    });
};

BlogEngineService.prototype.revokePost = function(name) {
    return this.http.post('/admin-rest/revoke.json', '', {
        params: {
            name: name
        }
    });
};

BlogEngineService.prototype.listTags = function(limit, position) {
    return this.http.get('/admin-rest/tags.json?limit=' + limit + '&position=' + position);
};

BlogEngineService.prototype.getCalendar = function() {
    return this.http.get('/admin-rest/calendar.json');
};

BlogEngineService.prototype.loadPosts = function(pageOffset, pageSize, status) {
    return this.http.get('/admin-rest/list-by-status.json', {
        params: {
            position: pageOffset,
            limit: pageSize,
            status: status
        }
    });
};