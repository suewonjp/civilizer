function isImage(fileExt) {
    return (fileExt == ".png")
        || (fileExt == ".jpg")
        || (fileExt == ".gif")
        || (fileExt == ".bmp")
        || (fileExt == ".ico")
        || (fileExt == ".svg")
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
    var link = $("<a href='#'>"),
        icon = $("<span class='fa fa-minus-square "+ iconClass + "'>");
    link.prepend(icon);
    if (typeof toggler === "function") {
        link.off("click").on("click", function (e) {
            toggler();
            $(this).find("." + iconClass).toggleClass("fa-plus-square fa-minus-square");
            e.preventDefault();
        });
    }
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
        if (bar.hasClass("ui-draggable-handle"))
            frame.draggable("disable");
        target.hide();
        frame.css({width:"auto", height:"auto"});
    }
    else {
        if (frame.hasClass("ui-resizable"))
            frame.resizable("enable");
        if (bar.hasClass("ui-draggable-handle"))
            frame.draggable("enable");
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

function showPopup(target, e) {
    var x = e.clientX, y = e.clientY;
    var tx = target.width(), ty = target.height();
    var size = getViewportSize();
    x -= Math.max(x + tx - size.width, 0);
    y -= Math.max(y + ty - size.height, 0);
    target.css({ left:x, top:y }).show();
}

function showOrHide(target, letItShow) {
    if ((letItShow === undefined || letItShow === null) && target.is) {
        letItShow = (target.is(":visible") === false);
    }
    
    if (letItShow)
        target.show();
    else
        target.hide();
    return letItShow;
}

function setupPfInplaceText(pfInplace, text, onCommit) {
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
        }
        else {
            input.val(text);
            display.text(text);
            val = text;
        }
        if ($.isFunction(onCommit))
            onCommit(val, text);
        pfInplace.hide();
    }
    
    input.off("blur.utils")
    .on("blur.utils", function(e) {
        commitInput();
    })
    
    input.off("keypress.pfit").on("keypress.pfit", function(e) {
        if (e.which == $.ui.keyCode.ENTER && input.is(":visible")) {
            commitInput();
            e.preventDefault();
            return false;
        }
    });
}

function disableAutoSubmitOnEnterForForms() {
    for (var i=0; i<arguments.length; ++i) {
        $(arguments[i]).off('keypress.disableAutoSubmitOnEnter').on('keypress.disableAutoSubmitOnEnter', function(e) {
            if (e.which === $.ui.keyCode.ENTER && $(e.target).is(':input:not(textarea,:button,:submit,:reset)')) {
                e.preventDefault();
            }
        });
    }
}

function addSubmitParam(jqForms, params, clearBeforeAdding) {
    for (var key in params) {
        if (clearBeforeAdding)
            jqForms.find("input[type=hidden][name='"+key+"']").remove();
        jqForms.append("<input class='ui-submit-param' type='hidden' name='"+key+"' value='"+params[key]+"'/>");
    }
}

function selectItemOnPfListbox(value, pfListbox) {
    for (var i=0,len=pfListbox.options.length; i<len; ++i) {
        if (pfListbox.options.eq(i).text() === value) {
            pfListbox.selectItem(pfListbox.items.eq(i));
            break;
        }
    }
    
//    var item = $("<div>").append($(pfListbox.items).clone()).find("li[data-label='"+value+"']")
//    pfListbox.selectItem(item);
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

function getFileName(path) {
    var output = path;
    var iii;
    iii = output.lastIndexOf("/");
    if (iii > -1)
        output = output.substring(iii+1);
    iii = output.lastIndexOf("\\");
    if (iii > -1)
        output = output.substring(iii+1);
    return output;
}

function parseJsonArgs(obj) {
    var argStr = obj instanceof jQuery ? obj.attr("args") : obj;
    if (typeof argStr === "string") {
        try {
            return JSON.parse(argStr.replace(/&quot;/g,'"'));
        } catch (e) {
            console.error("[Civilizer] JSON.parse() failed! : " + argStr);
        }
    }
    return {};
}

function prefix(str, sep) {
    // prefix("foo.bar.txt", ".") => foo
    return str && str.substring(0, str.indexOf(sep));
}

function suffix(str, sep, excludeSep) {
    // suffix("foo.bar.txt", ".") => .txt
    // suffix("foo.bar.txt", ".", true) => txt
    return str && str.substring(str.lastIndexOf(sep) + (excludeSep === true ? sep.length : 0));
}

function inbetween(str, sep0, sep1) {
    // inbetween("foo.bar.txt", ".", ".") => bar
    // inbetween("foo.txt", ".", ".") => null
    if (!str)
        return null;
    var idx = str.indexOf(sep0);
    if (idx < 0)
        return null;
    str = str.substring(idx + sep0.length);
    idx = str.lastIndexOf(sep1);
    if (idx < 0)
        return null;
    return str.substring(0, idx);
}

function jsonp(url, successcb) {
    $.ajax({
        type:"get",
        url: url,
        dataType: 'jsonp',
        success: successcb // (data, textStatus, jqXHR)
    });
}

