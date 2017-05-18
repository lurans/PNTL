define(["can/util/fixture/fixture"], function (fixture) {
    "use strict";
    var deploymentList = {
        'deploymentList': [
            {
                id:'1',
                service:'meterticket',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'2',
                service:'ConsoleFramework',
                version:'2015-12-02_07-57-34',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:09:18'
            },
            {
                id:'3',
                service:'ApiGateway',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'4',
                service:'AutoScaling',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'5',
                service:'VPC',
                version:'1.1.8.SP4-2015-12-02_02-57-00',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'6',
                service:'meterticket',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'7',
                service:'ECS',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'8',
                service:'AutoScaling',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'9',
                service:'meterticket',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            },
            {
                id:'10',
                service:'ApiGateway',
                version:'1.0.1-2015-12-02_08-15-51',
                content:'xxxxxxxx',
                updater:'jenkins',
                time:'2015-12-02 08:14:45'
            }
        ]
    };

    var servicesList = [
        {
            serviceName:"ConsoleFramework"
        },
        {
            serviceName:"ECM"
        }
    ];
    var taskNodeList = {
        taskNodeList:[
            {
                name:'node1'
            },
            {
                name:'node2'
            },
            {
                name:'node3'
            }
        ]
    };
    var taskSoftList = {
        taskSoftList:[
            {
                name:'softA'
            },
            {
                name:'softB'
            },
            {
                name:'softC'
            }
        ]
    };
    var taskSoftVersion = {
        taskSoftVersion:[
            {
                name:'softA',
                version:['versionA1.0.0.1','versionA1.0.0.2','versionA1.0.0.3']
            },
            {
                name:'softB',
                version:['versionB2.0.0.1','versionB2.0.0.2','versionB2.0.0.3']
            },
            {
                name:'softC',
                version:['versionC3.0.0.1','versionC3.0.0.2','versionC3.0.0.3']
            }
        ]
    };

    var taskList1 = {
           "taskList1":[
               {
                    id:'1',
                    name:"ECS",
                    serviceNode:'ECS_WEB1',
                    action:'install',
                    package:'smn 1.0.0',
                    version:"1.0.1",
                    operator:'huwanqing',
                    timeConsuming:'00\'00\"',
                    timeStart:'2015-12-03 19:01:18"',
                    timeEnd:'2015-12-03 19:01:18',
                    status:0
               },
               {
                   id:'2',
                   name:"ConsoleFramework",
                   serviceNode:'ECS_WEB1',
                   action:'Rollback',
                   package:'smn 1.0.0',
                   version:"1.0.1",
                   operator:'huwanqing',
                   timeConsuming:'00\'00\"',
                   timeStart:'2015-12-03 19:01:18"',
                   timeEnd:'2015-12-03 19:01:18',
                   status:1
               },
               {
                   id:'3',
                   name:"ECS",
                   serviceNode:'ECS_WEB1',
                   action:'Upgrade',
                   package:'smn 1.0.0',
                   version:"1.0.1",
                   operator:'huwanqing',
                   timeConsuming:'00\'00\"',
                   timeStart:'2015-12-03 19:01:18"',
                   timeEnd:'2015-12-03 19:01:18',
                   status:0
               },
               {
                   id:'4',
                   name:"ConsoleFramework",
                   serviceNode:'ECS_WEB1',
                   action:'Rollback',
                   package:'smn 1.0.0',
                   version:"1.1.1",
                   operator:'huwanqing',
                   timeConsuming:'00\'00\"',
                   timeStart:'2015-12-03 19:01:18"',
                   timeEnd:'2015-12-03 19:01:18',
                   status:2
               },
               {
                   id:'5',
                   name:"ECS",
                   serviceNode:'ECS_WEB1',
                   action:'install',
                   package:'smn 1.0.0',
                   version:"1.0.1",
                   operator:'huwanqing',
                   timeConsuming:'00\'00\"',
                   timeStart:'2015-12-03 19:01:18"',
                   timeEnd:'2015-12-03 19:01:18',
                   status:0
               },
               {
                   id:'7',
                   name:"ECS",
                   serviceNode:'ECS_WEB1',
                   action:'Upgrade',
                   package:'smn 1.0.0',
                   version:"2.0.1",
                   operator:'huwanqing',
                   timeConsuming:'00\'00\"',
                   timeStart:'2015-12-03 19:01:18"',
                   timeEnd:'2015-12-03 19:01:18',
                   status:2
               },
               {
                   id:'6',
                   name:"CS",
                   serviceNode:'ECS_WEB1',
                   action:'install',
                   package:'smn 1.0.0',
                   version:"1.0.2",
                   operator:'huwanqing',
                   timeConsuming:'00\'00\"',
                   timeStart:'2015-12-03 19:01:18"',
                   timeEnd:'2015-12-03 19:01:18',
                   status:1
               }
           ]
    };


    var nodeNameList = {
        ECS_WEB1:[
            {
                serviceNode:'ECS_WEB1',
                soft:'smn 1.0.0',
                version:'1.0.1'
            },
            {
                serviceNode:'ECS_WEB2',
                soft:'smq 1.0.0',
                version:'1.2.1'
            }
        ],
        ConsoleFramework_1:[
            {
                serviceNode:'CF_WEB1',
                soft:'smn 1.0.0',
                version:'1.0.1'
            },
            {
                serviceNode:'CF_WEB2',
                soft:'smq 1.0.0',
                version:'1.2.1'
            }
        ]
    };

    fixture({
        "GET /odmk/deploy/getDeploymentList":function(original, response){
            var data = original.data;
            var page = data.page;
            var displayLength = data.displayLength;
            var start = (page-1)>0?(page-1)*displayLength:0;
            var end = page*displayLength>deploymentList.deploymentList.length?deploymentList.deploymentList.length:page*displayLength;
            var option = {
                totalRecords:'',
                list:[]
            };
            option.totalRecords = deploymentList.deploymentList.length;
            for(var i = start;i<end;i++){
                option.list.push(deploymentList.deploymentList[i]);
            }
            response(200, "success", option, {});
        },
        "GET /odmk/deploy/getTaskList":function(original, response){
            var data = original.data;
            var page = data.page;
            var displayLength = data.displayLength;
            var start = (page-1)>0?(page-1)*displayLength:0;
            var end = page*displayLength>taskList1.taskList1.length?taskList1.taskList1.length:page*displayLength;
            var option = {
                totalRecords:'',
                list:[]
            };
            option.totalRecords = taskList1.taskList1.length;
            for(var i = start;i<end;i++){
                option.list.push(taskList1.taskList1[i]);
            }
            response(200, "success", option, {});
        },
        "GET /odmk/deploy/getTaskDetails/{id}":function(original, response){
            var data = original.data;
            var id = data.id+'';
            var option = "";
            for(var i =0;i<taskList1.taskList1.length;i++){
                if(id===taskList1.taskList1[i].id){
                    option = taskList1.taskList1[i];
                }
            }
            response(200, "success", option, {});
        },
        "GET /odmk/deploy/getNodeNameList/{nodeName}":function(original, response){
            var nodeName = original.data.nodeName;
            var option = nodeNameList[nodeName+''];
            response(200, "success", option, {});
        },
        "GET /odmk/deploy/getServicesList":function(original, response){
            response(200, "success", servicesList, {});
        },
        "GET /odmk/deploy/getTaskNodeList":function(original, response){
            response(200, "success", taskNodeList, {});
        },
        "GET /odmk/deploy/getTaskSoftList":function(original, response){
            response(200, "success", taskSoftList, {});
        },
        "GET /odmk/deploy/getTaskSoftVersion":function(original, response){
            var soft = original.data;
            var result = "";
            for(var i=0;i<taskSoftVersion.taskSoftVersion.length;i++){
                if(soft===taskSoftVersion.taskSoftVersion[i].name){
                    result = taskSoftVersion.taskSoftVersion[i];
                }
            }
            response(200, "success", result, {});
        }
    });
    return fixture;
});