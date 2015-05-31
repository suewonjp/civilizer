function setupTabViewsForTagPalette() {
	var currentTagGroupTabIndex = "0";

    $tab = $( "#tag-palette-panel" ).tabs({
         activate : function (e, ui) {
            currentTagGroupTabIndex = ui.newTab.index().toString();
            sessionStorage.setItem('tag-palette-tab-index', currentTagGroupTabIndex);
         }
    });

    if (sessionStorage.getItem('tag-palette-tab-index')) {
        currentTagGroupTabIndex = sessionStorage.getItem('tag-palette-tab-index');
        $tab.tabs('option', 'active', currentTagGroupTabIndex);
    }
}

function setContextMenuForTags() {
   	var menu = $("#tag-context-menu");
   	
	$(".each-tag").bind("contextmenu", function(event) {
    	menu.css({ left:event.pageX, top:event.pageY }).show();
    	var target = $(event.target).closest(".each-tag");
    	menu.data("target-tag", target);
    	$("#tag-palette-form\\:id-placeholder-for-tag").val(target.attr("_tid"));
    	event.preventDefault();
    });

    $(document).bind("click", function(event) {
    	menu.hide();
    });
}

function onExpandComplete() {
	setContextMenuForTags();
	setupDraggableForTags();
}

function showTagEditor() {
	var dlg = PF("tagEditor");
	dlg.show();
	
	var menu = $("#tag-context-menu");
	var target = menu.data("target-tag");
	var tagName = target.find(".each-tag-name").text();
	if (tagName == "") {
		tagName = "~unnamed~"
	}
	
	var inplace = PF("tagNameInplace");
	var display = inplace.jq.find(".ui-inplace-display");
	display.text(tagName)[0].title=MSG.click_to_rename;
	var input = inplace.jq.find(".ui-inplace-content input");
	input.val(tagName);
	inplace.hide();
	
	function abortInput(val) {
		input.val(tagName);
		display.text(tagName);
	}
	
	function commitInput() {
		var val = input.val();
		if (! val || val == "") {
			abortInput(val);
		}
		else {
			display.text(val);
		}
		inplace.hide();
	}
	
	dlg.jq.find("#tag-palette-form\\:tag-name-panel").click(function(e) {
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

function showTagInfo() {
    var dlg = PF("tagInfoDlg");
    dlg.show();
    
    var menu = $("#tag-context-menu");
    var target = menu.data("target-tag");
    var tagName = target.find(".each-tag-name").text();
    if (tagName == "") {
        tagName = "~unnamed~"
    }
    
    dlg.jq.find("._tag-name").text(" "+tagName);
}
