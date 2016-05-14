'use strict';

blogAdminApp.service('chartsService', ChartsService);

function ChartsService() {}

ChartsService.prototype.renderTagsDonutChart = function(id, tags, chartWidth, chartHeight) {
    var totalTags = tags.reduce((total, tag) => total + tag.count, 0);
    var modifier = 6.3 / totalTags;

    var color = d3.scale.category20();

    var svg = d3.select(id)
        .append('svg')
        .attr('height', chartHeight)
        .attr('width', chartWidth)
        .append('g')
        .attr('transform', 'translate(200, 200)');

    var position = 0;
    svg.selectAll('path')
        .data(tags)
        .enter()
        .append('path')
        .attr('fill', function(tag, i) {
            return color(i);
        })
        .attr('d', function(tag, i) {
            var value = tag.count * modifier;
            var arc = d3.svg.arc()
                .innerRadius(40)
                .outerRadius(150)
                .startAngle(position)
                .endAngle(position + value);
            position = position + value;
            return arc();
        });

};

ChartsService.prototype.renderTagsBarChart = function(id, tags, chartWidth, chartHeight) {
    var svg = d3.select(id)
        .append('svg')
        .attr('height', chartHeight)
        .attr('width', chartWidth)
        .append('g');


    var tagsValues = tags.map(tag => tag.count);
    var tagsNames = tags.map(tag => tag._id);

    var color = d3.scale.category20();

    var yScale = d3.scale.linear()
        .domain([0, d3.max(tagsValues) + 50])
        .range([0, chartHeight]);

    var xScale = d3.scale.ordinal()
        .domain(tagsNames)
        .rangeBands([0, chartWidth],.2,.5)

    svg.selectAll('rect')
        .data(tags)
        .enter()
        .append('rect')
        .attr('fill', function(tag, i) {
            return color(i);
        })
        .attr('stroke-width', 1)
        .attr('stroke', '#333')
        .attr('x', function(tag, i) {
            return xScale(tag._id);
        })
        .attr('y', function(tag) {
            return chartHeight - yScale(tag.count);
        })
        .attr('width', xScale.rangeBand())
        .attr('height', function(tag) {
            return yScale(tag.count);
        });

    svg.selectAll('text')
        .data(tags)
        .enter()
        .append('text')
        .text(function(tag) {
            return tag._id;
        })
        .attr('transform', function(tag, i) {
            return 'translate(' + (xScale(tag._id) + 5) + ',' + (chartHeight - yScale(tag.count) - 5) + ')rotate(-20)'
        });
};


ChartsService.prototype.renderCalendar = function(id, data, chartWidth, chartHeight) {
    var years = data.calendar;
    years.reverse();
    var yearsNames = data.calendar.map(year => year._id);
    var pubs = data.calendar.map(year => year.count);

    var svg = d3.select(id)
        .append('svg')
        .attr('height', chartHeight + 20)
        .attr('width', chartWidth);

    var yScale = d3.scale.linear()
        .domain([0, d3.max(pubs) + 50])
        .range([chartHeight, 0]);

    var xScale = d3.scale.ordinal()
        .domain(yearsNames)
        .rangeBands([0, chartWidth],.2,.5);



    svg.append('g').selectAll('line')
        .data(years)
        .enter()
        .append('line')
        .attr('x1', function(year, index) {
            return xScale(year._id);
        })
        .attr('y1', function(year, index) {
            return yScale(year.count);
        })
        .attr('x2', function(year, index) {
            if (years[index + 1]) {
                return xScale(years[index + 1]._id);
            }
            return xScale(year._id);
        })
        .attr('y2', function(year, index) {
            if (years[index + 1]) {
                return yScale(years[index + 1].count);
            }
            return yScale(year.count);
        })
        .attr('style', function() {
           return 'stroke-width:1;fill:none;stroke:#000';
        });

    svg.append('g').selectAll('text')
        .data(years)
        .enter()
        .append('text')
        .text(function(year) {
            return year._id + '(' + year.count + ')';
        })
        .attr('transform', function(year, index) {
            return 'translate(' + xScale(year._id) + ',' + (yScale(year.count)) + ')';
        });

    svg.append('g').selectAll('circle')
        .data(years)
        .enter()
        .append('circle')
        .attr('cx', function(year, index) {
            return xScale(year._id);

        })
        .attr('cy', function(year, index) {
            return yScale(year.count);
        })
        .attr('r', 5);

    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient('bottom');

    var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient('right');

    svg.append('g')
        .attr('transform', 'translate(0,' + (chartHeight) + ')')
        .call(xAxis);
    svg.append('g').call(yAxis);
};