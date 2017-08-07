define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/lossFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var lossFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "lossFlowServ","$interval",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, lossFlowServ,$interval) {
                $scope.i18n = i18n;

                $scope.button = {
                    "id":"resetBtn_id",
                    "text" : i18n.chkFlow_term_recovery_btn,
                };

                $scope.ipList=[];
                $scope.ipSeq=[];
                $scope.lossMatrix=[];

                var para={
                    "az_id":"",
                    "pod_id":""
                };
                var margin = {top: 10, right: 10, bottom: 50, left: 10};
                var width = 640,
                    height = 640;
                var lossThreshold = [0, 5, 10, 20, 50, 100];
                var colors = ['#92DD92', '#FFFF00', '#FFBB77','#FF8000', '#FF2D2D', '#750000'];
                var legendElementWidth = width/lossThreshold.length;
                function statusColor(lossRate)
                {
                    for(var i=0; i<lossThreshold.length-1; i++)
                    {
                        if(lossRate>=lossThreshold[i]&&lossThreshold[i+1]>lossRate)
                            return colors[i];
                    }
                    return colors[i];
                }

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

                function loss_info_chart()
                {
                    var ipListLen=$scope.ipList.length;
                    var xEvent,yEvent,sEvent;
                    var PRIORITY = 1000000;

                    //定义SVG画布,在svg-g元素中操作
                    var x = d3.scale.ordinal().rangeBands([0, width]),
                        z = d3.scale.linear().domain([0, 4]).clamp(true);

                    x.domain(d3.range(ipListLen));

                    var colorScale = d3.scale.quantile()
                                   .domain(lossThreshold)
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
                          .data($scope.lossMatrix)
                        .enter().append("g")
                          .attr("class", "row")
                          .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
                          .each(row).append("line")
                          .attr("x2", width);
                    var column = svg.selectAll(".column")
                          .data($scope.lossMatrix)
                        .enter().append("g")
                          .attr("class", "column")
                          .attr("transform", function(d, i) { return "translate("+  x(i) +",0)rotate(-90)"; })
                          .append("line")
                          .attr("x1", -width);
                    if(ipListLen>150)
                    {
                        row.style("stroke-width","0.5");
                        column.style("stroke-width","0.5");
                    }

                    function row(row) {
                        var cell = d3.select(this).selectAll(".cell")
                            .data(row.filter(function(d) { return d.z; }))
                          .enter().append("rect")
                            .attr("class", "cell")
                            .attr("x", function(d) { return x(d.x); })
                            .attr("width", x.rangeBand())
                            .attr("height", x.rangeBand())
                            .style("fill", function(d) { return statusColor(d.z); })
                            .on("mouseover", mouseover)
                            .on("mouseout", mouseout);
                    }

                    function mouseover(d) {
                        d3.select(this).classed("cell-hover", true);
                        //Update the LPtooltip position and value
                        d3.select("#LPtooltip")
                            .style("left", (d3.event.pageX + 10 - packets_loss_chart_pos.x - 80) + "px")
                            .style("top", (d3.event.pageY - 10 - packets_loss_chart_pos.y + 80) + "px")
                            .style("z-index",PRIORITY)
                            .select("#LPvalue")
                            .text("src_ip:"+$scope.ipList[d.y] + " dst_ip:" + $scope.ipList[d.x]
                                + " send_loss_rate:" + d.send_loss_rate + " send_pkgs:" + d.send_pkgs
                                + " recv_loss_rate:" + d.recv_loss_rate + " recv_pkgs:" + d.recv_pkgs
                                + " time:" + d.time);
                        d3.select("#LPtooltip").classed("hidden", false);
                    }
                    function mouseout() {
                        d3.select(this).classed("cell-hover", false);
                        d3.select("#LPtooltip").classed("hidden", true);
                    }
                    var legend = svg.selectAll(".legend")
                        .data(lossThreshold)
                        .enter().append("g")
                        .attr("class", "legend");
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
                }
                function getIpInfo(lossDataJson)
                {
                    $scope.ipList=[];
                    $scope.ipSeq=[];
                    $scope.lossMatrix=[];

                    var len=lossDataJson.length;
                    lossDataJson.forEach(function(ip,i){
                        $scope.ipList[i]=ip['ip'];
                        $scope.ipSeq[ip['ip']]=i;
                        $scope.lossMatrix[i] = d3.range(len).map(function(j) { return {
                            x: j,
                            y: i,
                            z: 0,
                            send_loss_rate: "",
                            send_pkgs: "",
                            recv_loss_rate: "",
                            recv_pkgs: "",
                            time: ""
                            };
                        });
                    });
                }
                function getLossLinkInfo(linkDataInfo)
                {
                    linkDataInfo.forEach(function(link,i)
                    {
                        var srcIp = link['src_ip'];
                        var dstIp = link['dst_ip'];
                        if(srcIp in $scope.ipSeq && dstIp in $scope.ipSeq)
                        {
                            var i = $scope.ipSeq[srcIp];
                            var j = $scope.ipSeq[dstIp];
                            $scope.lossMatrix[i][j].z = d3.max([parseFloat(link['send_loss_rate']),parseFloat(link['recv_loss_rate'])]);
                            $scope.lossMatrix[i][j].send_loss_rate = link["send_loss_rate"];
                            $scope.lossMatrix[i][j].send_pkgs = link["send_pkgs"];
                            $scope.lossMatrix[i][j].recv_loss_rate = link["recv_loss_rate"];
                            $scope.lossMatrix[i][j].recv_pkgs = link["recv_pkgs"];
                            $scope.lossMatrix[i][j].time = link["time"];
                        }
                    });
                }
                function getLossLink()
                {
                    var lossInfoPromise = lossFlowServ.getLossInfo();
                    lossInfoPromise.then(function(responseData){
                        getLossLinkInfo(responseData);
                        $("#packets_loss_chart").html("");
                        loss_info_chart();
                    },function(responseData){
                        //showERRORMsg

                    });
                }
                function getIpList()
                {
                    var para={
                        "az_id":"",
                        "pod_id":""
                    };
                    var ipListPromise = lossFlowServ.postIpList(para);
                    ipListPromise.then(function(responseData){
                        if(responseData.result == null||responseData.result == ""){
                            $scope.noData = true;
                            $scope.showData = false;
                        }else {
                            $scope.noData = false;
                            $scope.showData = true;
                            getIpInfo(responseData.result);
                            getLossLink();
                        }
                    },function(responseData){
                        //showERRORMsg

                    });
                }

                var init = function()
                {
                    getIpList();
                };
                init();
                var autoRefresh = $interval(getIpList, 60000);
                $scope.stopAutoRefresh = function () {
                    if (autoRefresh) {
                        $interval.cancel(autoRefresh);
                        autoRefresh = null;
                    }
                }
                $scope.$on('$destroy', function (angularEvent, current, previous) {
                    $scope.stopAutoRefresh();
                });
            }];

        var module = angular.module('common.config');
        module.tinyController('lossFlow.ctrl', lossFlowCtrl);
        return module;
        });