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

function addToggler(target, toggler) {
    var collapseIcon = "fa-minus-square";
    var expandIcon = "fa-plus-square";
    var link = $("<a>").attr("href", "#");
    var icon = $("<span>").addClass("fa " + collapseIcon);
    link.prepend(icon).click(function (event) {
        toggler();
        icon.toggleClass(collapseIcon + " " + expandIcon);
        event.preventDefault();
    }); 
    target.before(link);
    return link;
}

function toggleWindow(frame, bar) {
    var target = bar.next();
    if (target.is(":visible")) {
        if (frame.hasClass("ui-resizable"))
            frame.resizable("disable");
        target.hide();
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
    
    function abortInput(val) {
        input.val(text);
        display.text(text);
    }
    
    function commitInput() {
        var val = input.val().trim();
        if (val) {
            display.text(val);
            if ($.isFunction(onCommit))
                onCommit(val, text);
        }
        else {
            abortInput(val);
        }
        pfInplace.hide();
    }
    
    jqOuterBox.click(function(e) {
        if (e.target != input[0] && input.is(":visible")) {
            commitInput();
        }
    });
    
    input.keypress(function(e) {
        // [TODO] key check should be in a cross-browser way
        if (e.keyCode == 13 && input.is(":visible")) {
            commitInput();
            e.preventDefault();
            return false;
        }
    });
}

function disableAutoSubmitOnEnterForForms(jqForms) {
    jqForms.off('keypress.disableAutoSubmitOnEnter').on('keypress.disableAutoSubmitOnEnter', function(event) {
        if (event.which === $.ui.keyCode.ENTER && $(event.target).is(':input:not(:button,:submit,:reset)')) {
            event.preventDefault();
        }
    });
}

//function boolToSign(b) {
//    return b * 2 - 1;
//}

