function prepareSelectionBox() {
    setContextMenuForSelections();
    
    initTogglerIcon($("#selection-box-title"));

    setupDndForFragments(true);
}

function getSelectedFragments() {
    var output = [];
    $("#selection-box-form\\:selection-box-panel").find(".each-selected-frg").each(function() {
        var $this = $(this);
        if ($this.hasClass("middle-line"))
            return;
        output.push("#" + $this.attr("_fid") + "  " + $this.attr("_ft"));
    });
    return output;
}

function listUnselectedFragments() {
    var output = "";
    $("#selection-box-form\\:selection-box-panel").find(".each-selected-frg").each(function() {
        var $this = $(this);
        if (! $this.hasClass("middle-line"))
            return;
        output += $this.attr("_fid") + " ";
    });
    return output;
}

function kickOperationForSelectedFragments(tgtId) {
    $("#fragment-group-form\\:ok").click(function() {
        addSubmitParam($("#fragment-group-form"), {unselected:listUnselectedFragments()});
        document.forms["fragment-group-form"]["fragment-group-form:"+tgtId].click();
    });
}

function confirmRelatingSelectedFragments() {
    kickOperationForSelectedFragments("ok-relate-fragments");
    var mainMsg = MSG.confirm_relating;
    var subMsg = getSelectedFragments().join("\n");
    showConfirmDlg(mainMsg, subMsg, "fa-link", "aqua");
}

function confirmTrashingSelectedFragments() {
    kickOperationForSelectedFragments("ok-trash-fragments");
    var mainMsg = MSG.confirm_trashing;
    var subMsg = getSelectedFragments().join("\n");
    showConfirmDlg(mainMsg, subMsg, "fa-trash", "orangered");
}

function appendTagToSelectedFragments() {
    var submitBtn = PF("tagAllDlgSubmit");
    submitBtn.disable();
    
    var tagInput = $("#selection-box-form\\:tag-all-dlg input");
    
    tagInput.off("keyup")
    .on("keyup", function() {
        var typed = $(this).val().trim();
        if (typed)
            submitBtn.enable();
        else
            submitBtn.disable();
    })
    ;

    submitBtn.jq.off("click").on("click", function() {
        addSubmitParam($("#fragment-group-form"), {unselected:listUnselectedFragments()});
        appendTags([{name:"tagNames", value:tagInput.val().trim()}]);
    });
    
    var dlg = PF("tagAllDlg");
    dlg.jq.find("input").watermark(MSG.type_tag);
    dlg.show();
}

function confirmUnselectingSelectedFragments() {
    document.forms["fragment-group-form"]["fragment-group-form:ok-unselect-all-fragments"].click();
}

function selectOrUnselectFragment() {
    var target = $("#selection-box-context-menu").data("target-fragment");
    if (target.hasClass("middle-line")) {
        // mark as selected
        target.removeClass("middle-line");
    }
    else {
        // mark as unselected
        target.addClass("middle-line");
    }
}

function onChangeFragmentCheckbox(chkbox, frgId) {
    var self = $(chkbox).closest(".fragment-header"),
        frg = $("#fragment-group").find(".fragment-header[_fid=" + frgId + "]").not(self),
        pfcb;
    for (var i=0; i<frg.length; ++i) {
        pfcb = PF(frg.eq(i).data("pfCheckbox"));
        if (chkbox.checked) pfcb.check();
        else pfcb.uncheck();
    }
}

function selectFragmentById(frgId) {
    var frg = $("#fragment-group").find(".fragment-header[_fid=" + frgId + "]");
    for (var i=0; i<frg.length; ++i) {
        PF(frg.eq(i).data("pfCheckbox")).check();
    }
    if (! frg.length) {
        selectFragment([{ name:'fid', value:frgId }]);
    }
}
