function setContextMenuForFragments() {
    var menu = $("#frg-context-menu");
    
    $("#fragment-group, #fragment-overlay-content")
    .off("contextmenu.cvz_frg").on("contextmenu.cvz_frg", ".fragment-header", function(e) {
   
        var target = $(e.target);
        if (target.hasClass("rclick-hint")) {
            target = target.closest(".fragment-header");            
        }
        if (target.hasClass("fragment-header")) {
            showPopup(menu, e);
            menu.data("target-frg", target);
            if (target.attr("_deletable") === "true") {
                menu.find("#fragment-group-form\\:bookmark").hide();
                menu.find("#fragment-group-form\\:trash").hide();
                menu.find("#fragment-group-form\\:delete").show();
                menu.find("#fragment-group-form\\:restore").show();
                menu.find("#fragment-group-form\\:relateNew").hide();
                menu.find("#fragment-group-form\\:touch").hide();
            }
            else {
                menu.find("#fragment-group-form\\:bookmark").show();
                menu.find("#fragment-group-form\\:trash").show();
                menu.find("#fragment-group-form\\:delete").hide();
                menu.find("#fragment-group-form\\:restore").hide();
                menu.find("#fragment-group-form\\:relateNew").show();
                menu.find("#fragment-group-form\\:touch").show();
            }
            e.preventDefault();
        }
    });
}

function setContextMenuForBookmarks() {
    var menu = $("#bookmark-context-menu");
    
    $("#bookmark-form\\:bookmark-panel")
    .off("contextmenu.cvz_bm").on("contextmenu.cvz_bm", ".each-bookmark", function(e) {
 
        showPopup(menu, e);
        var target = $(e.target).closest(".each-bookmark");
        menu.data("target-bookmark", target);
        e.preventDefault();
    });
}

function setContextMenuForTags() {
    var menu = $("#tag-context-menu");
    
    $("#fragment-group, #tag-palette-panel, #fragment-overlay-content")
    .off("contextmenu.cvz_tag").on("contextmenu.cvz_tag", ".each-tag", function(e) {
 
        showPopup(menu, e);
        var target = $(e.target).closest(".each-tag");
        menu.data("target-tag", target);
        var tid = target.attr("_tid");
        if (tid <=0) {
            // special tags; not modifiable
            $("#tag-palette-form\\:edit").hide();
            $("#tag-palette-form\\:trash").hide();
        }
        else {
            $("#tag-palette-form\\:edit").show();
            $("#tag-palette-form\\:trash").show();
        }
        $("#tag-palette-form\\:id-placeholder-for-tag").val(tid);
        e.preventDefault();
    });
}
function setContextMenuForSelections() {
    var menu = $("#selection-box-context-menu");

    $("#selection-box")
    .off("contextmenu.cvz_sb").on("contextmenu.cvz_sb", function(e) {
        
        var fragments = getSelectedFragments();
        menu.find("#selection-box-form\\:bookmark").show();
        showOrHide(menu.find("#selection-box-form\\:relate"), fragments.length > 1);
        menu.find("#selection-box-form\\:trash").show();
        menu.find("#selection-box-form\\:unselect").show();
        menu.find("#selection-box-form\\:select_unselect").hide();
        if (fragments.length > 0)
            showPopup(menu, e);
        e.preventDefault();
    });
    
    $("#selection-box-form\\:selection-box-panel")
    .off("contextmenu.cvz_sb").on("contextmenu.cvz_sb", ".each-selected-frg", function(e) {
        
        var target = $(e.target).closest(".each-selected-frg");
        if (isNaN(parseInt(target.attr("_fid"))))
            return;
        
        menu.find("#selection-box-form\\:bookmark").hide();
        menu.find("#selection-box-form\\:relate").hide();
        menu.find("#selection-box-form\\:trash").hide();
        menu.find("#selection-box-form\\:unselect").hide();
        menu.find("#selection-box-form\\:select_unselect").show();
        
        showPopup(menu, e);
        menu.data("target-fragment", target);
        e.preventDefault();
    });
}

function setContextMenuForFiles() {
    var menu = $("#file-context-menu");
    
    $("#file-box-form\\:file-box-panel")
    .off("contextmenu.cvz_file").on("contextmenu.cvz_file", ".each-file, #file-path-tree", function(e) {
        showPopup(menu, e);
        var target = $(e.target);
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
            menu.find("#file-box-form\\:upload").show();
        }
        else {
            // the menu has a target file or folder
            menu.find("#file-box-form\\:upload").hide();
            menu.find("#file-box-form\\:rename").show();
            menu.find("#file-box-form\\:move").show();
            menu.find("#file-box-form\\:delete").show();
        }
        menu.data("target-file", target);
        e.preventDefault();
    });
}

function completeContextMenuSetup() {
    $(document).off("click.cvz_ctxtm_off").on("click.cvz_ctxtm_off", function(e) {
        $("#frg-context-menu, #bookmark-context-menu, #tag-context-menu, #selection-box-context-menu, #file-context-menu")
        .hide();
    });
}

function setupContextMenus() {
    // Add "rclick-disabled" class to whatever element
    // that needs to keep from invoking browsers's default context menu.
    $(".rclick-disabled").off("contextmenu.cvz_rcd").on("contextmenu.cvz_rcd", function(e) {
        e.preventDefault(); 
    });
    
    setContextMenuForFragments();
    setContextMenuForBookmarks();
    setContextMenuForTags();
    setContextMenuForSelections();
    setContextMenuForFiles();

    completeContextMenuSetup();
}
