function setupFragmentEditor() {
	var $window = $(window);
	var $editorFrame = $("#editor-frame");
	
	$("#new-fragment-editor-trigger, .new-fragment-editor-trigger-on-toolbar").click(function (e) {
		onClickFragmentEditorTrigger($editorFrame, $window, e, MSG.label_new_fragment, true);
    });
	
    $editorFrame.resizable({
        alsoResize: "#markItUpFragment-content-editor, #fragment-content-editor",
        minWidth: 581,
        minHeight: 234,
    });
	
	$editorFrame.draggable({
    	handle:"#editor-title-bar, #editor-button-bar"
    	, cursor:"move"
    });
    
    $("#editor-title-bar").dblclick(function() {
    	toggleWindow($editorFrame, $(this));
    });
	
    $("#fragment-content-editor").markItUp(markItUpSettings, {});
}

function onClickFragmentEditorTrigger($editorFrame, $window, e, title, forNewFragment) {
	var editorHeight = Math.max($editorFrame.height(), 596);
    var top = ($window.height() - editorHeight) / 2 + $window.scrollTop() + "px";
    var titleInput = $("#fragment-editor-form\\:title-input");
    if (forNewFragment) {
    	var el = titleInput;
        el.val(null);
        el.watermark(MSG.label_title);
        
        el = $("#fragment-editor-form\\:tags-input");
        el.val(null);
        el.watermark(MSG.how_to_input_tags);
        
        $("#fragment-content-editor").val(null);
    }
    
    $editorFrame.lightbox_me({
        centered:false
        , showOverlay:false
        , lightboxSpeed:"slow"
        , closeSelector:"#editor-frame-close-button"
        , closeEsc:false
        , modalCSS:{ position:"absolute", top:top, left:"30%" }
        , onClose:clearFragmentEditor
    });
    
    titleInput.focus();
    
    var titleBar = $("#editor-title-bar");
    if (titleBar.next().is(":visible") == false) {
    	toggleWindow($editorFrame, titleBar);
    }
    
    $("#editor-title").text(title);
    
    e.preventDefault();
}

function clearFragmentEditor() {
    $("#fragment-content-editor").val(null);    
    $("#fragment-editor-form\\:id-placeholder-for-fragment").val(null);
    
    // If the preview pop-up is open, close it
    var frgOverlay = $("#fragment-overlay");
    if (frgOverlay.find(".fragment-header").length == 0) {
        frgOverlay.trigger("close");
    }
    
    // [NOTE] without the following code,
    // jQuery UI Autocomplete object won't disapper even after its parent is closed
    $("#fragment-editor-form\\:tags-input").autocomplete("widget").hide();
}

function openFragmentEditorForEdit(e) {
	var menu = $("#frg-context-menu");
	var target = menu.data("target-frg");
	var fragmentId = target.attr("_fid");
	var panelId = findPanel(target);
    
	$("#fragment-editor-form\\:id-placeholder-for-fragment").val(fragmentId);
    
    var el = target.find(".-cvz-data-title");
    $("#fragment-editor-form\\:title-input").val(
            el.text().replace(/&lt;/g, '<').replace(/&gt;/g, '>'));
    
    el = target.find(".-cvz-data-tags");
    $("#fragment-editor-form\\:tags-input").val(el.text());
    
    el = target.find(".-cvz-data-content");
    $("#fragment-content-editor").val(el.text());
    
    var $window = $(window);
	var $editorFrame = $("#editor-frame");
    onClickFragmentEditorTrigger($editorFrame, $window, e, MSG.label_edit, false);
}

function openFragmentEditorToRelate(e) {
    var menu = $("#frg-context-menu");
    var target = menu.data("target-frg");
    var frgId = target.attr("_fid");
    addSubmitParam($("#fragment-editor-form"), {relatedFrgId:frgId}, true);
    var $window = $(window);
    var $editorFrame = $("#editor-frame");
    var title = MSG.label_new_fragment+" ( <=> #"+frgId+" )";
    onClickFragmentEditorTrigger($editorFrame, $window, e, title, true);
}

function prepareFragmentContent() {
    var $editor = $("#fragment-content-editor");
    var srcContent = $editor.val();
    $("#fragment-editor-form\\:content-placeholder").val(srcContent);
    var titleInput = $("#fragment-editor-form\\:title-input");
    var title = titleInput.val().replace(/</g, '&lt;').replace(/>/g, '&gt;');
    titleInput.val(title);
    return srcContent;
}

function previewFragment() {
	var overlayFrame = $("#fragment-overlay");
	overlayFrame.lightbox_me({
        centered:false
        , showOverlay:false
        , lightboxSpeed:"fast"
        , closeSelector:"#fragment-overlay-close-button"
        , closeEsc:true
        , modalCSS:{ position:"fixed", bottom: '2%', right: '2%' }
    });
	
	$("#fragmen-overlay-back-button").hide();
    
	$("#fragment-overlay-title").text(MSG.label_preview);
	
	var srcContent = prepareFragmentContent();
    var outputHtml = $("<span class='fragment-content'>").wrapInner(translateFragmentContent(srcContent));
    postprocessFragmentContent($("#fragment-overlay-content").html(outputHtml));
    
    var titleBar = $("#fragment-overlay-title-bar");
    if (titleBar.next().is(":visible") == false) {
    	toggleWindow(overlayFrame, titleBar);
    }
}

