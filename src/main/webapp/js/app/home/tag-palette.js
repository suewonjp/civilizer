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
	    showPopup(menu, event);
    	var target = $(event.target).closest(".each-tag");
    	menu.data("target-tag", target);
    	var tid = target.attr("_tid");
    	if (tid <=0) {
    	    // special tags; not modifiable
    	    $("#tag-palette-form\\:edit").hide();
    	    $("#tag-palette-form\\:trash").hide();
    	}
    	else {
    	    $("#tag-palette-form\\:edit").show();
    	    $("#tag-palette-form\\:trash").show();
    	}
    	$("#tag-palette-form\\:id-placeholder-for-tag").val(tid);
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
	
	initPfInplaceWidget(PF("tagNameInplace"), tagName, dlg.jq.find("#tag-palette-form\\:tag-name-panel"));
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
