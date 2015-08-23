function isImage(fileExt) {
    return (fileExt == ".png")
        || (fileExt == ".jpg")
        || (fileExt == ".gif")
        || (fileExt == ".bmp")
        || (fileExt == ".ico")
        ;
}

function hasAnyClass(obj, classes) {
    for (var i=0; i<classes.length; ++i) {
        if (obj.hasClass(classes[i])) {
            return true;
        }
    }
    return false;
}

function makeObjectsInsertableToTextArea() {
    // [NOTE] original source code can be found at:
    // http://skfox.com/2008/11/26/jquery-example-inserting-text-with-drag-n-drop/
    $.fn.insertAtCaret = function (myValue) {
        return this.each(function(){
            //IE support
            if (document.selection) {
                this.focus();
                sel = document.selection.createRange();
                sel.text = myValue;
                this.focus();
            }
            //MOZILLA / NETSCAPE support
            else if (this.selectionStart || this.selectionStart == '0') {
                var startPos = this.selectionStart;
                var endPos = this.selectionEnd;
                var scrollTop = this.scrollTop;
                this.value = this.value.substring(0, startPos)+ myValue+ this.value.substring(endPos,this.value.length);
                this.focus();
                this.selectionStart = startPos + myValue.length;
                this.selectionEnd = startPos + myValue.length;
                this.scrollTop = scrollTop;
            }
            else {
                this.value += myValue;
                this.focus();
            }
        });
    };
}

function addToggler(target, iconClass, toggler) {
    var collapseIcon = "fa-minus-square";
    var expandIcon = "fa-plus-square";
    var link = $("<a>").attr("href", "#");
    var icon = $("<span>").addClass(iconClass + " fa " + collapseIcon);
    link.prepend(icon).click(function (event) {
        toggler();
        icon.toggleClass(collapseIcon + " " + expandIcon);
        event.preventDefault();
    }); 
    target.before(link);
    return link;
}

function initTogglerIcon(target) {
    var collapseIcon = "fa-minus-square";
    var expandIcon = "fa-plus-square";
    target.prev("a").find("span").removeClass(expandIcon).addClass(collapseIcon);
}

function toggleWindow(frame, bar) {
    var target = bar.next();
    if (target.is(":visible")) {
        if (frame.hasClass("ui-resizable"))
            frame.resizable("disable");
        target.hide();
        frame.css({width:"auto", height:"auto"});
    }
    else {
        if (frame.hasClass("ui-resizable"))
            frame.resizable("enable");
        target.show();
    }
}

function parseMarkdown(inputText) {
    return marked(inputText);
}

function getViewportSize() {
    var pageWidth = window.innerWidth,
        pageHeight = window.innerHeight;

    if (typeof pageWidth != "number"){
        if (document.compatMode == "CSS1Compat"){
            pageWidth = document.documentElement.clientWidth;
            pageHeight = document.documentElement.clientHeight;
        } else {
            pageWidth = document.body.clientWidth;
            pageHeight = document.body.clientHeight;
        }
    }
    
    return {width:pageWidth, height:pageHeight};
}

function showPopup(target, event) {
    var x = event.clientX, y = event.clientY;
    var tx = target.width(), ty = target.height();
    var size = getViewportSize();
    x -= Math.max(x + tx - size.width, 0);
    y -= Math.max(y + ty - size.height, 0);
    target.css({ left:x, top:y }).show();
}

function showOrHide(target, letItShow) {
    letItShow ? target.show() : target.hide();
}

function initPfInplaceWidget(pfInplace, text, jqOuterBox, onCommit) {
    var display = pfInplace.jq.find(".ui-inplace-display");
    display.text(text)[0].title=MSG.click_to_rename;
    var input = pfInplace.jq.find(".ui-inplace-content input");
    input.val(text);
    pfInplace.hide();
    
    function commitInput() {
        var val = input.val().trim();
        if (val) {
            input.val(val);
            display.text(val);
            if ($.isFunction(onCommit))
                onCommit(val, text);
        }
        else {
            input.val(text);
            display.text(text);
        }
        pfInplace.hide();
    }
    
    jqOuterBox.off("click").on("click", function(e) {
        if (e.target != input[0] && input.is(":visible")) {
            commitInput();
        }
    });
    
    input.off("keypress").on("keypress", function(e) {
        // [TODO] key check should be in a cross-browser way
        if (e.which == $.ui.keyCode.ENTER && input.is(":visible")) {
            commitInput();
            e.preventDefault();
            return false;
        }
    });
}

function disableAutoSubmitOnEnterForForms(jqForms) {
    jqForms.off('keypress.disableAutoSubmitOnEnter').on('keypress.disableAutoSubmitOnEnter', function(event) {
        if (event.which === $.ui.keyCode.ENTER && $(event.target).is(':input:not(textarea,:button,:submit,:reset)')) {
            event.preventDefault();
        }
    });
}

function addSubmitParam(jqForms, params, clearBeforeAdding) {
    for (var key in params) {
        if (clearBeforeAdding)
            jqForms.find("input[type=hidden][name='"+key+"']").remove();
        jqForms.append("<input class='ui-submit-param' type='hidden' name='"+key+"' value='"+params[key]+"'/>");
    }
}

function selectItemOnPfListbox(value, pfListbox) {
    for (var i=0; i<pfListbox.options.length; ++i) {
        var option = pfListbox.options.eq(i);
        if (option.text() === value) {
            var item = pfListbox.items.eq(i);
            pfListbox.selectItem(item);
            return;
        }
    }
}

function parentChildFolders(parent, child) {
    // let parent = '/folder 0/folder 1'
    // let child = '/folder 0/folder 1/folder 2'
    // then, this function will return true
    var iii = child.indexOf(parent);
    return (iii != 0) ?
          false : (child.charAt(parent.length) === SYSPROP.fileSep ? true : false);  
}

//function boolToSign(b) {
//    return b * 2 - 1;
//}

