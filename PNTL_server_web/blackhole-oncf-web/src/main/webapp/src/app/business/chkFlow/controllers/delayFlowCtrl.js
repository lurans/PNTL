define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/delayFlowFixture"],
    function (i18n,commonException,Step, _StepDirective, ViewMode) {
        "use strict";

        var delayFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "delayFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, delayFlowServ) {
                $scope.i18n = i18n;

                $scope.button = {
                    "id":"resetBtn_id",
                    "text" : i18n.chkFlow_term_reset_btn,
                };

                function getAbsPoint(e)
                {
                    var x = e.offsetLeft, y = e.offsetTop;
                    while (e = e.offsetParent) {
                        x += e.offsetLeft;
                        y += e.offsetTop;
                    }
                    return {
                        x: x,
                        y: y
                    }
                }

                var delay_time_chart_pos = getAbsPoint(document.getElementById('delay_time_chart_wrapper'));

                var delay_info_chart = function()
                {
                    //svg
                    var margin = {top: 10, right: 10, bottom: 50, left: 10};
                    var width = 640,height = 640;
                    var colors = ['#79CD79', '#92DD92', '#B8EFB8', '#FFFFB9', '#FFED97', '#FFBB77', '#FFA042', '#FF8000', '#FF2D2D', '#EA0000', '#AE0000'];
                    var delay_time_axis = [0, 100, 200, 300, 400, 500, 600, 800, 1000, 1500, 2000];
                    var delay_time_axis_label = ["0ms", "100ms", "200ms", "300ms", "400ms", "500ms", "600ms", "800ms", "1000ms", "1500ms", "2000+ms"];
                    var legendElementWidth = width/colors.length;

                    var ipListPromise = delayFlowServ.postIpList({"az_id":"","pod_id":""});
                    ipListPromise.then(function(responseData){
                        var ipListRet=responseData.result;
                        var delayInfoPromise = delayFlowServ.getDelayInfo();
                        delayInfoPromise.then(function(responseData){
                            var delayLink=responseData;
                            var ipList=[],
                                ipSeq=[],
                                delayMatrix=[],
                                len=ipListRet.length;
                            ipListRet.forEach(function(ip,i){
                                ipList[i]=ip['ip'];
                                ipSeq[ip['ip']]=i;
                                delayMatrix[i] = d3.range(len).map(function(j) { return {x: j, y: i, z: 0}; });
                            });
                            delayLink.forEach(function(link,i)
                            {
                                delayMatrix[ipSeq[link['src_ip']]][ipSeq[link['dst_ip']]].z=
                                    d3.max([link['send_delay'],link['recv_delay'],link['send_round_delay'],link['recv_round_delay']]);
                            });

                            var xEvent,yEvent,sEvent;
                            //定义SVG画布,在svg-g元素中操作
                            var x = d3.scale.ordinal().rangeBands([0, width]),
                                z = d3.scale.linear().domain([0, 4]).clamp(true);

                            x.domain(d3.range(len));
                            var colorScale = d3.scale.quantile()
                                           .domain(delay_time_axis)
                                           .range(colors);

                            //suofang
                            var zoom = d3.behavior.zoom()
                                        .scaleExtent([1, 10])
                                        .on("zoom", zoomed);
                            function zoomed() {
                                xEvent=d3.event.translate[0];
                                yEvent=d3.event.translate[1];
                                sEvent=d3.event.scale;
                                d3.select(this).attr("transform",
                                    "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
                            }
                            var svg = d3.select("#delay_time_chart").append("svg")
                                        .attr("width", width + margin.left + margin.right)
                                        .attr("height", height + margin.top + margin.bottom)
                                        .append("g")
                                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                                        .call(zoom);

                            svg.append("rect")
                                .attr("class", "backRect")
                                .attr("width", width)
                                .attr("height", height);
                            //cell data
                            var row = svg.selectAll(".row")
                                  .data(delayMatrix)
                                .enter().append("g")
                                  .attr("class", "row")
                                  .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
                                  .each(row);
                            var column = svg.selectAll(".column")
                                  .data(delayMatrix)
                                .enter().append("g")
                                  .attr("class", "column")
                                  .attr("transform", function(d, i) { return "translate("+  x(i) +",0)rotate(-90)"; });

                            if(len>150)
                            {
                                row.append("line")
                                  .attr("x2", width)
                                  .style("stroke-width","0.5");
                                column.append("line")
                                  .attr("x1", -width)
                                  .style("stroke-width","0.5");
                            }
                            else
                            {
                                row.append("line")
                                  .attr("x2", width);
                                column.append("line")
                                  .attr("x1", -width);
                            }

                            function row(row) {
                                var cell = d3.select(this).selectAll(".cell")
                                    .data(row.filter(function(d) { return d.z; }))
                                  .enter().append("rect")
                                    .attr("class", "cell")
                                    .attr("x", function(d) { return x(d.x); })
                                    .attr("width", x.rangeBand())
                                    .attr("height", x.rangeBand())
                                    .style("fill", function(d) { return colorScale(d.z); })
                                    .on("mouseover", mouseover)
                                    .on("mouseout", mouseout);
                            }

                            function mouseover(d) {
                                d3.select(this).classed("cell-hover", true);
                                //Update the DTtooltip position and value
                                d3.select("#DTtooltip")
                                    .style("left", (d3.event.pageX + 10 - delay_time_chart_pos.x) + "px")
                                    .style("top", (d3.event.pageY - 10 - delay_time_chart_pos.y + 50) + "px")
                                    .select("#DTvalue")
                                    .text("src_ip:"+ipList[d.x] + ", dst_ip:" + ipList[d.y] +", delay_time:"
                                        + d.z + "ms");
                                d3.select("#DTtooltip").classed("hidden", false);
                            }
                            function mouseout() {
                                d3.select(this).classed("cell-hover", false);
                                d3.select("#DTtooltip").classed("hidden", true);
                            }
                            var legend = svg.selectAll(".legend")
                                .data(delay_time_axis_label)
                                .enter().append("g")
                                .attr("class", "legend");

                            legend.append("rect")
                                .attr("x", function (d, i) {
                                    return legendElementWidth * i;
                                })
                                .attr("y", height + 8)
                                .attr("width", legendElementWidth)
                                .attr("height", 8)
                                .style("fill", function (d, i) {
                                    return colors[i];
                                });

                            legend.append("text")
                                .attr("class", "mono")
                                .text(function (d) {
                                    return d;
                                })
                                .attr("width", legendElementWidth)
                                .attr("x", function (d, i) {
                                    return legendElementWidth * i;
                                })
                                .attr("y", height + 32);
                            $scope.resetBtn = function()
                            {
                                d3.transition().duration(250).tween("zoom", function() {
                                    var si = d3.interpolate(sEvent, 1);
                                    var xi = d3.interpolate(xEvent, 0);
                                    var yi = d3.interpolate(yEvent, 0);
                                    return function(t){
                                        svg.call(zoom.translate([xi(t),yi(t)]).scale(si(t)).event);
                                    }
                                });
                            }
                            svg.call(zoom.event);
                        },
                        function(responseData)
                        {
                            //showERRORMsg
                        });
                    },
                    function(responseData)
                    {
                        //showERRORMsg
                    });
                };
                delay_info_chart();
            }];


        var module = angular.module('common.config');
        module.tinyController('delayFlow.ctrl', delayFlowCtrl);

        return module;
    });