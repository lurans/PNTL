define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/delayFlowFixture"],
    function (i18n,commonException,Step, _StepDirective, ViewMode) {
        "use strict";

        var delayFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "delayFlowServ","$interval",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, delayFlowServ,$interval) {
                $scope.i18n = i18n;

                $scope.button = {
                    "id":"resetBtn_id",
                    "text" : i18n.chkFlow_term_recovery_btn
                };

                $scope.ipList=[];
                $scope.ipSeq=[];
                $scope.delayMatrix=[];

                var margin = {top: 10, right: 10, bottom: 50, left: 10};
                var width = 640,
                    height = 640;
                var colors = ['#750000','#92DD92', '#60EE97', '#FFBB77','#FF8000', '#FF2D2D'];
                var delayTimeLevel = [-1, 0, 10, 20, 50, 100];
                var legendElementWidth = width/delayTimeLevel.length;
                function statusColor(delayTime)
                {
                    for(var i=0; i<delayTimeLevel.length-1; i++)
                    {
                        if(delayTime>=delayTimeLevel[i]&&delayTimeLevel[i+1]>delayTime)
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
                var delay_time_chart_pos = getAbsPoint(document.getElementById('delay_time_chart_wrapper'));
                function delay_info_chart()
                {
                    var ipListLen=$scope.ipList.length;
                    var xEvent,yEvent,sEvent;
                    var PRIORITY = 1000000;

                    var xEvent,yEvent,sEvent;
                    //定义SVG画布,在svg-g元素中操作
                    var x = d3.scale.ordinal().rangeBands([0, width]),
                        z = d3.scale.linear().domain([0, 4]).clamp(true);

                    x.domain(d3.range(ipListLen));
                    var colorScale = d3.scale.quantile()
                                   .domain(delayTimeLevel)
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
                          .data($scope.delayMatrix)
                        .enter().append("g")
                          .attr("class", "row")
                          .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
                          .each(row)
                          .append("line")
                          .attr("x2", width);
                    var column = svg.selectAll(".column")
                          .data($scope.delayMatrix)
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
                        //Update the DTtooltip position and value
                        d3.select("#DTtooltip")
                            .style("left", (d3.event.pageX + 10 - delay_time_chart_pos.x - 80) + "px")
                            .style("top", (d3.event.pageY - 10 - delay_time_chart_pos.y + 80) + "px")
                            .style("z-index",PRIORITY)
                            .select("#DTvalue")
                            .text("src_ip:"+$scope.ipList[d.y] + " dst_ip:" + $scope.ipList[d.x]
                                + " send_delay:" + d.send_delay + "ms" + " recv_delay:" + d.recv_delay + "ms"
                                + " send_round_delay:" + d.send_round_delay + "ms" + " timestamp:" + d.timestamp
                                + " time:" + d.time);
                        d3.select("#DTtooltip").classed("hidden", false);
                    }
                    function mouseout() {
                        d3.select(this).classed("cell-hover", false);
                        d3.select("#DTtooltip").classed("hidden", true);
                    }
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
                    };
                    svg.call(zoom.event);
                }
                function getIpInfo(delayDataJson)
                {
                    $scope.ipList=[];
                    $scope.ipSeq=[];
                    $scope.delayMatrix=[];

                    var len=delayDataJson.length;
                    delayDataJson.forEach(function(ip,i){
                        $scope.ipList[i]=ip['ip'];
                        $scope.ipSeq[ip['ip']]=i;
                        $scope.delayMatrix[i] = d3.range(len).map(function(j) {
                            return {
                                x: j,
                                y: i,
                                z: 0,
                                send_delay: "",
                                recv_delay: "",
                                send_round_delay: "",
                                recv_round_delay: "",
                                timestamp:"",
                                time: ""
                            };
                        });
                    });
                }
                function getDelayLinkInfo(linkDataInfo)
                {
                    linkDataInfo.forEach(function(link,index){
                        var srcIp = link['src_ip'];
                        var dstIp = link['dst_ip'];
                        if(srcIp in $scope.ipSeq && dstIp in $scope.ipSeq)
                        {
                            var i = $scope.ipSeq[srcIp];
                            var j = $scope.ipSeq[dstIp];
                            var sendRoundDelay = link['send_round_delay'][0] === '-' ? -1 : parseFloat(link['send_round_delay']);
                            $scope.delayMatrix[i][j].z = sendRoundDelay < 0 ? -1 : sendRoundDelay;
                            $scope.delayMatrix[i][j].send_delay = link["send_delay"];
                            $scope.delayMatrix[i][j].recv_delay = link["recv_delay"];
                            $scope.delayMatrix[i][j].send_round_delay = link["send_round_delay"];
                            $scope.delayMatrix[i][j].recv_round_delay = link["recv_round_delay"];
                            $scope.delayMatrix[i][j].timestamp = link["timestamp"];
                            $scope.delayMatrix[i][j].time = link["time"];
                        }
                    });
                }
                function getDelayLink()
                {
                    var delayInfoPromise = delayFlowServ.getDelayInfo();
                    delayInfoPromise.then(function(responseData){
                        getDelayLinkInfo(responseData);
                        $("#delay_time_chart").html("");
                        delay_info_chart();
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
                    var ipListPromise = delayFlowServ.postIpList(para);
                    ipListPromise.then(function(responseData){
                        if(responseData.result === null||responseData.result === ""){
                            $scope.noData = true;
                            $scope.showData = false;
                        }else {
                            $scope.noData = false;
                            $scope.showData = true;
                            getIpInfo(responseData.result);
                            getDelayLink();
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
                };
                $scope.$on('$destroy', function (angularEvent, current, previous) {
                    $scope.stopAutoRefresh();
                });
            }];


        var module = angular.module('common.config');
        module.tinyController('delayFlow.ctrl', delayFlowCtrl);

        return module;
    });