'use strict';

blogAdminApp.controller('HomeCtrl', function($scope, $mdDialog, $mdToast, $mdSidenav, dataService, dynamicPostsService, chartsService, editorService) {

    this.posts = [];

    this.chartsService = chartsService;
    this.dataService = dataService;
    this.editorService = editorService;

    this.notification = $mdToast;

    this.editorService.init('postEditor', 'postEditorWrapper');

    this.tags = [];

    this.template = 'welcome.html';
    this.postStatusFilter = 'ALL';

    this.selectedPost;
    this.dynamicPosts = dynamicPostsService;

    this.isReading = true;


    this.toggleLeftSideNav = function() {
        $mdSidenav('left').toggle();
        this.dynamicPosts = new DynamicPosts($http);
    };

    this.savePost = function() {
        console.log(this.selectedPost);
        var postParams = {
            name: this.selectedPost.name,
            oldName: this.selectedPost.oldName,
            title: this.selectedPost.title,
            allTags: this.selectedPost.tags.join(),
            timestamp: this.selectedPost.date.getTime()
        };
        if (!this.isReading) {
            this.selectedPost.content = this.editorService.getContent();
        }
        if (this.selectedPost.content != '') {
            this.dataService.savePost(postParams, this.selectedPost.content).then(function(response) {
                this.notification.show(this.notification.simple().textContent(response.data).hideDelay(3000));
            }.bind(this)).then(function(response) {
                this.selectedPost.oldName = this.selectedPost.name;

            }.bind(this));
        }
    };

    this.createPost = function() {
        var newPost;
        var createPostDialog = $mdDialog.prompt()
            .title('Create new post')
            .ariaLabel('permalink')
            .placeholder('permalink')
            .ok('Create')
            .cancel('Cancel');
        $mdDialog.show(createPostDialog).then(function(permalink) {
            newPost = {
                name: permalink,
                title: permalink,
                timestamp: (new Date()).getTime(),
                date: new Date(),
                content: permalink,
                tags: []
            };
            this.dataService.createPost(newPost)
        }).then(function() {
            if (newPost) {
                this.selectPost(newPost);
            }
        }.bind(this)).then(function(response) {
            this.notification.show(this.notification.simple().textContent(response.data).hideDelay(3000));
        }.bind(this));;
    };

    this.selectPost = function(post) {
        if (this.template != 'post.html') {
            this.template = 'post.html';
        }
        this.selectedPost = post;
        this.selectedPost.oldName = this.selectedPost.name;
        $mdSidenav('left').close();
    };

    this.switchRawHtml = function() {
        if (!this.isRawHtml) {
            this.editorService.showRawEditor();
        } else {
            this.selectedPost.content = this.editorService.showEditor();
        }
    };

    this.publishPost = function() {
        this.selectedPost.status = 'PUBLISHED';
        this.dataService.publishPost(this.selectedPost.name).then(function(response) {
            this.notification.show(this.notification.simple().textContent(response.data).hideDelay(3000));
        }.bind(this));
    };

    this.revokePost = function() {
        this.selectedPost.status = 'DRAFT';
        this.dataService.revokePost(this.selectedPost.name).then(function(response) {
            this.notification.show(this.notification.simple().textContent(response.data).hideDelay(3000));
        }.bind(this));
    };

    this.filterPosts = function() {
        this.dynamicPosts.setStatus(this.postStatusFilter);
    };

    this.dataService.listTags(10, 0).then(function(response) {
        this.tags = response.data.tags;
        this.dataService.getCalendar().then(function(response) {
            this.chartsService.renderTagsBarChart('#tags-bar-chart', this.tags, 400, 400);
            this.chartsService.renderTagsDonutChart('#tags-donut-chart', this.tags, 400, 400);
            this.chartsService.renderCalendar('#publish-dynamics', response.data, 400, 400);
        }.bind(this));
    }.bind(this));
});