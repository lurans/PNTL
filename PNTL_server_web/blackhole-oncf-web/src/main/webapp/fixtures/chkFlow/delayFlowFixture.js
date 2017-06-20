define(["can/util/fixture/fixture", "tiny-lib/underscore"], function (fixture, _) {
    "use strict";
    var delayData=[
            {
                "src_ip": "192.168.0.39",
                "dst_ip": "192.168.0.38",
                "send_delay": 12,//发送时延
                "recv_delay": 300,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.39",
                "dst_ip": "192.168.0.37",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.47",
                "dst_ip": "192.168.0.43",
                "send_delay": 420,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.46",
                "dst_ip": "192.168.0.34",
                "send_delay": 512,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.19",
                "dst_ip": "192.168.0.11",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.16",
                "dst_ip": "192.168.0.43",
                "send_delay": 620,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.3",
                "dst_ip": "192.168.0.44",
                "send_delay": 102,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.32",
                "dst_ip": "192.168.0.48",
                "send_delay": 1000,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.31",
                "dst_ip": "192.168.0.49",
                "send_delay": 120,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 222,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.49",
                "dst_ip": "192.168.0.11",
                "send_delay": 1200,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.18",
                "dst_ip": "192.168.0.2",
                "send_delay": 1800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.7",
                "dst_ip": "192.168.0.33",
                "send_delay": 120,//发送时延
                "recv_delay": 600,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.6",
                "dst_ip": "192.168.0.14",
                "send_delay": 12,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.35",
                "dst_ip": "192.168.0.15",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.38",
                "dst_ip": "192.168.0.39",
                "send_delay": 12,//发送时延
                "recv_delay": 300,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.37",
                "dst_ip": "192.168.0.39",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.43",
                "dst_ip": "192.168.0.47",
                "send_delay": 420,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.34",
                "dst_ip": "192.168.0.46",
                "send_delay": 512,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.11",
                "dst_ip": "192.168.0.19",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.43",
                "dst_ip": "192.168.0.16",
                "send_delay": 620,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.44",
                "dst_ip": "192.168.0.3",
                "send_delay": 102,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.48",
                "dst_ip": "192.168.0.32",
                "send_delay": 1000,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.49",
                "dst_ip": "192.168.0.31",
                "send_delay": 120,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 222,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.11",
                "dst_ip": "192.168.0.49",
                "send_delay": 1200,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.2",
                "dst_ip": "192.168.0.18",
                "send_delay": 1800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.33",
                "dst_ip": "192.168.0.7",
                "send_delay": 120,//发送时延
                "recv_delay": 600,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.14",
                "dst_ip": "192.168.0.6",
                "send_delay": 12,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.15",
                "dst_ip": "192.168.0.35",
                "send_delay": 1000,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            }/*,
            {
                "src_ip": "192.168.1.39",
                "dst_ip": "192.168.1.38",
                "send_delay": 12,//发送时延
                "recv_delay": 800,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.2",
                "dst_ip": "192.168.0.38",
                "send_delay": 12,//发送时延
                "recv_delay": 800,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.38",
                "dst_ip": "192.168.1.2",
                "send_delay": 12,//发送时延
                "recv_delay": 800,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.37",
                "dst_ip": "192.168.1.2",
                "send_delay": 12,//发送时延
                "recv_delay": 2000,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.12",
                "dst_ip": "192.168.0.18",
                "send_delay": 12,//发送时延
                "recv_delay": 800,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.0.18",
                "dst_ip": "192.168.1.12",
                "send_delay": 12,//发送时延
                "recv_delay": 800,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.39",
                "dst_ip": "192.168.1.37",
                "send_delay": 80,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.47",
                "dst_ip": "192.168.1.43",
                "send_delay": 420,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.46",
                "dst_ip": "192.168.1.34",
                "send_delay": 512,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.19",
                "dst_ip": "192.168.1.11",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.16",
                "dst_ip": "192.168.1.43",
                "send_delay": 620,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.3",
                "dst_ip": "192.168.1.44",
                "send_delay": 102,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.32",
                "dst_ip": "192.168.1.48",
                "send_delay": 1000,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.31",
                "dst_ip": "192.168.1.49",
                "send_delay": 120,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 222,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.49",
                "dst_ip": "192.168.1.11",
                "send_delay": 1200,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.18",
                "dst_ip": "192.168.1.2",
                "send_delay": 1800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.7",
                "dst_ip": "192.168.1.33",
                "send_delay": 120,//发送时延
                "recv_delay": 600,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.6",
                "dst_ip": "192.168.1.14",
                "send_delay": 12,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.35",
                "dst_ip": "192.168.1.15",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.38",
                "dst_ip": "192.168.1.39",
                "send_delay": 12,//发送时延
                "recv_delay": 300,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.37",
                "dst_ip": "192.168.1.39",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.43",
                "dst_ip": "192.168.1.47",
                "send_delay": 420,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.34",
                "dst_ip": "192.168.1.46",
                "send_delay": 512,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.11",
                "dst_ip": "192.168.1.19",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.43",
                "dst_ip": "192.168.1.16",
                "send_delay": 620,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.44",
                "dst_ip": "192.168.1.3",
                "send_delay": 102,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.48",
                "dst_ip": "192.168.1.32",
                "send_delay": 1000,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.49",
                "dst_ip": "192.168.1.31",
                "send_delay": 120,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 222,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.11",
                "dst_ip": "192.168.1.49",
                "send_delay": 1200,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.2",
                "dst_ip": "192.168.1.18",
                "send_delay": 1800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.33",
                "dst_ip": "192.168.1.7",
                "send_delay": 120,//发送时延
                "recv_delay": 600,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 180//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.14",
                "dst_ip": "192.168.1.6",
                "send_delay": 12,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 12,//发送到接受时延
                "recv_round_delay": 60//对端发送到接收时延
            },
            {
                "src_ip": "192.168.1.15",
                "dst_ip": "192.168.1.35",
                "send_delay": 800,//发送时延
                "recv_delay": 60,//接收时延
                "send_round_delay": 10,//发送到接受时延
                "recv_round_delay": 6//对端发送到接收时延
            }*/
        ];

    var ipList=[];
    for(var i=0;i<=255;i++){
        var ip="192.168.0."+i;
        ipList.push({
        "ip":ip,
        });
    }

    var ipInfo={
        "az_id":"",
        "pod_id":"",
        "result":ipList
        /*[
            {
                "ip": "192.168.0.39",
            },
            {
                "ip": "192.168.0.38",
            },
            {
                "ip": "192.168.0.37",
            },
            {
                "ip": "192.168.0.36",
            },
            {
                "ip": "192.168.0.35",
            },
            {
                "ip": "192.168.0.34",
            },
            {
                "ip": "192.168.0.33",
            },
            {
                "ip": "192.168.0.32",
            },
            {
                "ip": "192.168.0.31",
            },
            {
                "ip": "192.168.0.49",
            },
            {
                "ip": "192.168.0.48",
            },
            {
                "ip": "192.168.0.47",
            },
            {
                "ip": "192.168.0.46",
            },
            {
                "ip": "192.168.0.45",
            },
            {
                "ip": "192.168.0.44",
            },
            {
                "ip": "192.168.0.43",
            },
            {
                "ip": "192.168.0.42",
            },
            {
                "ip": "192.168.0.41",
            },
            {
                "ip": "192.168.0.19",
            },
            {
                "ip": "192.168.0.18",
            },
            {
                "ip": "192.168.0.17",
            },
            {
                "ip": "192.168.0.16",
            },
            {
                "ip": "192.168.0.15",
            },
            {
                "ip": "192.168.0.14",
            },
            {
                "ip": "192.168.0.13",
            },
            {
                "ip": "192.168.0.12",
            },
            {
                "ip": "192.168.0.11",
            },
            {
                "ip": "192.168.0.9",
            },
            {
                "ip": "192.168.0.8",
            },
            {
                "ip": "192.168.0.7",
            },
            {
                "ip": "192.168.0.6",
            },
            {
                "ip": "192.168.0.5",
            },
            {
                "ip": "192.168.0.4",
            },
            {
                "ip": "192.168.0.3",
            },
            {
                "ip": "192.168.0.2",
            },
            {
                "ip": "192.168.0.1",
            },
            {
                "ip": "192.168.1.39",
            },
            {
                "ip": "192.168.1.38",
            },
            {
                "ip": "192.168.1.37",
            },
            {
                "ip": "192.168.1.36",
            },
            {
                "ip": "192.168.1.35",
            },
            {
                "ip": "192.168.1.34",
            },
            {
                "ip": "192.168.1.33",
            },
            {
                "ip": "192.168.1.32",
            },
            {
                "ip": "192.168.1.31",
            },
            {
                "ip": "192.168.1.49",
            },
            {
                "ip": "192.168.1.48",
            },
            {
                "ip": "192.168.1.47",
            },
            {
                "ip": "192.168.1.46",
            },
            {
                "ip": "192.168.1.45",
            },
            {
                "ip": "192.168.1.44",
            },
            {
                "ip": "192.168.1.43",
            },
            {
                "ip": "192.168.1.42",
            },
            {
                "ip": "192.168.1.41",
            },
            {
                "ip": "192.168.1.19",
            },
            {
                "ip": "192.168.1.18",
            },
            {
                "ip": "192.168.1.17",
            },
            {
                "ip": "192.168.1.16",
            },
            {
                "ip": "192.168.1.15",
            },
            {
                "ip": "192.168.1.14",
            },
            {
                "ip": "192.168.1.13",
            },
            {
                "ip": "192.168.1.12",
            },
            {
                "ip": "192.168.1.11",
            },
            {
                "ip": "192.168.1.9",
            },
            {
                "ip": "192.168.1.8",
            },
            {
                "ip": "192.168.1.7",
            },
            {
                "ip": "192.168.1.6",
            },
            {
                "ip": "192.168.1.5",
            },
            {
                "ip": "192.168.1.4",
            },
            {
                "ip": "192.168.1.3",
            },
            {
                "ip": "192.168.1.2",
            },
            {
                "ip": "192.168.1.1",
            }
        ]*/
    };
    fixture({
        "POST /rest/chkflow/ipList":function(original, response) {
            response(200, "success", ipInfo, {})
        },
        "GET /rest/chkflow/ipList":function(original, response) {
            response(200, "success", ipInfo, {})
        },
        "GET /rest/chkflow/delayInfo":function(original, response) {
            response(200, "success", delayData, {})
        }
    });
    return fixture;
});