function validateTagNames() {
    var tagNames = $("#fragment-editor-form\\:tags-input").val();
    var invalidChars = ["\\", ":"];
    for (var i=0; i<invalidChars.length; ++i) {
        if (tagNames.indexOf(invalidChars[i]) > -1) {
            return invalidChars[i];
        }
    }
    return null;
}

function onSubmitFragment() {
    prepareFragmentContent();
    var valid = true;
    if (!$("#fragment-editor-form\\:title-input").val()) {
        showError(MSG.title_empty);
        valid = false;
    }
    else {
        var invalidChar = validateTagNames();
        if (invalidChar) {
            showError("'" + invalidChar + MSG.cant_use_for_tags);
            valid = false;
        }
    }
    
    if (valid)
        document.forms["fragment-editor-form"]["fragment-editor-form:save-fragment-btn"].click()
}

function autocompleteForTypingTags() {    
    var pfThemeBugFix = false;
    var theme = localStorage.getItem("theme");
    if (theme && (theme == "afterdark" || theme == "afterwork"))
        // [BUG] A focused item gets invisible over these themes.
        pfThemeBugFix = true;
    
	var tagSuggestions = [];
    $("#tag-palette-flat .each-tag").each(function (index, value) {
        tagSuggestions.push($(value).find(".each-tag-name").text());
    });
    
    function split(input) {
        return input.trim().split(/"?\,\s*"?/);
    }
    
    function alreadyTyped(typedTags, suggestion) {
    	for (var i=0; i<typedTags.length; ++i) {
    		if (typedTags[i].replace(/"/g, '') == suggestion) return true;
    	}
    	return false;
    }
    
    function Options() {
        var opts = this;

        // [extended option] check whether autocomplete can be invoked. it is overridable.
        this.allow = function(input) {
            return input;
        };
        
        // [jQuery.ui.autocomplete option] displaying suggestions.
        this.source = function(req, res) {
            var input = opts.allow(req.term);
            if (!input)
                return;
            var tags = split(input);
            var typed = null;
            if (tags.length) {
                typed = tags[tags.length-1].replace(/"/g, '');
            }
            var output = [];
            if (! typed) {
                res([]); // no typing, no suggestion.
                return;
            }
            for (var i=0; i<tagSuggestions.length; ++i) {
                var suggestion = tagSuggestions[i];
                if (suggestion.indexOf(typed) == 0 && ! alreadyTyped(tags, suggestion)) {
                    // filter out:
                    // - tags not starting with the typed word
                    // - already typed tags
                    output.push(suggestion);
                }
            }
            res(output);
        };
        
        // [jQuery.ui.autocomplete option] callback invoked on the focused item.
        this.focus = function () {
            if (pfThemeBugFix) {
                var acMenu = $("ul.ui-autocomplete");
                acMenu.find("li.ui-weak-focus").removeClass("ui-weak-focus");
                acMenu.find("li.ui-state-focus").addClass("ui-weak-focus");
            }
            
            // prevent value inserted on focus
            return false;
        };
        
        // [jQuery.ui.autocomplete option] callback invoked when the user has selected an item.
        this.select = function (e, ui) {
            var items = split(this.value.substring(opts.startIdx));
            
            // remove the current input (being typed)
            items.pop();
            
            // add the selected item
            items.push(ui.item.value);
            
            // normalize all the items so far
            var output = "";
            for (var i=0; i<items.length; ++i) {
                var item = items[i];
                if (item.indexOf(" ") > -1) {
                    // double quote any item with space characters included
                    if (item.charAt(0) != '"')
                        item = '"' + item;
                    if (item.charAt(item.length-1) != '"')
                        item = item + '"';
                }
                // separate each item with comma-and-space
                output += item + ", ";
            }
            this.value = this.value.substring(0, opts.startIdx) + output;
            
            return false;
        };
        
        // [extended option] autocomplete can be applicable to the string starting at this index.
        this.startIdx = 0;
        
        // [jQuery.ui.autocomplete option] generates focus on an item when appearing.
        this.autoFocus = true;
    }; // function Options()
   
    $("#fragment-editor-form\\:tags-input, #fragment-group-form\\:search-panel\\:tag-keywords").autocomplete(new Options());
    
    var anotherOpts = new Options();
    anotherOpts.allow = function(input) {
        var idx = input.lastIndexOf("tag:");
        if (idx == -1)
            return null;
        anotherOpts.startIdx = idx + 4;
        return input.substring(anotherOpts.startIdx);
    };
    $("#fragment-group-form\\:search-panel\\:quick-search-input, #last-search-phrase-0 input, #last-search-phrase-1 input, #last-search-phrase-2 input")
        .autocomplete(anotherOpts);
}
