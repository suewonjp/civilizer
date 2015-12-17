// [NOTE] this is a default basic setting for all jQuery UI Draggable objects;
// It should remain as IMMUTABLE
var baseDraggableSettings = {
    cursor:"move",
    scroll: false,
    helper: "clone",
    zIndex: 10000,
    containment: "document",
    appendTo: "body",
};

function newBaseDroppable(acceptableClasses) {
    var weakFocus = "ui-weak-focus", strongFocus = "ui-strong-focus",
    output = {
        over: function(e, ui) {
            var from = ui.draggable;
            var to = $(e.target);
            if (hasAnyClass(from, acceptableClasses)) {
                to.addClass(strongFocus);
                to.removeClass(weakFocus);
            }
        },
        out: function(e, ui) {
            var from = ui.draggable;
            var to = $(e.target);
            if (hasAnyClass(from, acceptableClasses)) {
                to.removeClass(strongFocus);
                to.addClass(weakFocus);
            }
        },
        activate: function(e, ui) {
            var from = ui.draggable;
            var to = $(e.target);
            if (hasAnyClass(from, acceptableClasses)) {
                to.addClass(weakFocus);
            }
        },
        deactivate: function(e, ui) {
            var to = $(e.target);
            to.removeClass(strongFocus);
            to.removeClass(weakFocus);
        },
        greedy: true,
    };
    return output;
}

function relatingFragmentsDropHandler(e, ui) {
    var from = ui.draggable;
    var to = $(e.target);
    if (hasAnyClass(from, ["fragment-title", "small-fragment-box"])) {
        var fromId = from.attr("_fid");
        var toId = to.find(".fragment-title").attr("_fid") || to.attr("_fid");
        if (fromId != toId) {
            if (!fragmentEditorVisible())
                // [NOTE] block this functionality when the fragment editor is running.
                confirmRelatingFragments(fromId, toId);
        }
    }
}

function fragmentFetchDropHandler(e, ui) {
    var from = ui.draggable;
    var to = $(e.target);
    if (from.hasClass("each-tag")) {
        fetchFragmentsByTag(from, to);
    }
    else if (hasAnyClass(from, ["fragment-title", "small-fragment-box"])) {
        fetchFragments(findPanel(to), [from.attr("_fid")]);
    }
}

function bookmarkingDropHandler(e, ui) {
    var from = ui.draggable;
    var to = $(e.target);
    if (hasAnyClass(from, ["fragment-title", "small-fragment-box"])) {
        var frgId = from.attr("_fid");
        bookmarkFragment([ {name:"fragmentId", value:frgId} ]);
    }
}

function trashingDropHandler(e, ui) {
    var from = ui.draggable;
    var to = $(e.target);
    if (hasAnyClass(from, ["fragment-title", "small-fragment-box"])) {
        var panelId = findPanel(from);
        var deleting = FRAGMENT_DELETABLE[panelId];
        var frgId = from.attr("_fid");
        confirmTrashingFragments(frgId, deleting);
    }
    else if (from.hasClass("each-tag")) {
        confirmTrashingTag(from.attr("_tid"), Boolean(from.attr("_frgCnt") == 0));
    }
}

function frgEditorDropHandler(e, ui) {
    var from = ui.draggable;
    var to = $(e.target);
    if (hasAnyClass(from, ["fragment-title", "small-fragment-box"])) {
        var id = from.attr("_fid");
        var title = from.attr("_ft") || from.text();
        title = title ? title.trim() : "";
        var encoded = "{{[frgm] "+id+" "+title+" }}  \n";
        $(this).insertAtCaret(encoded);
    }
    else if (from.hasClass("each-tag")) {
        var id = from.attr("_tid");
        var encoded = "{{[tag] "+id+" }}  \n";
        $(this).insertAtCaret(encoded);
    }
    else if (from.hasClass("fb-file")) {
        var ids = from.attr("id");
        var id = ids.substr(ids.lastIndexOf("-") + 1);
        var encoded = "{{[file] "+id+" }}  \n";
        $(this).insertAtCaret(encoded);
    }
}

function setupDraggableForFragmentTitle() {
    $(".fragment-title, .small-fragment-box").draggable(baseDraggableSettings);
}

function setupDraggableForTags() {
    var tagPalettePanel = $("#tag-palette-panel");
    var overflowOption = tagPalettePanel.css("overflow");
    $("#tag-palette-panel .each-tag, #fragment-group .each-tag").draggable(baseDraggableSettings);
    $("#tag-palette-panel").off(".cvz_tag_dnd")
    .on("dragstart.cvz_tag_dnd", ".each-tag", function(e, ui) {
        // [NOTE] the helper object disappears at the outside of the panel unless doing this
        tagPalettePanel.css({overflow:"initial"});
    })
    .on("dragstop.cvz_tag_dnd", ".each-tag", function(e, ui) {
        tagPalettePanel.css({overflow:overflowOption});
    });
}

function setupDraggableForFiles() {
    var fileBoxPanel = $("#file-box-form\\:file-box-panel");
    var overflowOption = fileBoxPanel.css("overflow");
    $(".fb-file").draggable(baseDraggableSettings);
    $("#file-path-tree").off(".cvz_file_dnd")
    .on("dragstart.cvz_file_dnd", function(e, ui) {
        fileBoxPanel.css({overflow:"initial"});
    })
    .on("dragstop.cvz_file_dnd", function(e, ui) {
        // [NOTE] the overflow style should be identical between the tag palette and file box
        fileBoxPanel.css({overflow:overflowOption});
    });
}

function setupDndForRelatingFragments() {
    var droppable = newBaseDroppable(["fragment-title", "small-fragment-box"]);
    droppable.drop = relatingFragmentsDropHandler;
    $(".fragment-header, .small-fragment-box").droppable(droppable);
}

function setupDndForFragmentFetch() {
    var droppable = newBaseDroppable(["fragment-title", "small-fragment-box", "each-tag"]);
    droppable.drop = fragmentFetchDropHandler;
    $('[id^="fragment-group-form\\:fragment-panel-toolbar-"]').droppable(droppable);
    $("#panel-activation-buttons label").droppable(droppable);
}

function setupDndForBookmarking() {
    var droppable = newBaseDroppable(["fragment-title", "small-fragment-box"]);
    droppable.drop = bookmarkingDropHandler;
    $("#bookmark-form\\:bookmark-panel").droppable(droppable);
}

function setupDndForTrashing() {
    var droppable = newBaseDroppable(["fragment-title", "small-fragment-box", "each-tag"]);
    droppable.drop = trashingDropHandler;
    $("#trashcan").droppable(droppable);
}

function setupDndToDropDataToFrgEditor() {
    var droppable = newBaseDroppable(["fragment-title", "small-fragment-box", "each-tag", "fb-file"]);
    droppable.drop = frgEditorDropHandler;
    $("#fragment-content-editor").droppable(droppable);
}

function setupDragAndDrop() {
    setupDraggableForFragmentTitle();
    setupDraggableForTags();
    setupDraggableForFiles();
    
    setupDndForRelatingFragments();
    setupDndForFragmentFetch();
    setupDndForBookmarking();
    setupDndForTrashing();
    setupDndToDropDataToFrgEditor();
}

