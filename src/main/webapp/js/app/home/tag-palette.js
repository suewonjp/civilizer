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
    
    function onNameInplaceCommit(val, text) {
        saveBtn.disable();
        if (val !== text) {
            var invalidChar = validateTagNames(val);
            if (invalidChar)
                showError("'" + invalidChar + MSG.cant_use_for_tags);
            else
                saveBtn.enable();
        }
    }

    var inplace = PF("tagNameInplace");
    inplace.hide();    

    setupPfInplaceText(
            PF("tagNameInplace")
            , inplace.jq.next("span").text()
            , $("#tag-palette-form\\:tag-name-panel")
            , onNameInplaceCommit
    );
    
    var dlg = PF("tagEditor");
    dlg.jq.off(".te")
    .on("focus.te", ".ui-inplace-content input", function(e) {
        saveBtn.disable();
    })
    .find("input[name=isNewTag]").val(true);
    $("#tag-palette-form\\:parent-tags").empty();
    $("#tag-palette-form\\:child-tags").empty();
    dlg.show();
}

function showTagEditorForEditing() {
    var saveBtn = PF("tagEditorSaveBtn");
    
    function onNameInplaceCommit(val, text) {
        saveBtn.disable();
        if (val.trim()) {
            var invalidChar = validateTagNames(val);
            if (invalidChar)
                showError("'" + invalidChar + MSG.cant_use_for_tags);
            else
                saveBtn.enable();
        }
    }
    
    var menu = $("#tag-context-menu");
    var target = menu.data("target-tag");
    var tagName = target.find(".each-tag-name").text() || target.text();
    if (tagName == "") {
        tagName = "???";
    }
    
    var inplace = PF("tagNameInplace");
    inplace.hide();
    
    setupPfInplaceText(
            inplace
            , tagName
            , $("#tag-palette-form\\:tag-name-panel")
            , onNameInplaceCommit
    );
    
	var dlg = PF("tagEditor");
    dlg.jq.off(".te")
    .on("focus.te", ".ui-inplace-content input", function(e) {
        saveBtn.disable();
    })
    .find("input[name=isNewTag]").val(false);
	dlg.show();
}

function showTagInfo() {
    var dlg = PF("tagInfoDlg");
    dlg.show();
    
    var menu = $("#tag-context-menu");
    var target = menu.data("target-tag");
    var tagName = target.find(".each-tag-name").text() || target.text();
    if (tagName == "") {
        tagName = "???";
    }
    
    dlg.jq.find("._tag-name").text(" "+tagName);
}
