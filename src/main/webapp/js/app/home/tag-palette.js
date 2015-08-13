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

function onExpandComplete() {
	setContextMenuForTags();
	setupDraggableForTags();
}

function showTagEditorForCreating() {
    var saveBtn = PF("tagEditorSaveBtn");
    saveBtn.disable();
    var inplace = PF("tagNameInplace");
    inplace.hide();
    var input = inplace.jq.find(".ui-inplace-content input");
    inplace.jq.find(".ui-inplace-display").text(MSG.name_required);
    input.val("").off("change").on("change", function() {
            if ($(this).val().trim())
                saveBtn.enable();
            else
                saveBtn.disable();
        })
        .off("keypress.newTagName").on("keypress.newTagName", function(e) {
            if (e.which == $.ui.keyCode.ENTER) $(this).change();
        });
    var dlg = PF("tagEditor");
    dlg.jq.find("input[name=isNewTag]").val(true);
    dlg.show();
}

function showTagEditorForEditing() {
	var dlg = PF("tagEditor");
	
	var menu = $("#tag-context-menu");
	var target = menu.data("target-tag");
	var tagName = target.find(".each-tag-name").text();
	if (tagName == "") {
		tagName = "???";
	}
	
	initPfInplaceWidget(
	        PF("tagNameInplace")
	        , tagName
	        , dlg.jq.find("#tag-palette-form\\:tag-name-panel")
	);

	dlg.jq.find("input[name=isNewTag]").val(false);
	dlg.show();
}

function showTagInfo() {
    var dlg = PF("tagInfoDlg");
    dlg.show();
    
    var menu = $("#tag-context-menu");
    var target = menu.data("target-tag");
    var tagName = target.find(".each-tag-name").text();
    if (tagName == "") {
        tagName = "???";
    }
    
    dlg.jq.find("._tag-name").text(" "+tagName);
}
