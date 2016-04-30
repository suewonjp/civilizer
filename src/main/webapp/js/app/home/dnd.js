function setupDndForFragments(forFramentOverlay) {
    var srcSelector = ".fragment-title, .small-fragment-box";
    if (forFramentOverlay === true) {
        dndx(srcSelector, ".fragment-header, .small-fragment-box").refresh();
    }
    else {
        dndx(srcSelector)
        .onconflict(function($srcObj, $tgtObj0, $tgtObj1) {
            if ($tgtObj0.is("#fragment-content-editor")) return $tgtObj0;
            if ($tgtObj1.is("#fragment-content-editor")) return $tgtObj1;
            if ($tgtObj0.is("#fragment-overlay-content .fragment-header")) return $tgtObj0;
            if ($tgtObj1.is("#fragment-overlay-content .fragment-header")) return $tgtObj1;
            if ($tgtObj0.is(".fragment-header")) return $tgtObj0;
            if ($tgtObj1.is(".fragment-header")) return $tgtObj1;
            return $tgtObj0;
        })
        .ondrop(function(eventType, $srcObj, $tgtObj, srcSelector, tgtSelector, e) {
            var frgId = $srcObj.attr("_fid");
            if ($tgtObj.is(".fragment-header, .small-fragment-box")) {
                var tgtFrgId = $tgtObj.find(".fragment-title").attr("_fid") || $tgtObj.attr("_fid");
                if (frgId != tgtFrgId) {
                    if (!fragmentEditorVisible())
                        // [NOTE] block this functionality when the fragment editor is running.
                        confirmRelatingFragments(frgId, tgtFrgId);
                }
            }
            else if ($tgtObj.is("[id^='fragment-group-form\\:fragment-panel-toolbar-']")) {
                fetchFragments(findPanel($tgtObj), [frgId]);
            }
            else if ($tgtObj.is("#panel-activation-buttons label")) {
                fetchFragments(findPanel($tgtObj), [frgId]);
            }
            else if ($tgtObj.is("#bookmark-form\\:bookmark-panel")) {
                bookmarkFragment([ {name:"fragmentId", value:frgId} ]);
            }
            else if ($tgtObj.is("#trashcan .fa-trash")) {
                var panelId = findPanel($srcObj), deleting = FRAGMENT_DELETABLE[panelId];
                confirmTrashingFragments(frgId, deleting);
            }
            else if ($tgtObj.is("#fragment-content-editor")) {
                var title = $srcObj.attr("_ft") || $srcObj.text();
                title = (typeof title === "string") ? title.trim() : "";
                var encoded = "{{[frgm] "+id+" "+title+" }}  \n";
                $tgtObj.insertAtCaret(encoded);
            }
        })
        .targets(".fragment-header, .small-fragment-box")
        .targets("[id^='fragment-group-form\\:fragment-panel-toolbar-'], #panel-activation-buttons label, #bookmark-form\\:bookmark-panel, #trashcan .fa-trash, #fragment-content-editor")
        ;
        
        // Trashed fragments are not draggable
        $(".fragment-header .each-tag-name:contains(#trash)").each(function() {
            $(this).closest(".fragment-header")
                .find(".fragment-title.ui-draggable").draggable("destroy");
        });
    }
}

function setupDndForTags(forFramentOverlay, onTagTreeExpand) {
    function onDrop(eventType, $srcObj, $tgtObj, srcSelector, tgtSelector, e) {
        if ($tgtObj.is("[id^='fragment-group-form\\:fragment-panel-toolbar-']")) {
            fetchFragmentsByTag($srcObj, $tgtObj);
        }
        else if ($tgtObj.is("#panel-activation-buttons label")) {
            fetchFragmentsByTag($srcObj, $tgtObj);
        }
        else if ($tgtObj.is("#trashcan .fa-trash")) {
            confirmTrashingTag($srcObj.attr("_tid"), Boolean($srcObj.attr("_frgCnt") == 0));
        }
        else if ($tgtObj.is("#fragment-content-editor")) {
            var id = $srcObj.attr("_tid"), encoded = "{{[tag] "+id+" }}  \n";
            $tgtObj.insertAtCaret(encoded);
        }
    }
    
    var tgtSelector = "[id^='fragment-group-form\\:fragment-panel-toolbar-'], #panel-activation-buttons label, #trashcan .fa-trash, #fragment-content-editor";
    if (forFramentOverlay === true) {
        dndx("#fragment-overlay-content .each-tag", tgtSelector).ondrop(onDrop).refresh();
    }
    else if (onTagTreeExpand === true) {
        dndx("#tag-palette-panel .each-tag, #fragment-group .each-tag", tgtSelector).refresh();
    }
    else {
        dndx("#tag-palette-panel .each-tag, #fragment-group .each-tag", tgtSelector)
        .onconflict(function($srcObj, $tgtObj0, $tgtObj1) {
            if ($tgtObj0.is("#fragment-content-editor")) return $tgtObj0;
            if ($tgtObj1.is("#fragment-content-editor")) return $tgtObj1;
            return $tgtObj0;
        })
        .ondrop(onDrop);
        
        // Trash tags are not draggable
        $("#fragment-group .each-tag-name:contains(#trash)").each(function() {
           $(this).closest(".each-tag").draggable("destroy"); 
        });
    }
}

function setupDndForFiles() {
    dndx(".fb-file", "#fragment-content-editor")
    .ondrop(function(eventType, $srcObj, $tgtObj, srcSelector, tgtSelector, e) {
        if ($tgtObj.is("#fragment-content-editor")) {
            var ids = $srcObj.attr("id"), id = ids.substr(ids.lastIndexOf("-") + 1),
                encoded = "{{[file] "+id+" }}  \n";
            $tgtObj.insertAtCaret(encoded);
        }
    });
}

function setupDragAndDrop() {
    // Global settings for all drag & drop operations
    dndx()
    .visualcue(function(eventType, $srcObj, $tgtObj, srcSelector, tgtSelector, e) {
        switch (eventType) {
        case "dropactivate":
            $tgtObj.addClass("dnd-visualcue-activate"); 
            break;
        case "dropdeactivate":
            $tgtObj.removeClass("dnd-visualcue-over dnd-visualcue-activate");
            break;
        case "dropover": 
            $tgtObj.addClass("dnd-visualcue-over");
            break;
        case "dropout":
            $tgtObj.removeClass("dnd-visualcue-over");
            break;
        }
    })
    .draggableOptions({
        cursor:"move",
        cursorAt:{ left:30 },
        scroll: false,
        helper: function() {
            var clone = $(this).clone().removeAttr("id");
            clone.css({ "min-width":60, "max-width":60, "overflow":"hidden", "white-space":"nowrap" });
            return clone;
        },
        zIndex: 10000,
        containment: "document",
        appendTo: "body",
    })
    ;
    
    setupDndForFragments();
    setupDndForTags();
    setupDndForFiles();
}

