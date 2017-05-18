define(["can/util/fixture/fixture"], function (fixture) {
    "use strict";
    var packageList = {
        'packageList':[
            {
                id:'package_201512211827',
                name:'smn-ps-1.0.0-release.tar.gz',
                productName:'smn',
                packageType:'xxx',
                version:'1.0.0',
                releaseTime:'2015-12-21 18:27',
                publisher:'Louis',
                size:'12354'
            },
            {
                id:'package_201512211828',
                name:'smn-ps-1.0.1-release.tar.gz',
                productName:'odmk',
                packageType:'xxx',
                version:'1.0.0',
                releaseTime:'2015-12-21 18:28',
                publisher:'Louis',
                size:'321312'
            },
            {
                id:'package_201512211829',
                name:'smn-ps-1.0.2-release.tar.gz',
                productName:'smq',
                packageType:'xxx',
                version:'1.0.2',
                releaseTime:'2015-12-21 18:29',
                publisher:'Louis',
                size:'312312'
            }
        ]
    };
    fixture({
        "GET /rest/basic/packageList":function(original, response){
            var page = original.data.currentPage;
            var displayLength = original.data.displayLength;
            var totalPage = Math.ceil(packageList.packageList.length / displayLength);
            var data={
                "packageList":[],
                "page":page,
                "totalPage":totalPage,
                "contentNum":packageList.packageList.length
            };
            var end = page<totalPage?(page*displayLength):packageList.packageList.length;
            if(totalPage){
                for(var i = (page-1)*displayLength;i<end;i++){
                    data.packageList.push(packageList.packageList[i]);
                }
            }
            data.num=data.packageList.length;
            response(200, "success", data, {});
        },
        "GET /rest/basic/packageList/{id}":function(original, response){
            var i = Math.random();
            if ( i < 0.99) {
                response(200, "success", {result:true},{});
            } else {
                response(400, "success", {result:false},{});
            }
        }
    });
    return fixture
});