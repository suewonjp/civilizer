function processFileClasses(parent) {
	parent.find(".-cvz-file").each(function () {
		var $this = $(this);
		var fileId = $this.text().trim();
		var filePath = $("#-cvz-file-" + fileId).attr("_fp");
		if (filePath) {
			var fileName = filePath.slice(filePath.lastIndexOf(SYSPROP.fileSep) + 1);
			var fileExt = fileName.slice(fileName.lastIndexOf("."));
			filePath = "file-box" + filePath;
			if (isImage(fileExt)) {
				$this.text("").append("<img title='"+fileName+"' src='"+filePath+"'>");
			}
			else {
				$this.text("").append("<a target='_blank' title='"+fileName+"' href='"+filePath+"'>" + filePath + "</a>");
			}
		}
		else { // the file does not exist on the file system for whatever reasons...
			$this.text("").append("<span>").addClass("-cvz-error fa fa-warning").text("  " + MSG.file_not_found + " : " + fileId);
		}
	});
}

var FUDC = { // File Upload Dialog Controller
    fileCount : 0,
    show : function() {
        var dlg = PF("moveFileDlg");
        dlg.show();
        dlg.jq.find(".ui-dialog-title").text(MSG.upload_file);

        $("#file-box-form\\:dst-path").text(MSG.select_destination).removeClass("fa-close").css({color:"aqua"});
        $("#file-upload-box").show();
        $("#file-box-form\\:src-path").text("");
        $("#file-box-form\\:ok").hide();
        $("#file-box-form\\:folder-tree").off("click").on("click", ".each-file", function (e) {
            var $this = $(this),
                fp = $this.attr("_fp") || "";

            $("#file-box-form\\:id-placeholder-for-dst-node").val($this.attr("_id"));

            $("#file-box-form\\:dst-path").text(fp + SYSPROP.fileSep);
            $("#file-box-form\\:folder-tree .each-file").removeClass("fa-upload fb-target-dir");
            $this.addClass("fa-upload fb-target-dir");
            e.preventDefault();
        });
        this.fileCount = 0;
    },
    onstart :  function(pfObj) {
        this.fileCount = pfObj.files.length;
    },
    oncomplete : function(pfObj) {
        if (--this.fileCount === 0) {
            setTimeout(function() {
                document.forms["file-box-form"]["file-box-form:ok-upload"].click();
            }, 500);
        }
    }
};

function showMoveFileDialog() {
	var dlg = PF("moveFileDlg");
    dlg.show();
    dlg.jq.find(".ui-dialog-title").text(MSG.move);
    
    $("#file-upload-box").hide();
    
    var target = $("#file-context-menu").data("target-file");
	var srcId = target.attr("_id");
	$("#file-box-form\\:id-placeholder-for-src-node").val(srcId);
	var srcPath = target.attr("_fp");
	$("#file-box-form\\:src-path").text(srcPath);
	var srcName = target.text().trim();
	var srcIsFolder = target.attr("_isFolder");
	var dstOutput = $("#file-box-form\\:dst-path").text(MSG.select_destination).removeClass("fa-close").css({color:"aqua"});
	var treeNodes = $("#file-box-form\\:folder-tree .each-file").removeClass("fa-upload fb-target-dir");
	var okBtn = $("#file-box-form\\:ok").hide().off("click")
	.on("click", function() {
		document.forms["file-box-form"]["file-box-form:ok-move"].click();
	});

	$("#file-box-form\\:folder-tree .each-file").off("click")
	.on("click", function () {
		var $this = $(this);
		treeNodes.removeClass("fa-upload fb-target-dir");
		var fp = $this.attr("_fp");
		if (fp === undefined)
			fp = "";
		var dstPath = fp + SYSPROP.fileSep + srcName;
		var canMove = (srcPath !== dstPath) &&
		    (!srcIsFolder || !parentChildFolders(srcPath, dstPath));
		if (canMove) {
			var dstId = $this.attr("_id");
			$("#file-box-form\\:id-placeholder-for-dst-node").val(dstId);
			$this.addClass("fa-upload fb-target-dir");
			dstOutput.text(dstPath).removeClass("fa-close").css({color:"aqua"});
			okBtn.show().effect("shake");
		}
		else {
			dstOutput.text(" " + MSG.cant_move).addClass("fa fa-close").css({color:"red"});
			okBtn.hide();
		}
	});
}

