function onAjaxCompleteForSelectionBox() {
    setContextMenuForSelections();
    
    setupFragmentOverlayTrigger($("#selection-box-form\\:selection-box-panel"));
    
    initTogglerIcon($("#selection-box-title"));
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
