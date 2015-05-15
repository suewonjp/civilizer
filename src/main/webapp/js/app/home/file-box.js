function setContextMenuForFiles() {
    var menu = $("#file-context-menu");
    
	$(".each-file, #file-path-tree").bind("contextmenu", function(event) {
    	menu.css({ left:event.pageX, top:event.pageY }).show();
    	var target = $(event.target);
    	if (target.attr("_isFolder") === "false") {
    		menu.find("#file-box-form\\:new-folder").hide();
    	}
    	else {
    		menu.find("#file-box-form\\:new-folder").show();
    	}
    	if (isNaN(parseInt(target.attr("_id")))) {
    		// the menu has no specified target;
    		// the target will be the root folder
    		menu.find("#file-box-form\\:rename").hide();
    		menu.find("#file-box-form\\:move").hide();
    		menu.find("#file-box-form\\:delete").hide();
    	}
    	else {
    		// the menu has a target file or folder
    		menu.find("#file-box-form\\:rename").show();
    		menu.find("#file-box-form\\:move").show();
    		menu.find("#file-box-form\\:delete").show();
    	}
    	menu.data("target-file", target);
    	event.preventDefault();
    });

    $(document).bind("click", function(event) {
    	menu.hide();
    });
}

function isImage(fileExt) {
	return (fileExt == ".png")
		|| (fileExt == ".jpg")
		|| (fileExt == ".gif")
		|| (fileExt == ".bmp")
		|| (fileExt == ".ico")
		;
}

function processFileClasses(parent) {
	parent.find(".-cvz-file").each(function () {
		var $this = $(this);
		var fileId = $this.text();
		var filePath = $("#-cvz-file-" + fileId).attr("_fp");
		if (filePath) {
			var fileName = filePath.slice(filePath.lastIndexOf(SYSPROP.fileSep) + 1);
			var fileExt = fileName.slice(fileName.lastIndexOf("."));
			filePath = "file-box" + filePath;
			if (isImage(fileExt)) {
				$this.text("").append("<img title='"+fileName+"' src='"+filePath+"'>");
			}
			else {
				$this.text("").append("<a title='"+fileName+"' href='"+filePath+"'>" + filePath + "</a>");
			}
		}
		else { // the file does not exist on the file system for whatever reasons...
			$this.text("").append("<span>").addClass("-cvz-error fa fa-warning").text("  " + MSG.file_not_found + " : " + fileId);
		}
	});
}

function showFileUploadDialog() {
	var dlg = PF("moveFileDlg");
    dlg.show();
    dlg.jq.find(".ui-dialog-title").text(MSG.label_upload_file);
    
    $("#file-upload-box").show();
    $("#file-box-form\\:src-path").text("");
    
	var menu = $("#file-context-menu");
	var target = menu.data("target-file");
	var dstOutput = $("#file-box-form\\:dst-path");
	dstOutput.text(MSG.select_destination).removeClass("fa-close").css({color:"aqua"});
	var treeNodes = $("#file-box-form\\:folder-tree .each-file");
	treeNodes.removeClass("fa-upload fb-target-dir");
	var fp = "";
	var dstPath = "";
	var fileSelector = $("#file-box-form\\:file-selector");
	fileSelector.val(null);
	var holderForDstId = $("#file-box-form\\:id-placeholder-for-dst-node");
	holderForDstId.val(0);
	var okBtn = $("#file-box-form\\:ok").hide().click(function() {
		document.forms["file-box-form"]["file-box-form:ok-upload"].click();
	});
	
	fileSelector.change(function () {
	    var fileToUpload = $(this).val();
	    if (fileToUpload) {
	    	dstPath = fp + SYSPROP.fileSep + fileToUpload;
	    	dstOutput.text(dstPath);
	    	okBtn.show().effect("shake");
	    }
	});
	
	$("#file-box-form\\:folder-tree .each-file").click(function () {
		var $this = $(this);
		treeNodes.removeClass("fa-upload fb-target-dir");
		fp = $this.attr("_fp");
		if (fp === undefined)
			fp = "";
		var fileToUpload = fileSelector.val();
		if (fileToUpload === undefined)
			fileToUpload = "";
		dstPath = fp + SYSPROP.fileSep + fileToUpload;
		dstOutput.text(dstPath);
		var dstId = $this.attr("_id");
		holderForDstId.val(dstId);
		$this.addClass("fa-upload fb-target-dir");
	});
}