function showRenameFileDialog() {
    var target = $("#file-context-menu").data("target-file");
	var srcId = target.attr("_id");
	$("#file-box-form\\:id-placeholder-for-src-node").val(srcId);
	var curName = target.text().trim();
	var submitBtn = PF("renameFileDlgSubmit");

	$("#file-box-form\\:rename-file-dlg-name-input").val(curName).off("focus keydown keyup")
	.on("focus", function() {
	    $(this).select();
	    submitBtn.disable();
	})
	.on("keydown", function(e) {
	    var typed = $(this).val().trim();
	    if (e.which == $.ui.keyCode.ENTER && (!typed || typed == curName))
	        return false;
	})
	.on("keyup", function(e) {
	    var typed = $(this).val().trim();
        if (!typed || typed == curName)
            submitBtn.disable();
        else
            submitBtn.enable();
	})
	;
    
    var dlg = PF("renameFileDlg");
    dlg.show();
    dlg.jq.find(".ui-dialog-title").text(MSG.rename);
}

function showNewDirectoryDialog() {
    var target = $("#file-context-menu").data("target-file");
    var submitBtn = PF("renameFileDlgSubmit");

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
	
    $("#file-box-form\\:rename-file-dlg-name-input").val("new-directory").off("focus keydown keyup")
    .on("focus", function() {
        $(this).select();
        submitBtn.enable();
    })
    .on("keydown", function() {
        var typed = $(this).val().trim();
        if (e.which == $.ui.keyCode.ENTER && !typed)
            return false;
    })
    .on("keyup", function() {
        var typed = $(this).val().trim();
        if (!typed)
            submitBtn.disable();
        else
            submitBtn.enable();
    })
    ;
    
    var dlg = PF("renameFileDlg");
    dlg.show();
    dlg.jq.find(".ui-dialog-title").text(MSG.new_folder);
}

function showFileInfo() {
    var overlayFrame = $("#fragment-overlay");
    overlayFrame.lightbox_me({
        centered:false
        , showOverlay:false
        , lightboxSpeed:"fast"
        , closeSelector:"#fragment-overlay-close-button"
        , closeEsc:true
        , modalCSS:{ position:"fixed", bottom: '34px', right: '2%' }
    });
    
    var titleBar = $("#fragment-overlay-title-bar");
    if (titleBar.next().is(":visible") == false) {
        toggleWindow(overlayFrame, titleBar);
    }
    
    $("#fragmen-overlay-back-button").hide();
    
    var target = $("#file-context-menu").data("target-file") || $(this);
    $("#file-context-menu").data("target-file", null)
    var fp = target.attr("_fp");
    var path = "file-box/"+fp;
    var title, content = "<p class='file-info'>"+fp+"</p>";
    if (target.attr("_isFolder") == "true") {
        title = "<i class='fa fa-folder-open'></i>";
    }
    else {
        var fid = suffix(target.attr("id"), "-", true);
        title = "<i class='fa fa-file'> #"+fid+"</i>";
        content = "<a target='_blank' href='"+path+"'>"+content+"</a>";
        $("#fragment-group-form\\:search-panel\\:quick-search-input").val("\\{\\{\\[file\\](\\s*)"+fid+"(\\s*)\\}\\}/r");
        content += "<a href='#' onclick='searchFragmentsForPanel(findAvailablePanel())'><p class='file-info'>"+MSG.get_frg4file+"</p></a>";
        if (isImage(suffix(fp, ".")))
            content += "<img class='file-info-img' src='"+path+"'/>";
    }
    
    $("#fragment-overlay-title").html(title);
    $("#fragment-overlay-content").html(content);
}

function setupFileInfo() {
    $("#file-box-form\\:file-box-panel").off(".file_info")
    .on("click.file_info", ".each-file", showFileInfo);
}
