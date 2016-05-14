'use strict';

blogAdminApp.service('dynamicPostsService', DynamicPosts);

function DynamicPosts(dataService) {
    this.dataService = dataService;
    this.loadedPages = {};
    this.inProgress = false;
    this.numItems = 300;
    this.PAGE_SIZE = 30;
    this.status = 'ALL';
};

DynamicPosts.prototype.setStatus = function(status) {
    this.status = status;
    this.loadedPages = {};
};

DynamicPosts.prototype.setLength = function(length) {
    this.numItems = length;
};

DynamicPosts.prototype.getItemAtIndex = function(index) {
    var pageNumber = Math.floor(index / this.PAGE_SIZE);
    var page = this.loadedPages[pageNumber];

    if (page) {
        return page[index % this.PAGE_SIZE];
    } else {
        if (!this.inProgress) {
            this.fetchPage_(pageNumber);
        }
    }
};

DynamicPosts.prototype.fetchPage_ = function(pageNumber) {
    this.inProgress = true;

    var pageOffset = pageNumber * this.PAGE_SIZE;
    this.dataService.loadPosts(pageOffset, this.PAGE_SIZE, this.status).then(function(response) {
        if (response.data && response.data.posts) {
            response.data.posts.forEach(function (post) {
                post.date = post.date ? new Date(post.date.$date) : new Date();

                post.tags = post.tags || [];
            });
            this.loadedPages[pageNumber] = response.data.posts;
            this.inProgress = false;

            this.numItems = response.data['total']['$numberLong'] * 1;
        }
    }.bind(this));
};

DynamicPosts.prototype.getLength = function() {
    return this.numItems;
};