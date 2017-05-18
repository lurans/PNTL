define(["tiny-directives/Directive", "tinyext-widgets/FilterSelect"], function(Directive, SelectWidget) {

    var DEFAULT_CONFIG = {
        "directiveName" : "tinyFiltersel",
        "widgetClass" : SelectWidget,
        "scope" : {
            "id" : "=",
            "values" : "=",
            "width" : "=",
            "display" : "=",
            "disable" : "=",
            "mode" : "=",
            "defaultSelectid" : "=",
            "height" : "=",
            "validate" : "=",
            "tooltip" : "=",
            "isvalidtip" : "=", 
            "errorMsg" : "=", 
            "extendFunction" : "=",
            "change" : "&"
        },
        "constantProperties" : ["mode","validate", "tooltip", "isvalidtip", "errorMsg", "extendFunction"],
		"template" : "<span></span>",
		"replace" : "true"
    };

    var Select = Directive.extend({
        "init" : function(options) {
            var directiveThis = this;
            directiveThis._super(_.extend({}, DEFAULT_CONFIG, options));
        },
		"compile" : function(){
			var directiveThis = this;
			return directiveThis.link;
		},		
		"link": function( scope, element, attrMap ){
			var selectThis = this,constants = _.pick(scope, selectThis.constantProperties);
			
			//filter undefined scope
			var validProperName = [];
			_.filter(constants,function(value,key){
				if(value !== undefined) {
					validProperName.push(key);
				}
                return value !== undefined;
			});
			selectThis = new selectThis.widgetClass(_.pick(constants,validProperName));
			element.append(selectThis._element);
			
            scope.$watch("id", function(newValues, oldValues) {
                selectThis.option("id", newValues);
            });
            scope.$watch("width", function(newValues, oldValues) {
                selectThis.option("width", newValues);
            });
            scope.$watch("height", function(newValues, oldValues) {
                selectThis.option("height", newValues);
            });
            scope.$watch("display", function(newValues, oldValues) {
                selectThis.option("display", newValues);
            });
            scope.$watch("disable", function(newValues, oldValues) {
                selectThis.option("disable", newValues);
            });
            scope.$watch("defaultSelectid", function(newValues, oldValues) {
                selectThis.option("default-selectid", newValues);
            });
            scope.$watch("values", function(newValues, oldValues) {
                selectThis.option("values", newValues);
            });
            
            if (attrMap.change) {
                selectThis.$inputText.on("changeEvt", function() {
                    scope.$apply(scope.change);
                });
            }
        }
    });

    new Select().toAngularDirective();
    return Select;
});
