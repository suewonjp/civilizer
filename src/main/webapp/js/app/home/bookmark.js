function setContextMenuForBookmarks() {
	var menu = $("#bookmark-context-menu");
   	
	$(".each-bookmark").bind("contextmenu", function(event) {
    	menu.css({ left:event.pageX, top:event.pageY }).show();
    	var target = $(event.target).closest(".each-bookmark");
    	menu.data("target-bookmark", target);
    	$("#fragment-group-form\\:id-placeholder-for-fragment").val(target.attr("_bid"));
    	event.preventDefault();
    });

    $(document).bind("click", function(event) {
    	menu.hide();
    });
}

function confirmUnbookmarkingFragment() {
	$("#fragment-group-form\\:ok").click(function() {
		document.forms["fragment-group-form"]["fragment-group-form:ok-unbookmark-fragment"].click();
	});
	var target = $("#bookmark-context-menu").data("target-bookmark");
	showConfirmDlg(MSG.confirm_unbookmarking, target ? target.text() : "");
}
