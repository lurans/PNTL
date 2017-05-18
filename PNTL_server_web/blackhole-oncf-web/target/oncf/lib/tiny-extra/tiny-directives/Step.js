define(["tiny-directives/Directive", "tiny-widgets/Step"], function(Directive, StepWidget) {
    var DEFAULT_CONFIG = {

        "directiveName" : "tinyStep",

        "widgetClass" : StepWidget,

        "constantProperties" : ["id", "values"],

        "scope" : {
            "id" : "=",
            "values" : "=",
            "display" : "=",
            "disable" : "=",
            "jumpable" : "=",
            "width" : "=",
            "cls" : "=",
            "clickFn" : "&"
        }

    };

    var Step = Directive.extend({

        "init" : function(options) {

            var directiveThis = this;

            directiveThis._super(_.extend({
            }, DEFAULT_CONFIG, options));

        },

        "link" : function(scope, element, attrMap) {
        	scope.$watch("id", function(newValue, oldValue) {

                element.widget().option("id", newValue);

            });

            scope.$watch("values", function(newValue, oldValue) {

                element.widget().option("values", newValue);

            });

            scope.$watch("display", function(newValue, oldValue) {

                element.widget().option("display", newValue);

            });

            scope.$watch("disable", function(newValue, oldValue) {

                element.widget().option("disable", newValue);

            });
            scope.$watch("jumpable", function(newValue, oldValue) {

                element.widget().option("jumpable", newValue);

            });
            scope.$watch("width", function(newValue, oldValue) {

                element.widget().option("width", newValue);

            });

            if (attrMap.clickFn) {
                element.widget().on("clickFn", function() {
                    var param = {
                        "index": arguments[0]
                    };
                    scope.clickFn(param);
                });
            }
            var widgetInstance = element.widget();

        }
    });

    new Step().toAngularDirective();

    return Step;

});

