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

function createTagEditorController() {
    var ctrr = new Object();
    var dlg, saveBtn, nameInplace;
    var newTag = false;
    
    function getDialog() {
        return dlg || (dlg = PF('tagEditor'));
    }
    
    function getNameInplace() {
        return nameInplace || (nameInplace = PF("tagNameInplace"));
    }
    
    function getSaveBtn() {
        return saveBtn || (saveBtn = PF("tagEditorSaveBtn"));
    }
    
    function onNameInplaceCommit(val, text) {
        saveBtn.disable();
        var submittable;
        if (newTag)
            submittable = (val !== text);
        else
            submittable = val.trim();
        if (!submittable)
            return;
        var invalidChar = validateTagNames(val);
        if (invalidChar)
            showError("'" + invalidChar + MSG.cant_use_for_tags);
        else
            saveBtn.enable();
    }
    
    ctrr.showDialog = function(_newTag) {
        newTag = _newTag;
        saveBtn = getSaveBtn();
        if (newTag)
            saveBtn.disable();
        
        PF("parentTagNameFilter").hide();
        PF("childTagNameFilter").hide();

        var nameInplace = getNameInplace();
        nameInplace.hide();
        
        var tagName;
        if (newTag)
            tagName = nameInplace.jq.next("span").text();
        else {
            var target = $("#tag-context-menu").data("target-tag");
            tagName = target.find(".each-tag-name").text() || target.text();
        }

        setupPfInplaceText(
                nameInplace
                , tagName
                , $("#tag-palette-form\\:tag-name-panel")
                , onNameInplaceCommit
        );
        
        dlg = getDialog();
        dlg.jq.off(".te")
        .on("focus.te", "#tag-palette-form\\:tag-name-panel input", function(e) {
            saveBtn.disable();
        })
        .find("input[name=isNewTag]").val(newTag);
        dlg.show();
    }
    
    return ctrr;
}

var TEC = createTagEditorController();

function showTagInfo() {
    var dlg = PF("tagInfoDlg");
    dlg.show();
    
    var target = $("#tag-context-menu").data("target-tag");
    var tagName = target.find(".each-tag-name").text() || target.text();
    
    dlg.jq.find("._tag-name")
    .text(" "+tagName+" [ "+target.attr("_frgCnt")+" ("+target.attr("_frgCntWtHrc")+") ]");
    
    $("#tag-palette-form\\:info-parent-tags span, #tag-palette-form\\:info-child-tags span")
    .each(function() {
        var $this = $(this);
        var target = getTag($this.attr("_tid"));
        $this.text(" "+$this.attr("_name")+" [ "+target.attr("_frgCnt")+" ("+target.attr("_frgCntWtHrc")+") ]");
    });
}
