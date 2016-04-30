function setupTabViewsForTagPalette() {
	var currentTagGroupTabIndex = "0";
	
	$("#tag-quick-search input").watermark(MSG.type_tag);

    $tab = $( "#tag-palette-panel" ).tabs({
         activate : function (e, ui) {
            currentTagGroupTabIndex = ui.newTab.index().toString();
            sessionStorage.setItem('tag-palette-tab-index', currentTagGroupTabIndex);
            showOrHide($("#tag-quick-search"), currentTagGroupTabIndex == "0");
         }
    });

    if (sessionStorage.getItem('tag-palette-tab-index')) {
        currentTagGroupTabIndex = sessionStorage.getItem('tag-palette-tab-index');
        $tab.tabs('option', 'active', currentTagGroupTabIndex);
    }
}

function onExpandComplete() {
	setContextMenuForTags();
	setupDndForTags(false, true);
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
    if (target.attr("_frgCnt") === undefined)
        target = getTagByName(tagName);
    
    dlg.jq.find("._tag-name")
    .text(" "+tagName+" [ "+target.attr("_frgCnt")+" ("+target.attr("_frgCntWtHrc")+") ]")
    .attr("_tid", target.attr("_tid"))
    ;
    
    $("#tag-palette-form\\:info-parent-tags span, #tag-palette-form\\:info-child-tags span")
    .each(function() {
        var $this = $(this);
        var target = getTag($this.attr("_tid"));
        $this.text(" "+$this.attr("_name")+" [ "+target.attr("_frgCnt")+" ("+target.attr("_frgCntWtHrc")+") ]");
    });
}

function refreshTagInfo(tid) {
    var dlg = PF("tagInfoDlg");
    dlg.hide();
    $("#tag-palette-form\\:id-placeholder-for-tag").val(tid);
    $("#tag-context-menu").data("target-tag", getTag(tid));
    document.forms["tag-palette-form"]["tag-palette-form:tag-info-refresh"].click();
}

function fetchFragmentsByTagQuickSearch(e, input) {
    if (e.which == $.ui.keyCode.ENTER) {
        var tag = getTagByName(input.val());
        if (tag)
            fetchFragmentsByTag(tag, null);
    }
    return false;
}
