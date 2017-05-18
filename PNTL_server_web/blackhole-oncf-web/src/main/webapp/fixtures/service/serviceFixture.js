define(["can/util/fixture/fixture"], function (fixture) {
    "use strict";
    var serviceList = {
        'serviceList':[
            {
                id:'service_201512211612',
                serviceName:'ECS',
                dns:'ECM',
                nodeList:[
                    {
                        nodeName:'ECS_WEB1',
                        serviceId:'ECS_WEB3_201512241532',
                        host:'B08-01_RH2288H_01',
                        serviceRole:'WEB',
                        packageList:[
                            {
                                packageName:'smn 1.0.1',
                                status:'1',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smn 1.0.2',
                                status:'0',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smn 1.0.3',
                                status:'1',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            }
                        ]
                    },
                    {
                        nodeName:'ECS_WEB2',
                        serviceId:'ECS_WEB3_201512241533',
                        host:'B08-01_RH2288H_02',
                        serviceRole:'WEB',
                        packageList:[
                            {
                                packageName:'smn 1.0.1',
                                status:'1',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smn 1.0.2',
                                status:'1',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smn 1.0.3',
                                status:'0',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            }
                        ]
                    }
                ]
            },
            {
                id:'service_201512211613',
                serviceName:'ConsoleFramework',
                dns:'ECS',
                nodeList:[
                    {
                        nodeName:'ConsoleFramework_1',
                        serviceId:'ConsoleFramework_20151224',
                        host:'B08-02_RH2288H_01',
                        serviceRole:'WEB2',
                        packageList:[
                            {
                                packageName:'smq 1.0.1',
                                status:'0',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smq 1.0.2',
                                packageVersion:'1.0.0',
                                status:'0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smq 1.0.3',
                                status:'1',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            }
                        ]
                    },
                    {
                        nodeName:'ConsoleFramework_2',
                        serviceId:'ConsoleFramework_20151223',
                        host:'B08-02_RH2288H_05',
                        serviceRole:'WEB3',
                        packageList:[
                            {
                                packageName:'smq 1.0.1',
                                status:'0',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smq 1.0.2',
                                status:'0',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            },
                            {
                                packageName:'smq 1.0.3',
                                status:'0',
                                packageVersion:'1.0.0',
                                installTime:'2015-12-21 17:20'
                            }
                        ]
                    }
                ]
            }
        ]
    };
    fixture({
        "GET /rest/basic/serviceList":function(original, response){
            var page = original.data.currentPage;
            var displayLength = original.data.displayLength;
            var totalPage = Math.ceil(serviceList.serviceList.length / displayLength);
            var data={
                "serviceList":[],
                "page":page,
                "totalPage":totalPage,
                "contentNum":serviceList.serviceList.length
            };
            var end = page<totalPage?(page*displayLength):serviceList.serviceList.length;
            if(totalPage){
                for(var i = (page-1)*displayLength;i<end;i++){
                    data.serviceList.push(serviceList.serviceList[i]);
                }
            }
            data.num=data.serviceList.length;
            response(200, "success", data, {});
        },
        "GET /rest/basic/service/nodeList/{nodeName}":function(original, response){
            response(200, "success",serviceList.serviceList[0].nodeList[0], {});
        },
        "PUT /rest/basic/serviceList/{serviceName}":function( original, response){
            var i = Math.random();
            if ( i < 0.5) {
                response(200, "success", {result:true},{});
            } else {
                response(400, "success", {result:false},{});
            }
        },
        "PUT /rest/basic/serviceList/nodeList/{nodeName}":function( original, response){
            var i = Math.random();
            if ( i < 0.5) {
                response(200, "success", {result:true},{});
            } else {
                response(400, "success", {result:false},{});
            }
        },
        "DELETE /rest/basic/serviceList/{serviceName}":function( original, response){
            var i = Math.random();
            if ( i < 0.5) {
                response(200, "success", {result:true},{});
            } else {
                response(400, "success", {result:false},{});
            }
        }
    });
    return fixture;
});