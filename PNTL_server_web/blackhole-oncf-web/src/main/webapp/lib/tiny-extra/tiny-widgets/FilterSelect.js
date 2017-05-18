define(["tiny-lib/angular", "tiny-lib/jquery", "tiny-widgets/Widget", "tiny-common/util", "tiny-common/UnifyValid", "tiny-lib/encoder"],
function(angular, $, Widget, util, UnifyValid,encoder) {
    var domTemplate = '<div class="tiny-filterselect">'
                    + '  <div class="tiny-filterselect-input-field">'
                    + '    <input type="text" readonly="readonly" class="tiny-filterselect-text" style="display:none;">'
                    + '    <span class="tiny-filter-select-span"></span>' 
                    + '  </div>' 
                    + '</div>';
    var popTemplate = '<div class="tiny-filterselect-popup-container" style="display: none;"></div>';

    var DEFAULT_CONFIG = {
        "template" : domTemplate,
        "mode" : "single",
        "default-selectid": "",
        "values" : [],
        "width" : 100, 
        "mode" : "single",
        "display" : true,
        "disable" :false,
        
        //validator param
        "isvalidtip" : true
    };
    
    var CONST_VALUES = {
        "DECIMAL" : 10, 
        "MIN_WIDTH" : 50,        // 最小设置宽度，调整css时，可能需要修改
        "CALC_INPUT_WIDTH" : 45, // 计算内部input宽度时，减去的各种边框宽度，调整css时，可能需要修改
        "CALC_POP_WIDTH" : 2,    // 计算下拉框宽度时，减去边框宽度，调整css时，可能需要修改
        "CALC_POP_OFFSET" : 26,  // 计算下拉框位置时，相对于选择框的垂直偏移量，调整css时，可能需要修改
        "OPTION_BORDER" : 5      // 下拉选项相对于下拉框的宽度差值，调整css时，可能需要修改
    };
       
    var Select = Widget.extend({
        "init" : function(options) {
            var selectThis = this;
            var options = _.extend({}, DEFAULT_CONFIG, options);
            selectThis._super(options);
            selectThis._setOptions(options);
            
            //增加校验及tooltip
            selectThis._addValidate();
            
            $("#" + options["id"]).append(selectThis._element); 
        },
        
        "_addValidate" : function() {
            var selectThis = this;
            var options = selectThis.options;
            
            if (options.validate || options.tooltip) {
                selectThis.$inputField.attr("validator", options.validate);
                selectThis.$inputField.attr("isValidTip", options.isvalidtip);
                selectThis.$inputField.attr("errorMsg", options["errorMsg"] || "");
                selectThis.$inputField.attr("extendFunction", options["extendFunction"]);
                UnifyValid.instantValid(selectThis.$inputText, selectThis.$inputField, 
                        options["validate"], options["tooltip"]||"", options["isvalidtip"], 
                        options["errorMsg"]||undefined, options["extendFunction"]||undefined);
            }
        },
        
        // 重载的原因是要生成弹出菜单，并放到 body中
        "_generateElement" : function() {
            var selectThis = this;
            
            selectThis.$popup=$(popTemplate);
            $('body').append(selectThis.$popup);
            
            var html = _.template(selectThis.options.template)(this);
            selectThis._element = $(html);
        },
        
        "_setOption" : function(key, value) {
            var selectThis = this;
            var options = selectThis.options;
            
            selectThis._super(key, value);
            switch (key) {
                case "id" :
                    selectThis._updateId(value);
                    break;
                case "display" :
                    selectThis._updateDisplay();
                    break;
                case "disable" :
                    selectThis._updateDisable();
                    break;
                case "width" :
                    selectThis._updateWidth();
                    break;
                case "height" :
                    selectThis._updateHeight();
                    break;
                case "values" :
                    selectThis._updateValues();
                    break;
                default :
                    break;
            }
        },
        
        "_updateDisplay" : function() {
            var selectThis = this;
            var options = selectThis.options;
            
            if (util.isTrue(options["display"])) {
                selectThis.$element.css("display","inline-block");
            }
            else {
                selectThis.$element.css("display","none");
            }
        },
          
        "_updateDisable" : function() {
            var selectThis = this;
            var options = selectThis.options;
            
            if (util.isTrue(options["disable"])) {
                selectThis.$inputField.addClass("tiny-filterselect-input-field-disable");
                selectThis.$inputText.attr("disabled", "true");
                if (options.validate) {
                    selectThis.$inputField.attr( "tiny-valid-disable", true);
                }
            }
            else{
                selectThis.$inputField.removeClass("tiny-filterselect-input-field-disable");
                selectThis.$inputText.removeAttr("disabled");
                if (options.validate) {
                    selectThis.$inputField.removeAttr("tiny-valid-disable");
                }
            }
        },
        
        // 只设置最里边的input的宽度，同时设置弹出框的宽度，需要减掉的宽度见 CONST_VALUES
        "_updateWidth" : function() {
            var selectThis = this;
            var options = selectThis.options;
            var elementWidth = parseInt(options["width"], CONST_VALUES.DECIMAL);
            
            elementWidth = Math.max(elementWidth, CONST_VALUES.MIN_WIDTH);
            selectThis.$inputText.css("width", (elementWidth - CONST_VALUES.CALC_INPUT_WIDTH) + "px");
            selectThis.$spanText.css("max-width",  elementWidth + "px");
        },
        
        "_updateHeight" : function() {
            var selectThis = this;
            var options = selectThis.options;
            var height = parseInt(options["height"], CONST_VALUES.DECIMAL);
            
            selectThis.$popup.css("max-height",height);
        },
        
        "_updateValues" : function() {
            var selectThis = this;
            var options = selectThis.options;
            var optionHtml = "";
            var selectId,img,labelText,checked;
            var checkedArray = [];
            var idIndex = 1; 
            
            // 更新下拉菜单内容
            selectThis.$popup.empty();
            _.each(options["values"], function(val) {
                if (val["selectId"] || val["selectId"] == "") {
                    selectId = val["selectId"];
                }
                else {
                    selectId = "tinydefaultid" + idIndex;
                    val["selectId"] = selectId;
                }
                img = val["image"] || "";
                labelText = val["label"] || "";
                checked = val["checked"] || selectThis._isDefaultSelected(selectId);
                optionHtml += selectThis._optionHtmlFactory(selectId,img,labelText,checked);
                
                if (checked) {
                    checkedArray.push(selectId + "");
                }
                idIndex++;
            });
            selectThis.$popup.append(optionHtml);
            
            // 记录并规范化已选择内容
            selectThis._recordToSelected(checkedArray);
            // 根据selectThis.selected更新文本框
            selectThis._setInput(); 
        },
        
        // 将参数中的项目记录到selectThis.selected, 并进行单选判定和多选冗余删除
        "_recordToSelected" :function(checkedArray) {
            var selectThis = this;
            var options = selectThis.options;
            
            selectThis.selected = [];
            // 记录所有已经选择的,单选以第一个为准，多选需删除重复元素
            if ("single" === options["mode"] && checkedArray.length > 0) {
                selectThis.selected = [checkedArray[0]];
            }
            else {
                selectThis.selected = _.uniq(checkedArray);
            }
        },
        
        // 判断是否在default-selectid属性中指定选择，id可能是整数输入，判断是只用两个等号
        "_isDefaultSelected" : function(selectId) {
            var selectThis = this;
            var options = selectThis.options;
            var selectidArray=options["default-selectid"].split(",");
            
            for(var i = 0; i < selectidArray.length; i++) {
                if (selectId == selectidArray[i]) {
                    return true;
                }
            }
            return false;
        },
        
        // 根据selectThis.selected数组更新文本框.多选需要按照values顺序排列已选项目
        "_setInput" : function() {
            var selectThis = this;
            var options = selectThis.options;
            var checkedtext = [];
            
            _.each(options["values"], function(val) {
                if(selectThis._isIdSelected(val.selectId)) {
                    checkedtext.push(val.label || "");
                }
            });
            selectThis.$inputText.val(checkedtext.join(','));
            selectThis.$spanText.text(checkedtext.join(','));
        },
        
        // 判断一个id是否已经被选择，id可能使用整数输入，所以判断时只用两个等号
        "_isIdSelected" : function(selectId) {
            var selectThis = this;
            
            for(var i = 0; i < selectThis.selected.length; i++) {
                if (selectId == selectThis.selected[i]) {
                    return true;
                }
            }
            return false;
        },
        
        // 生成一个下拉选项的dom
        "_optionHtmlFactory" : function(selectId, image, label, checked) {
            var selectThis = this, options = this.options;
            var html = '<div class="tiny-filterselect-option">';
            
            // 多选时生成复选框
            if ("multiple" === options["mode"]) {
                html += '<span class="tiny-filterselect-checkbox';
                if (checked) {
                    html += ' tiny-filterselect-checkbox-checked';
                }
                html += '"></span>';
            }
            // 图标是可选项目
            if (image) {
                image = $.trim(image);
                image = $.encoder.encodeForURL(image);
                html += '<img src="' + image + '" />';
            }
            
            selectId = $.encoder.encodeForHTMLAttribute("selectId", selectId + "", true); 
            label = $.encoder.encodeForHTML(label);
            html += '<span selectId="'+selectId+'">' + label + '</span>';
            html += '</div>';
            return html;
        },
        
        "getSelectedId" : function() {
            var selectThis = this;
            var options = selectThis.options;
            
            // 多选返回id数组--可能为空，单选返回第一个id--可能为undefined
            if ("multiple" === options["mode"]) {
                return selectThis.selected;
            }
            else {
                return selectThis.selected[0];
            }
        },
        
        "getSelectedLabel" : function() {
            var selectThis = this;
            
            return selectThis.$inputText.val();
        },
        
        "opChecked" : function(selectedId) {
            var selectThis = this;
            var options = selectThis.options;
            var checkedArray = [];
            
            selectedId += "";
            var selctedIdArray = selectedId.split(",");
            // 记录所选，需检查id合法性.
            _.each(selctedIdArray, function(id) {
                index = selectThis._getOptionIndex(id);
                if (index >= 0) {
                    checkedArray.push(id);
                }
            });
            
            // 记录并规范化已选择内容
            selectThis._recordToSelected(checkedArray);
            // 根据 selectThis.selected更新文本框
            selectThis._setInput(); 
            // 多选时根据 selectThis.selected刷新下拉菜单选择状态
            if ("multiple" === options["mode"]) {
                selectThis._refreshCheckState();
            }
        }, 
        
        // 根据已选择内容，刷新下拉框的选择状态. last/first 对dom结构有依赖,如果修改dom，请注意这里
        "_refreshCheckState" : function() {
            var selectThis = this;
            var id;
            
            selectThis.$option = selectThis.$popup.find(".tiny-filterselect-option");
            selectThis.$option.each(function() {
            	id = $(this).children("span").last().attr("selectid");
            	if (selectThis._isIdSelected(id)) {
            		$(this).children("span").first().addClass("tiny-filterselect-checkbox-checked");
            	}
            	else {
            		$(this).children("span").first().removeClass("tiny-filterselect-checkbox-checked");
            	}
            });
        },        
        
        "opLabel" : function(selectId,value) {
            if (0  === arguments.length) {
                return;
            }
            
            var selectThis = this;
            var opindex = selectThis._getOptionIndex(selectId);
            if (opindex < 0) {
                return;
            }
            
            var options = selectThis.options;
            var values = options["values"];
            if (_.isUndefined(value)) {
                return values[opindex].label;
            }
            // 设置，同时改变了 values中的内容
            selectThis.$popup.find('[selectId ="'+selectId+'"]').text(value);
            values[opindex].label = value;
            
            //若该选项已选，则将input框中内容进行重置
            if (selectThis._isIdSelected(selectId)) {
                selectThis._setInput();
            }
        },
        
        // 根据selectId搜寻选项下标，id可能是整数输入，所以只用两个等号
        "_getOptionIndex" : function(selectId) {
            var selectThis = this;
            var options = selectThis.options;
            var values = options["values"];
            
            for(var i = 0; i < values.length; i++) {
                if (selectId == values[i].selectId) {
                    return i;
                }
            }
            return -1;
        },
        
        // 多选时刷新已选项目，记录到  selectThis.selected
        "_getSelectedLabels" : function() {
            var selectThis = this;
            selectThis.selected = [];
            
            selectThis.$option = selectThis.$popup.find(".tiny-filterselect-option");
            selectThis.$option.each(function() {
                if ($(this).children("span").first().hasClass("tiny-filterselect-checkbox-checked")) {
                    selectThis.selected.push($(this).children("span").last().attr("selectid"));
                }
            });
        },

        "_locateElement" : function() {
            var selectThis = this;
            
            selectThis.$element = selectThis._element;
            selectThis.$inputField = selectThis._element.find(".tiny-filterselect-input-field");
            selectThis.$inputText = selectThis._element.find(".tiny-filterselect-text");
            selectThis.$spanText = selectThis._element.find(".tiny-filter-select-span");
        },
        
        // 目前校验类事件的触发同菜单收起下拉在逻辑上一致。但要注意，由于blurEvt需要放在更新文本框之后，要求_unpop必须在更新文本框之后
        "_pop" : function() {
            var selectThis = this;
            var position = selectThis.$inputField.offset();
            
            selectThis.$popup.css({"left":position.left,"top" : position.top + CONST_VALUES.CALC_POP_OFFSET,"display": "block"});
            selectThis.$inputField.addClass("tiny-filterselect-input-field-selected");
            
            selectThis._setOptionWidth();
            
            selectThis.$inputText.trigger("focusEvt",[]);
        },
        "_unpop" : function() {
            var selectThis = this;
            
            selectThis.$popup.css("display", "none");
            selectThis.$inputField.removeClass("tiny-filterselect-input-field-selected");
            
            selectThis.$inputText.trigger("blurEvt",[]);
        },
        
        // 选项div使用table-caption属性实现自动同最长的对齐。这里设置一下所有选项最小宽度，使背景至少能够充满整个下拉框。
        "_setOptionWidth" : function() {
        	var selectThis = this;
        	
        	var allOption=selectThis.$popup.find(".tiny-filterselect-option");
        },

        "_addBehavior" : function() {
            var selectThis = this;
            var options = selectThis.options;
            selectThis._locateElement();
            var popFlag = false; // 标明鼠标点击是来源于点击控件自己的下拉
            
            // 点击鼠标的下拉与收起，收起时会触发校验
            selectThis.$inputField.on("mousedown", function(evt) {
                popFlag = true;
                if (util.isTrue(options["disable"])) {
                    return;
                }
                if ("none" == selectThis.$popup.css("display")) {
                    selectThis._pop();
                }
                else {
                    selectThis._unpop();
                }
            });
            
            // 下拉选择框选项点击与划过事件
            selectThis.$popup.on("mousedown", ".tiny-filterselect-option", function(evt) {
                var oldValues = selectThis.$inputText.val();
                var newValues = evt.currentTarget.textContent || evt.currentTarget.innerText;
                
                if ("single" === options["mode"]) {
                    // 选择相同选项,不进行处理,仅收起下拉框
                    if(oldValues === newValues) {
                        selectThis._unpop();
                    	return;
                    }
                    // 记录选中的id,更新文本框
                    selectThis.selected = [$(this).children("span").last().attr("selectid")];
                    selectThis._setInput();
                    // 收起下拉框
                    selectThis._unpop();
                    // 触发change事件
                    selectThis.$inputText.trigger("changeEvt", [evt]);
                    return;
                }
                // 变换复选框选中状态
                var checkboxElement = $(this).children("span").first();
                if (checkboxElement.hasClass("tiny-filterselect-checkbox-checked")) {
                    checkboxElement.removeClass("tiny-filterselect-checkbox-checked");
                }
                else {
                    checkboxElement.addClass("tiny-filterselect-checkbox-checked");
                }
                // 更新选中的id列表,并更新文本框
                selectThis._getSelectedLabels();
                selectThis._setInput();
                
                // 多选框点击选项，内容必然改变，触发change事件
                selectThis.$inputText.trigger("changeEvt", [evt]);
                evt.stopPropagation();
                
            }).on("mousemove", ".tiny-filterselect-option", function(evt) {
                $(this).addClass("tiny-filterselect-option-selected");
                $(this).children().first().addClass("tiny-filterselect-checkbox-focus");
                
            }).on("mouseleave", ".tiny-filterselect-option", function(evt) {
                $(this).removeClass("tiny-filterselect-option-selected");
                $(this).children().first().removeClass("tiny-filterselect-checkbox-focus");
            });
            
            // 阻止滚动鼠标，下拉框消失
            selectThis.$popup.on("mousewheel DOMMouseScroll", ".tiny-filterselect-option", function(e) {
                e.stopPropagation();
            });
            
            // 内容改变触发外部事件
            selectThis.$inputText.on("changeEvt", function(evt) {
                selectThis.trigger("change", [evt]);
                if ("function" == ( typeof options["change"])) {
                    options["change"](evt);
                }
            });
            
            // 外围事件引起的下拉框收起
            $(window).on("resize",function() {
                if ("block" == selectThis.$popup.css("display")) {
                    selectThis._unpop();
                }
            });
            $(document).on("keydown", function(evt) {
                if ((27 == evt.keyCode) && ("block" == selectThis.$popup.css("display"))) {
                    selectThis._unpop();
                }
            }).on("mousedown mousewheel DOMMouseScroll", function(evt) {
                if (popFlag) {
                    popFlag = false;
                    return;
                }
                // 阻止拖动滚动条和在滚动条上滚鼠标的时候关闭弹出框
                if ($(evt.target).hasClass("tiny-filterselect-popup-container")) {
                    return;
                }
                if ("block" == selectThis.$popup.css("display")) {
                    selectThis._unpop();
                }
            });
        }
    });
    
    return Select; 
});
