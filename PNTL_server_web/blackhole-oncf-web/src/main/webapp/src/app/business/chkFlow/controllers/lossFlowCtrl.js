define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/lossFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var lossFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "lossFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, lossFlowServ) {
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

                var packets_loss_chart_pos = getAbsPoint(document.getElementById('packets_loss_chart_wrapper'));
                //lossInfo --> linkdata
                var generate_loss_chart = function()
                {
                    //svg
                    var margin = {top: 10, right: 10, bottom: 50, left: 10};
                    var width = 640,height = 640;
                    var colors = ['#79CD79','#FFFFB9', '#FFBB77','#FF8000', '#FF2D2D', '#AE0000'];
                    var packets_loss_axis = [0, 5, 10, 20, 50, 100];
                    var packets_loss_axis_label = ["0%", "5%", "10%", "20%", "50%", "100%"];
                    var legendElementWidth = width/colors.length;
                    
                    var ipListPromise = lossFlowServ.postIpList({"az_id":"","pod_id":""});
                    ipListPromise.then(function(responseData){
                        var ipListRet=responseData.result;
                        var lossInfoPromise = lossFlowServ.getLossInfo();
                        lossInfoPromise.then(function(responseData){
                            var lossLink=responseData;
                            var ipList=[],
                                ipSeq=[],
                                lossMatrix=[],
                                len=ipListRet.length;
                            ipListRet.forEach(function(ip,i){
                                ipList[i]=ip['ip'];
                                ipSeq[ip['ip']]=i;
                                lossMatrix[i] = d3.range(len).map(function(j) { return {x: j, y: i, z: 0}; });
                            });
                            lossLink.forEach(function(link,i)
                            {
                                var srcIp = link['src_ip'];
                                var dstIp = link['dst_ip'];
                                if(srcIp in ipSeq && dstIp in ipSeq)
                                {
                                    lossMatrix[ipSeq[srcIp]][ipSeq[dstIp]].z=
                                    d3.max([parseFloat(link['send_loss_rate']),parseFloat(link['recv_loss_rate'])]);
                                }

                            });

                            var xEvent,yEvent,sEvent;

                            //定义SVG画布,在svg-g元素中操作
                            var x = d3.scale.ordinal().rangeBands([0, width]),
                                z = d3.scale.linear().domain([0, 4]).clamp(true);

                            x.domain(d3.range(len));

                            var colorScale = d3.scale.quantile()
                                           .domain(packets_loss_axis)
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
                            var svg = d3.select("#packets_loss_chart").append("svg")
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
                                  .data(lossMatrix)
                                .enter().append("g")
                                  .attr("class", "row")
                                  .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
                                  .each(row);
                            var column = svg.selectAll(".column")
                                  .data(lossMatrix)
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
                                //Update the LPtooltip position and value
                                d3.select("#LPtooltip")
                                    .style("left", (d3.event.pageX + 10 - packets_loss_chart_pos.x) + "px")
                                    .style("top", (d3.event.pageY - 10 - packets_loss_chart_pos.y + 50) + "px")
                                    .select("#LPvalue")
                                    .text("src_ip:"+ipList[d.x] + ", dst_ip:" + ipList[d.y] +", packets_loss:"
                                        + d.z + "%");
                                d3.select("#LPtooltip").classed("hidden", false);
                            }
                            function mouseout() {
                                d3.select(this).classed("cell-hover", false);
                                d3.select("#LPtooltip").classed("hidden", true);
                            }
                            var legend = svg.selectAll(".legend")
                                .data(packets_loss_axis_label)
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
                generate_loss_chart();
            }];

        var module = angular.module('common.config');
        module.tinyController('lossFlow.ctrl', lossFlowCtrl);
        return module;
        });