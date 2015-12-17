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
