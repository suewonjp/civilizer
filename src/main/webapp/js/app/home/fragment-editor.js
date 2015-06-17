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
	var editorHeight = Math.max($editorFrame.height(), 696);
    var top = ($window.height() - editorHeight) / 2 + $window.scrollTop() + "px";
    
    if (forNewFragment) {
    	var el = $("#fragment-editor-form\\:title-input");
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

function openFragmentEditorForEdit(event) {
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
    onClickFragmentEditorTrigger($editorFrame, $window, event, MSG.label_edit, false);
}

function openFragmentEditorToRelate(event) {
    var menu = $("#frg-context-menu");
    var target = menu.data("target-frg");
    var frgId = target.attr("_fid");
    addSubmitParam($("#fragment-editor-form"), {relatedFrgId:frgId}, true);
    var $window = $(window);
    var $editorFrame = $("#editor-frame");
    var title = MSG.label_new_fragment+" ( <=> #"+frgId+" )";
    onClickFragmentEditorTrigger($editorFrame, $window, event, title, true);
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

function autocompleteForTypingTags() {
	var tagSuggestions = [];
    $("#tag-palette-flat .each-tag").each(function (index, value) {
        tagSuggestions.push($(value).find(".each-tag-name").text());
    });
    
    var split = function (input) {
        return input.trim().split(/\,\s*/);
    }
    
    function alreadyTyped(typedTags, suggestion) {
    	for (var i=0; i<typedTags.length; ++i) {
    		if (typedTags[i] == suggestion) return true;
    	}
    	return false;
    }
    
    $("#fragment-editor-form\\:tags-input, #fragment-group-form\\:search-panel\\:tag-keywords").autocomplete({
        source: function(req, res) {
            var tags = split(req.term);
            if (tags.length && tags[tags.length-1] == "") {
                tags.pop();
            }
            var typed = tags[tags.length-1];
            tags.pop();
            var prefix = "";
            for (var i=0; i<tags.length; ++i) {
                prefix += tags[i] + ",";
            }
            var output = [];
            for (var i=0; i<tagSuggestions.length; ++i) {
                var suggestion = tagSuggestions[i];
                if (suggestion.indexOf(typed) == 0 && ! alreadyTyped(tags, suggestion)) {
                    output.push(suggestion);
                }
            }
            res(output);
        },
        focus: function () {
            // prevent value inserted on focus
            return false;
        },
        select: function (event, ui) {
            var terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push(ui.item.value);
            // add placeholder to get the comma-and-space at the end
            this.value = terms.join(", ") + ", ";
            return false;
        },
        autoFocus: true,
    });
}