function showMoveFileDialog() {
	var dlg = PF("moveFileDlg");
    dlg.show();
    dlg.jq.find(".ui-dialog-title").text(MSG.label_move);
    
    $("#file-upload-box").hide();
    
    var menu = $("#file-context-menu");
	var target = menu.data("target-file");
	var srcId = target.attr("_id");
	$("#file-box-form\\:id-placeholder-for-src-node").val(srcId);
	var srcPath = target.attr("_fp");
	$("#file-box-form\\:src-path").text(srcPath);
	var srcName = target.text().trim();
	var srcIsFolder = target.attr("_isFolder");
	var dstOutput = $("#file-box-form\\:dst-path");
	dstOutput.text(MSG.select_destination).removeClass("fa-close").css({color:"aqua"});
	var treeNodes = $("#file-box-form\\:folder-tree .each-file");
	treeNodes.removeClass("fa-upload fb-target-dir");
	var okBtn = $("#file-box-form\\:ok").hide().click(function() {
		document.forms["file-box-form"]["file-box-form:ok-move"].click();
	});

	$("#file-box-form\\:folder-tree .each-file").click(function () {
		var $this = $(this);
		treeNodes.removeClass("fa-upload fb-target-dir");
		var fp = $this.attr("_fp");
		if (fp === undefined)
			fp = "";
		var dstPath = fp + SYSPROP.fileSep + srcName;
		var canMove = (srcPath !== dstPath) && (!srcIsFolder || dstPath.indexOf(srcPath) != 0);
		if (canMove) {
			var dstId = $this.attr("_id");
			$("#file-box-form\\:id-placeholder-for-dst-node").val(dstId);
			$this.addClass("fa-upload fb-target-dir");
			dstOutput.text(dstPath);
			dstOutput.removeClass("fa-close").css({color:"aqua"});
			okBtn.show().effect("shake");
		}
		else {
			dstOutput.text(" " + MSG.cant_move);
			dstOutput.addClass("fa-close").css({color:"red"});
			okBtn.hide();
		}
	});
}

function showRenameFileDialog() {
    var menu = $("#file-context-menu");
	var target = menu.data("target-file");
	var srcId = target.attr("_id");
    $("#file-box-form\\:id-placeholder-for-src-node").val(srcId);
    
	var curName = target.text().trim();
    $("#file-box-form\\:rename-file-dlg-name-input").val(curName).focus(function() {
    	$(this).select();
    });
    
    var dlg = PF("renameFileDlg");
    dlg.show();
    dlg.jq.find(".ui-dialog-title").text(MSG.label_rename);
}

function showNewDirectoryDialog(target) {
    var menu = $("#file-context-menu");
	var target = menu.data("target-file");
	
    // As a rule, we should send the id after encoding it like so:
    //   -( (original-value) + 1 )
    // see also MainController.renameFile()
	var id = target.attr("_id");
    if (isNaN(id)) {
    	// In this case, we make a new directory at the root level
    	id = -1;
    }
    else {
    	id = -(++id);
    }
	$("#file-box-form\\:id-placeholder-for-src-node").val(id);
	
    $("#file-box-form\\:rename-file-dlg-name-input").val("new-directory");
    
    var dlg = PF("renameFileDlg");
    dlg.show();
    dlg.jq.find("input").focus(function() {
    	$(this).select();
    });
    dlg.jq.find(".ui-dialog-title").text(MSG.label_new_folder);
}
