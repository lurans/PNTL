//首先要继承class类。定义step的属性。
//这个控件有两种使用方式，一种是通过标签，一种是通过initialize方法。
//变量有：ostep,id,values,cls,display,disable,width,height,selectedNode;
define(["tiny-lib/angular","tiny-lib/underscore", "tiny-lib/jquery", "tiny-lib/Class", "tiny-widgets/Widget", "tiny-lib/encoder"], function(angular,_, $, Class, Widget, encoder) {
    var DEFAULT_CONFIG = {

        "template" : '<div class="tiny-step" style="position: relative;"></div>'
    };
    var CONST_VALUES = { 
        "NODE_WIDTH" : 16
    };

    var Step = Widget.extend({

        "init" : function(options) {

            var widgetThis = this;

            widgetThis._varInit(options);

            widgetThis._super(_.extend({
            }, DEFAULT_CONFIG, options));
            widgetThis._setId();
            widgetThis._reRender();
            $("#"+options["id"]).append(widgetThis._element);
        },
        "first" : function() {
            var widgetThis = this;

            widgetThis.selectedNode = 0;
            widgetThis._reRender();
        },

        "last" : function() {

            var widgetThis = this;

            widgetThis.selectedNode = widgetThis.values.length - 1;
            widgetThis._reRender();
        },

        "pre" : function(nodeLength) {
            var widgetThis = this;

            var nodeLength = parseInt(nodeLength, 10);
            if (isNaN(nodeLength) === true || nodeLength < 2) {
                widgetThis.selectedNode = widgetThis.selectedNode - 1;
            }
            else {
                widgetThis.selectedNode = widgetThis.selectedNode - parseInt(nodeLength, 10);
            }
            if (widgetThis.selectedNode < 0) {
                widgetThis.selectedNode = 0;
            }
            widgetThis._reRender();
        },

        "next" : function(nodeLength) {
            var widgetThis = this;

            var nodeLength = parseInt(nodeLength, 10);
            if (isNaN(nodeLength) === true || nodeLength < 2) {
                widgetThis.selectedNode = widgetThis.selectedNode + 1;
            }
            else {
                widgetThis.selectedNode = widgetThis.selectedNode + nodeLength;
            }
            if (widgetThis.selectedNode > (widgetThis.values.length - 1)) {
                widgetThis.selectedNode = widgetThis.values.length - 1;
            }
            widgetThis._reRender();
        },
        "jump" : function(index) {

            var widgetThis = this;

            var index = parseInt(index, 10);
            widgetThis.selectedNode = index;
            if (widgetThis.selectedNode > (widgetThis.values.length - 1)) {
                widgetThis.selectedNode = widgetThis.values.length - 1;
            }
            if(widgetThis.selectedNode<0)
            {
                widgetThis.selectedNode=0;
            }
            widgetThis._reRender();
        },
        "_generateElement" : function() {

            var widgetThis = this;

            widgetThis._super();

            widgetThis.oStep = widgetThis._element;
        },
        
        //成员变量初始化
        "_varInit" : function(options) {
            var widgetThis = this;
            widgetThis.oStep = null;
            widgetThis.id = options.id;
            widgetThis.cls = null;
            widgetThis.display = (options.display) ? options.display : true;
            widgetThis.disable = (options.disable) ? options.disable : false;
            widgetThis.jumpable = (options.jumpable) ? options.jumpable : true;
            widgetThis.width = (options.width) ? options.width : 180;
            widgetThis.height = 40;
            widgetThis.values = options.values;
            widgetThis.selectedNode = 0;
        },
        "_setOption" : function(key, value) {
            var widgetThis = this;

            widgetThis._super(key, value);

            var widgetThis = this;

            switch (key) {
            	case 'id':
                    widgetThis._updateId(value);
                    break;
                case 'cls':
                    widgetThis.cls = value;
                    break;
                case 'display':
                    widgetThis.display = value;
                    break;
                case 'disable':
                    widgetThis.disable = value;
                    break;
                case 'width':
                    widgetThis.width = value;
                    break;
                case 'selectedNode':
                    widgetThis.selectedNode = value;
                    break;
                case 'jumpable':
                    widgetThis.jumpable = value;
                    break;
                case 'values':
                    widgetThis.values = value;
                    break;

            }
            widgetThis._reRender();
        },
        "_addBehavior" : function() {

            var widgetThis = this;

            var element = widgetThis._element;

        },
        "_setId" : function() {
            var widgetThis = this;
            widgetThis.id = _.uniqueId("tiny-step-");
        },
        "_reRender" : function() {
            var widgetThis = this;
            //Empty widgetThis.oStep if it already exists.
            widgetThis.oStep.empty();
            widgetThis.oStep.addClass(widgetThis.cls);
            var mainConDiv = $('<div style="float:left;">');
            var nodeDatas = widgetThis.values;
            var remainingNodes = [];
            var nodeLink = [];
            var totalNodes = nodeDatas.length;

            // Get Width Of Links between Nodes.
            var linkWidth = (widgetThis.width - totalNodes * (CONST_VALUES.NODE_WIDTH)) / (totalNodes - 1);
            var remNodeImage;
            var remNodeData;

            for (var i = 0; i < totalNodes; i++) {

                // If node is selected then set selectedNode class else
                // deselecetdNode class of Div.
                if (widgetThis.selectedNode >= 0 && i <= widgetThis.selectedNode && widgetThis.disable !== true) {
                    nodeLink[i] = $('<div class="tiny-step-selectedNodeLink">');
                    remNodeImage = $('<div class="tiny-step-selectedNode"">');
                }
                else {
                    nodeLink[i] = $('<div class="tiny-step-nodeLink">');
                    remNodeImage = $('<div class="tiny-step-deselectedNode"">');
                }
                nodeLink[i].css({
                    "width" : linkWidth
                });
                if (i != 0) {
                    mainConDiv.append(nodeLink[i]);
                }
                // Created Next Node and appended to mainConDiv
                widgetThis.id = $.encoder.encodeForHTMLAttribute("id", widgetThis.id, true);
                remainingNodes[i] = $('<div id="' + widgetThis.id + '-' + i + '" style="float:left">');
                remainingNodes[i].css({
                    "width" : CONST_VALUES.NODE_WIDTH
                });
                if (widgetThis.selectedNode >= totalNodes && i == (totalNodes - 2)) {
                    if (widgetThis.disable === true) {
                        //Need to add 'selectedNodeDisable' class and image for
                        // same in css class
                        remNodeImage = $('<div class="tiny-step-deselectedNode">');
                    }
                    else {
                        remNodeImage = $('<div class="tiny-step-selectedNode">');
                    }
                }
                remNodeData = $('<div class = "tiny-step-text">');
                remNodeData.html(nodeDatas[i] + "");
                if (widgetThis.disable === true) {
                    remNodeData.css({
                        'color' : '#c7c7c7'
                    });
                }
                remainingNodes[i].append(remNodeImage);
                remainingNodes[i].append(remNodeData);
                //绑定事件。
                remainingNodes[i].on("click", function(evt) {
                    if (!widgetThis.disable && widgetThis.jumpable) {
                        widgetThis.trigger("click", [evt]);
                        var index = this.id.split("-").pop();
                        widgetThis.jump(index);

                        widgetThis.trigger("clickFn", [index]);
                        if ("function" == ( typeof widgetThis.options["clickFn"])) {
                            widgetThis.options["clickFn"](index);
                        }

                    }
                });
                mainConDiv.append(remainingNodes[i]);
            }
            mainConDiv.css({
                "width" : widgetThis.width,
                "height" : widgetThis.height
            });
            widgetThis.oStep.append(mainConDiv);
        }
    });
    window.tinyWidget.Step = Step;
    return Step;
});
