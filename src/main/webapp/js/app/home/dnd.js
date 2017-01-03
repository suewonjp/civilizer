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
            if ($tgtObj0.is("#fragment-overlay-content .fragment-header, #fragment-overlay-content .small-fragment-box")) return $tgtObj0;
            if ($tgtObj1.is("#fragment-overlay-content .fragment-header, #fragment-overlay-content .small-fragment-box")) return $tgtObj1;
            if ($tgtObj0.is("#editor-frame")) return $tgtObj0;
            if ($tgtObj1.is("#editor-frame")) return $tgtObj1;
            if ($tgtObj0.is("[id^='fragment-group-form\\:fragment-panel-toolbar-'], #panel-activation-buttons label")) return $tgtObj0;
            if ($tgtObj1.is("[id^='fragment-group-form\\:fragment-panel-toolbar-'], #panel-activation-buttons label")) return $tgtObj1;
            if ($tgtObj0.is("#fragment-overlay")) return $tgtObj0;
            if ($tgtObj1.is("#fragment-overlay")) return $tgtObj1;
            if ($tgtObj0.is(".fragment-header, .small-fragment-box")) return $tgtObj0;
            if ($tgtObj1.is(".fragment-header, .small-fragment-box")) return $tgtObj1;
            return $tgtObj0;
        })
        .ondrop(function(eventType, $srcObj, $tgtObj, srcSelector, tgtSelector, e) {
            var frgId = $srcObj.attr("_fid");
            if ($tgtObj.is(".fragment-header, .small-fragment-box")) {
                var tgtFrgId = $tgtObj.find(".fragment-title").attr("_fid") || $tgtObj.attr("_fid");
                if (frgId != tgtFrgId) {
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
                confirmBookmarkingFragment(frgId, $srcObj.text());
            }
            else if ($tgtObj.is("#selection-box-form\\:selection-box-panel")) {
                selectFragmentById(frgId);
            }
            else if ($tgtObj.is("#trashcan .fa-trash")) {
                confirmTrashingFragments(frgId, fragmentTrashed($srcObj));
            }
            else if ($tgtObj.is("#fragment-content-editor")) {
                var title = $srcObj.attr("_ft") || $srcObj.text();
                title = (typeof title === "string") ? title.trim() : "";
                var encoded = "{{[frgm] " + frgId + " "+title+" }}  \n";
                $tgtObj.insertAtCaret(encoded);
            }
        })
        .oncheckpair(function($srcObj, $tgtObj) {
            if ($tgtObj.is("#bookmark-form\\:bookmark-panel")) {
                // Let the bookmark panel invite only bookmarkable fragments
                return bookmarkable($srcObj);
            }
            var tgtFrgId = $tgtObj.attr("_fid");
            if (tgtFrgId) {
                // Don't let elements of the identical fragment attract each other 
                return tgtFrgId !== $srcObj.attr("_fid");
            }
            return true;
        })
        .targets(".fragment-header, .small-fragment-box")
        .targets("[id^='fragment-group-form\\:fragment-panel-toolbar-'], #panel-activation-buttons label, #bookmark-form\\:bookmark-panel, #selection-box-form\\:selection-box-panel, #trashcan .fa-trash, #fragment-content-editor")
        .newPair(null, "#editor-frame, #fragment-overlay").nullify()
        ;
    }
    
//    $(".fragment-header .each-tag-name:contains(#trash)").each(function() {
//        // Trashed fragments are not draggable
//        $(this).closest(".fragment-header")
//        .find(".fragment-title.ui-draggable").draggable("destroy");
//    });
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
            var tid = $srcObj.attr("_tid");
            confirmTrashingTag(tid, Boolean($srcObj.attr("_frgCnt") == 0));
        }
        else if ($tgtObj.is("#fragment-content-editor")) {
            var id = $srcObj.attr("_tid"), encoded = "{{[tag] "+id+" }}  \n";
            $tgtObj.insertAtCaret(encoded);
        }
    }
    
    function onConflict($srcObj, $tgtObj0, $tgtObj1) {
        if ($tgtObj0.is("#fragment-content-editor")) return $tgtObj0;
        if ($tgtObj1.is("#fragment-content-editor")) return $tgtObj1;
        if ($tgtObj0.is("#editor-frame")) 
            return $tgtObj0;
        if ($tgtObj1.is("#editor-frame")) 
            return $tgtObj1;
        if ($tgtObj0.is("#fragment-overlay")) 
            return $tgtObj0;
        if ($tgtObj1.is("#fragment-overlay")) 
            return $tgtObj1;        
        return $tgtObj0;
    }
    
    function onCheckPair($srcObj, $tgtObj) {
        if ($tgtObj.is("#trashcan .fa-trash")) {
            var tid = $srcObj.attr("_tid");
            if (tid <= 0) { // Don't attract special tags
                return false;
            }
        }
        return true;
    }
    
    var tgtSelector = "[id^='fragment-group-form\\:fragment-panel-toolbar-'], #panel-activation-buttons label, #trashcan .fa-trash, #fragment-content-editor";
    if (forFramentOverlay === true) {
        dndx("#fragment-overlay-content .each-tag", tgtSelector).refresh();
    }
    else if (onTagTreeExpand === true) {
        dndx("#tag-palette-panel .each-tag, #fragment-group .each-tag", tgtSelector).refresh();
    }
    else {
        dndx("#tag-palette-panel .each-tag, #fragment-group .each-tag", tgtSelector)
        .onconflict(onConflict)
        .ondrop(onDrop)
        .oncheckpair(onCheckPair)
        .newPair(null, "#editor-frame, #fragment-overlay").nullify()
        ;
        
        dndx("#fragment-overlay-content .each-tag", tgtSelector)
        .onconflict(onConflict)
        .ondrop(onDrop)
        .oncheckpair(onCheckPair)
        .newPair(null, "#editor-frame, #fragment-overlay").nullify()
        ;
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

