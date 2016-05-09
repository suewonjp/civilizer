function bookmarkable($frgHdr) {
    if (fragmentTrashed($frgHdr))
        return false;
    var fid = $frgHdr.attr("_fid"),
        bookmakrs = $("#bookmark-form\\:bookmark-panel .each-bookmark"),
        c = bookmakrs.length, i;
    if (fid) {
        for (i=0; i<c; ++i) {
            if ($(bookmakrs[i]).attr("_fid") === fid)
                return false; // Already bookmarked
        }
    }
    return true;
}

function confirmBookmarkingFragment(fid, title) {
    var subMsg = "\n#"+fid + "  " + title;
    $("#fragment-group-form\\:ok").click(function() {
        addSubmitParam($("#fragment-group-form"), {fragmentId:fid});
        document.forms["fragment-group-form"]["fragment-group-form:ok-bookmark-fragment"].click();
    });
    showConfirmDlg(MSG.confirm_bookmarking, subMsg, "fa-bookmark", "orange");
}

function confirmUnbookmarkingFragment() {
	var target = $("#bookmark-context-menu").data("target-bookmark");
	var fid = target.attr("_fid");
	var subMsg = "\n#"+fid + "  " + target.attr("_ft");
	$("#fragment-group-form\\:ok").click(function() {
	    addSubmitParam($("#fragment-group-form"), {fragmentId:fid});
	    document.forms["fragment-group-form"]["fragment-group-form:ok-unbookmark-fragment"].click();
	});
	showConfirmDlg(MSG.confirm_unbookmarking, subMsg, "fa-close", "orange");
}
