/*global jQuery */
/*eslint no-unused-vars:0*/

var dndx = null;

(function($) {

    //***** Data structure to manage a sequence of unique elements;
    function UniqueSequence() {}

    UniqueSequence.prototype = new Array;

    UniqueSequence.prototype.front = function() {
        return this.length ? this[0] : null;
    };

    UniqueSequence.prototype.pushFront = function(item) {
        var iii = this.indexOf(item), output = this.front();
        if (iii === -1) {
            this.unshift(item);
        }
        else {
            this.swap(iii, 0);
        }
        return output;
    };

    UniqueSequence.prototype.remove = function(item) {
        var iii = this.indexOf(item), output = item;
        if (iii > -1) {
            this[iii] = this[this.length - 1];
            this.pop();
            //--this.length;
        }
        else {
            output = null;
        }
        return output;
    };

    UniqueSequence.prototype.swap = function(index0, index1) {
        var tmp = this[index0];
        this[index0] = this[index1];
        this[index1] = tmp;
    };

    function createUniqueSequence() {
        return new UniqueSequence;
    }
    //*****/

    var srcClassName = "dndx-src",
        tgtClassName = "dndx-tgt",
        srcDataKey = "dndx-sk@",
        defaultZ = "10000",
        noop = function() {}, 
        configuration = null,
        apiOwner = null,
        dataStore = null;

    function createDataStore() {
        dataStore = {
            pairs: {},
            protoDraggableOptions: {
                scroll: false,
                zIndex: defaultZ,
                containment: "document",
                appendTo: "body",
            },
            protoDroppableOptions: {
                //greedy: true,
                //tolerance: "pointer",
            },
            protoPair: {
                visualcue: noop,
                cbConflict: noop,
                cbStart: noop,
                cbStop: noop,
                cbActivate: noop,
                cbDeactivate: noop,
                cbOver: noop,
                cbOut: noop,
                cbDrop: noop,
                cursorForDrag: "move",
                cursorForHover: "pointer",
            },
        };
    }

    //function forEachPair(pairs, cb) {
        //for (var srcSelector in pairs) {
            //for (var tgtSelector in pairs[srcSelector]) {
                //cb(pairs[srcSelector][tgtSelector]);
            //}
        //}
    //}

    function forEachSelector(pairs, cbForSrc, cbForTgt) {
        var tgtSet = {};
        for (var srcSelector in pairs) {
            cbForSrc(srcSelector);
            for (var tgtSelector in pairs[srcSelector]) {
                if (tgtSelector in tgtSet === false) {
                    tgtSet[tgtSelector] = true;
                    cbForTgt(srcSelector, tgtSelector);
                }
            }
        }
    }

    function setupSource(pairs, srcSelector) {
        // Create a new jQuery ui draggable object
        createDraggable(srcSelector);

        // Create a new slot for the given source selector
        pairs[srcSelector] = pairs[srcSelector] || {};

        // Add a property for the common settings for all pairs
        // associated with this source selector
        Object.defineProperty(pairs[srcSelector], srcClassName, {
            enumerable:false,
            writable:true,
            configurable:true,
            value:Object.create(dataStore.protoPair),
        });
        return pairs[srcSelector];
    }

    function setupPair(pairs, srcSelector, tgtSelector) {
        if (srcSelector in pairs === false) {
            setupSource(pairs, srcSelector);
        }

        if (tgtSelector in pairs[srcSelector] === false) {
            // Create a new jQuery ui droppable object
            createDroppable(srcSelector, tgtSelector);
        }

        return (pairs[srcSelector][tgtSelector] = 
                pairs[srcSelector][tgtSelector] || 
                Object.create(pairs[srcSelector][srcClassName], {
                    srcSelector:{ value:srcSelector, },
                    tgtSelector:{ value:tgtSelector, },
                }));
    }

    function embedSourceKey($obj, srcSelector) {
        var data = $obj.data(srcDataKey);
        if (data && data !== srcSelector) {
            throw new RangeError("Source group " + data + " and " + srcSelector + " share a common DOM object. DNDX doesn't support this kind of configuration!");
        }
        $obj.data(srcDataKey, srcSelector);
    }

    function embedDraggableHelperCreator(dstOptions, helperOption) {
        function createCloneHelper() {
            var $this = $(this), clone = $this.clone().removeAttr("id").data(srcDataKey, $this.data(srcDataKey));
            return clone;
        }

        if (helperOption === "clone") {
            dstOptions.helper = createCloneHelper;
        }
        else if (helperOption instanceof Function) {
            dstOptions.helper = function(e) {
                var selector = $(this).data(srcDataKey), helper = helperOption.call(this, e);
                $(helper).data(srcDataKey, selector).removeClass(tgtClassName);
                return helper;
            };
        }
    }

    function createDraggable(srcSelector) {
        var $obj = $(srcSelector);
        $obj.draggable(dataStore.protoDraggableOptions).addClass(srcClassName);
        embedSourceKey($obj, srcSelector);
    }

    function createDroppable(srcSelector, tgtSelector) {
        var $obj = $(tgtSelector);
        $obj.droppable(dataStore.protoDroppableOptions).addClass(tgtClassName);
    }

    function extendDraggableOptions(originalOptions, optionsToAdd) {
        var mergedOptions;
        optionsToAdd = optionsToAdd || {};
        if (originalOptions === dataStore.protoDraggableOptions)
            mergedOptions = $.extend(originalOptions, optionsToAdd);
        else
            mergedOptions = $.extend({}, originalOptions, optionsToAdd);
        embedDraggableHelperCreator(mergedOptions, optionsToAdd.helper);
        return mergedOptions;
    }

    function extendDroppableOptions(originalOptions, optionsToAdd) {
        var mergedOptions;
        if (originalOptions === dataStore.protoDroppableOptions)
            mergedOptions = $.extend(originalOptions, optionsToAdd);
        else
            mergedOptions = $.extend({}, originalOptions, optionsToAdd);
        return mergedOptions;
    }

    function refreshDraggable(srcSelector, options) {
        var $obj = $(srcSelector), instance = $obj.draggable("instance"),
            finalOptions = extendDraggableOptions(instance ? instance.options : {}, options);
            //finalOptions = $.extend({}, instance ? instance.options : {}, options);
        //embedDraggableHelperCreator(finalOptions, options.helper);
        $obj.draggable(finalOptions).addClass(srcClassName);
        embedSourceKey($obj, srcSelector);
    }

    function refreshDroppable(srcSelector, tgtSelector, options) {
        var $obj = $(tgtSelector), instance = $obj.droppable("instance");
        $obj.droppable(extendDroppableOptions(instance ? instance.options : {}, options)).addClass(tgtClassName);
        //$obj.droppable($.extend({}, instance ? instance.options : {}, options)).addClass(tgtClassName);
    }

    function refreshPair(srcSelector, tgtSelector) { 
        refreshDraggable(srcSelector);
        refreshDroppable(srcSelector, tgtSelector);
    }

    function refreshPairs(pairs) {
        forEachSelector(pairs, function(srcSelector) {
            refreshDraggable(srcSelector, dataStore.protoDraggableOptions);
        }, function(srcSelector, tgtSelector) {
            refreshDroppable(srcSelector, tgtSelector, dataStore.protoDroppableOptions);
        });
    }

    function findPairsForTargetSelector(tgtSelector) {
        var srcSelector, output = [];
        for (srcSelector in dataStore.pairs) {
            if (dataStore.pairs[srcSelector][tgtSelector]) {
                output.push(dataStore.pairs[srcSelector][tgtSelector]);
            }
        }
        return output;
    }

    function removePair(srcSelector, tgtSelector, removeUnderlingObjects) {
        if (srcSelector && tgtSelector) {
            // Remove a specific pair
            delete dataStore.pairs[srcSelector][tgtSelector];
            if (removeUnderlingObjects === true && findPairsForTargetSelector(tgtSelector).length === 0) {
                $(tgtSelector).droppable("destroy");
            }
            if ($.isEmptyObject(dataStore.pairs[srcSelector])) {
                delete dataStore.pairs[srcSelector];
                if (removeUnderlingObjects === true) {
                    $(srcSelector).draggable("destroy");
                }
            }
        }
        else if (srcSelector) {
            // Remove pairs grouped by source selector
            delete dataStore.pairs[srcSelector];
            if (removeUnderlingObjects === true) {
                $(srcSelector).draggable("destroy");
            }
        }
        else {
            // Remove all pairs
            dataStore.pairs = {};
            if (removeUnderlingObjects === true) {
                $(".ui-draggable").draggable("destroy");
                $(".ui-droppable").droppable("destroy");
            }
        }
    }

    function assignCallback(pair, source, slotName, cb, defaultOp) {
        var owner = pair || (source && source[srcClassName]) || dataStore.protoPair;
        cb = cb || defaultOp;
        if (cb === "fallback") {
            if (owner !== dataStore.protoPair)
                delete owner[slotName];
        }
        else if (cb instanceof Function) {
            owner[slotName] = cb;
        }
    }

    function showOverlay(srcObj, $tgtObj) {
        var id = "dndx-visualcue-canvas", canvas = document.getElementById(id),
            ctx, i, c, $obj, rc, padding = 5;
        if (!canvas) {
            canvas = document.createElement("canvas");
            document.body.appendChild(canvas);
            canvas.id = id;
            canvas.className = "dndx-visualcue-overlay ui-front";
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;

            ctx = canvas.getContext("2d");
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            rc = srcObj.getBoundingClientRect();
            ctx.clearRect(rc.left, rc.top, rc.width, rc.height);
        }
        ctx = ctx || canvas.getContext("2d");

        $obj = $tgtObj;
        for (i=0, c=$obj.length; i<c; ++i) {
            rc = $obj[i].getBoundingClientRect();
            ctx.clearRect(rc.left - padding, rc.top - padding, rc.width + padding*2, rc.height + padding*2);
        }
    }

    function hideOverlay() {
        var canvas = document.getElementById("dndx-visualcue-canvas");
        if (!canvas || canvas.hiding === "yes")
            return;
        canvas.hiding = "yes";
        canvas.style.opacity = 0;
        setTimeout(function() {
            canvas.parentNode.removeChild(canvas);
        }, 300);
    }

    var builtinVisualcueOwner = {
        visualcueNothing : noop,
        visualcueOverlay : function(eventType, $srcObj, $tgtObj) {
            switch (eventType) {
            case "dropactivate":
                showOverlay($srcObj[0], $tgtObj);
                break;
            case "dropdeactivate":
                hideOverlay();
                break;
            case "drop":
                $tgtObj.removeClass("dndx-visualcue-gradient");
                break;
            case "dropover": 
                $tgtObj.addClass("dndx-visualcue-gradient");
                break;
            case "dropout":
                $tgtObj.removeClass("dndx-visualcue-gradient");
                break;
            }
        },
        visualcueSwing : function(eventType, $srcObj, $tgtObj) {
            switch (eventType) {
            case "dropactivate":
                $tgtObj.addClass("dndx-visualcue-swing"); 
                break;
            case "dropdeactivate":
                $tgtObj.removeClass("dndx-visualcue-swing dndx-visualcue-gradient");
                break;
            case "dropover": 
                $tgtObj.addClass("dndx-visualcue-gradient");
                break;
            case "dropout":
                $tgtObj.removeClass("dndx-visualcue-gradient");
                break;
            }
        },
        visualcueExterior : function(eventType, $srcObj, $tgtObj) {
            switch (eventType) {
            case "dropactivate":
                $tgtObj.addClass("dndx-visualcue-exterior-activate"); 
                break;
            case "dropdeactivate":
                $tgtObj.removeClass("dndx-visualcue-exterior-activate dndx-visualcue-exterior-over");
                break;
            case "dropover": 
                $tgtObj.addClass("dndx-visualcue-exterior-over");
                break;
            case "dropout":
                $tgtObj.removeClass("dndx-visualcue-exterior-over");
                break;
            }
        },
    };

    function builtinVisualcue(name) {
        var vcName = "visualcue" + name;
        if (vcName in builtinVisualcueOwner === false) {
            triggerException("No builtin visualcue of such a name : " + name);
        }
        return builtinVisualcueOwner[vcName];
    }

    function cleanup() {
        unbindEventHandlers();
        apiOwner = dataStore = null;
    }

    // This function defines all APIs (except dndx() function) for the library and associates them to the object 'apiOwner'
    function defineAPIs() {
        apiOwner = {}; // This object owns all public functions of the library

        // CHAINABLE METHODS
        apiOwner.targets = function(tgtSelector) {
            validateSelector(tgtSelector);
            if (!this.srcSelector) {
                triggerException("Should not be called from the global level!");
            }
            setupPair(dataStore.pairs, this.srcSelector, tgtSelector);
            return createChainable(this.srcSelector, tgtSelector);
        };

        apiOwner.draggableOptions = function(options) {
            if (this.srcSelector) {
                refreshDraggable(this.srcSelector, options);
            }
            else {
                extendDraggableOptions(dataStore.protoDraggableOptions, options);
                //$.extend(dataStore.protoDraggableOptions, options);
                refreshPairs(dataStore.pairs);
            }
            return this;
        };
        apiOwner.droppableOptions = function(options) {
            if (this.srcSelector && this.tgtSelector) {
                refreshDroppable(this.srcSelector, this.tgtSelector, options);
            }
            else {
                extendDroppableOptions(dataStore.protoDroppableOptions, options);
                //$.extend(dataStore.protoDroppableOptions, options);
                refreshPairs(dataStore.pairs);
            }
            return this;
        };

        apiOwner.visualcue = function(param) {
            var owner = this.pair || (this.source && this.source[srcClassName]) || dataStore.protoPair;
            if (param === "fallback") {
                delete owner.visualcue;
            }
            else if (typeof param === "string") {
                owner.visualcue = builtinVisualcue(param);
            }
            else if (param instanceof Function) {
                owner.visualcue = param;
            }
            else if (param === null) {
                owner.visualcue = noop;
            }
            return this;
        };

        apiOwner.oncheckpair = function(cb) {
            assignCallback(this.pair, this.source, "cbCheckPair", cb);
            return this;
        };

        apiOwner.onconflict = function(cb) {
            assignCallback(this.pair, this.source, "cbConflict", cb, noop);
            return this;
        };

        apiOwner.onstart = function(cb) {
            if (this.pair) {
                triggerException("Should not be called from the pair level!");
            }
            assignCallback(null, this.source, "cbStart", cb, noop);
            return this;
        };
        apiOwner.onstop = function(cb) {
            if (this.pair) {
                triggerException("Should not be called from the pair level!");
            }
            assignCallback(null, this.source, "cbStop", cb, noop);
            return this;
        };

        apiOwner.onactivate = function(cb) {
            assignCallback(this.pair, this.source,  "cbActivate", cb, noop);
            return this;
        };
        apiOwner.ondeactivate = function(cb) {
            assignCallback(this.pair, this.source,  "cbDeactivate", cb, noop);
            return this;
        };
        apiOwner.onover = function(cb) {
            assignCallback(this.pair, this.source,  "cbOver", cb, noop);
            return this;
        };
        apiOwner.onout = function(cb) {
            assignCallback(this.pair, this.source,  "cbOut", cb, noop);
            return this;
        };
        apiOwner.ondrop = function(cb) {
            assignCallback(this.pair, this.source,  "cbDrop", cb, noop);
            return this;
        };

        //apiOwner.asSortableList = function(tgtSelector, options) {
            //validateSelector(tgtSelector);
            //var chainable = this;
            //if (this.source) {
            //}
            //return chainable;
        //};

        apiOwner.nullify = function() {
            if (this.pair) {
                this.pair.nullified = true;
                this.pair.visualcue = builtinVisualcue("Nothing");
                this.pair.cbActivate = this.pair.cbDeactivate = this.pair.cbOver = this.pair.cbOut = this.pair.cbDrop = noop;
            }
            return this;
        };

        apiOwner.refresh = function() {
            if (this.srcSelector && this.tgtSelector) {
                refreshPair(this.srcSelector, this.tgtSelector);
            }
            if (this.source) {
                var tmp = {};
                tmp[this.srcSelector] = this.source;
                refreshPairs(tmp);
            }
            else {
                refreshPairs(dataStore.pairs);
            }
            return this;
        };

        apiOwner.newPair = function(srcSelector, tgtSelector) {
            srcSelector = srcSelector || this.srcSelector;
            if (!srcSelector) {
                triggerException("Source selector should be specified!");
            }
            validateSelectors(srcSelector, tgtSelector);
            if (this.srcSelector && this.tgtSelector) {
                var srcPair = dataStore.pairs[this.srcSelector][this.tgtSelector],
                    dstPair = setupPair(dataStore.pairs, srcSelector, tgtSelector);
                var srcKeys = Object.keys(srcPair), i, c, k;
                for (i=0,c=srcKeys.length; i<c; ++i) {
                    k = srcKeys[i];
                    dstPair[k] = srcPair[k];
                }
                dataStore.pairs[srcSelector][tgtSelector] = dstPair;
            }
            return createChainable(srcSelector, tgtSelector);
        };

        apiOwner.configure = function(configOptions) {
            $.extend(configuration, configOptions);
            return this;
        };

        function _disable(b) {
            if (this.pair) {
                this.pair.disabled = b;
            }
            else if (this.source) {
                for (var tgtSelector in this.source) {
                    delete this.source[tgtSelector].disabled;
                }
                this.source[srcClassName].disabled = b;
            }
            else {
                forEachSelector(this.pairs, function(srcSelector) {
                    delete this.pairs[srcSelector][srcClassName].disabled;
                }, function(srcSelector, tgtSelector) {
                    delete this.pairs[srcSelector][tgtSelector].disabled;
                });
                dataStore.protoPair.disabled = b;
            }
            return this;
        }

        apiOwner.disable = function() {
            return _disable.call(this, true);
        };
        
        apiOwner.enable = function() {
            return _disable.call(this, false);
        };

        apiOwner.cursor = function(dragType, hoverType) {
            if (this.pair)
                return this;
            var owner = (this.source && this.source[srcClassName]) || dataStore.protoPair;
            owner.cursorForDrag = dragType || "move";
            owner.cursorForHover = hoverType || "pointer";
            return this;
        };

        // NON CHAINABLE METHODS
        apiOwner.remove = function(removeUnderlingObjects) {
            removePair(this.srcSelector, this.tgtSelector, removeUnderlingObjects);
        };

        apiOwner.destroy = cleanup;

        if ($.data(document.body, "dndx-under-inspection")) {
            // DEBUG/TEST METHODS (NOT FOR PRODUCTION USE!!!)
            apiOwner.dataStore = function() {
                return dataStore;
            };

            apiOwner.sourceDataKeyName = function() {
                return srcDataKey;
            };

            apiOwner.sourceGroup = function() {
                if (this.source)
                    return this.source[srcClassName];
            };

            apiOwner.uniqueSequence = function() {
                return createUniqueSequence();
            };
        }
    }

    // This function binds all required event handlers to the 'body' element of the DOM tree
    function bindEventHandlers() {
        function grabPair($src, $tgt) {
            var srcSelector = $src.data(srcDataKey), pair;
            if (srcSelector in dataStore.pairs === false || !$src.is(srcSelector))
                return null;
            for (var tgtSelector in dataStore.pairs[srcSelector]) {
                if (!$tgt.is(tgtSelector))
                    continue;
                pair = dataStore.pairs[srcSelector][tgtSelector];
                return pair.disabled ? null : pair;
            }
        }

        var tgtClass = "." + tgtClassName, srcClass = "." + srcClassName, eventContext = null;

        function rejectTarget(tgt, ec) {
            if (!ec)
                return true;
            var c = (ec.blacklist && ec.blacklist.length) || 0, i;
            for (i=0; i<c; ++i) {
                if (tgt === ec.blacklist[i])
                    return true;
            }
            return false;
        }

        $("body").off(".dndx")
        .on("dragstart.dndx", srcClass, function(e, ui) {
            e.stopPropagation();
            var srcSelector = ui.helper.data(srcDataKey);
            if (srcSelector in dataStore.pairs === false)
                return false;
            var etc = { srcSelector:srcSelector, position:ui.position, offset:ui.offset, event:e, },
                srcSettings = dataStore.pairs[srcSelector][srcClassName];
            srcSettings.cbStart(e.type, ui.helper, [], etc);
            ui.helper.css({ cursor: srcSettings.cursorForDrag, });
        })
        .on("dragstop.dndx", srcClass, function(e, ui) {
            e.stopPropagation();
            var srcSelector = ui.helper.data(srcDataKey);
            if (srcSelector in dataStore.pairs === false)
                return false;
            var etc = { srcSelector:srcSelector, originalPosition:ui.originalPosition, position:ui.position, offset:ui.offset, event:e, },
                srcSettings = dataStore.pairs[srcSelector][srcClassName];
            srcSettings.cbStop(e.type, ui.helper, [], etc);
            ui.helper.css({ cursor: srcSettings.cursorForHover, });
        })
        .on("dropactivate.dndx", tgtClass, function(e, ui) {
            var pair = grabPair(ui.draggable, $(e.target));
            if (pair) {
                eventContext = eventContext || { pairs:[], };
                if (eventContext.pairs.indexOf(pair) === -1) {
                    eventContext.pairs.push(pair);
                    var $tgtObj = $(pair.tgtSelector).not(".ui-draggable-dragging");
                    if (pair.cbCheckPair instanceof Function) {
                        eventContext.blacklist = eventContext.blacklist || createUniqueSequence();
                        $tgtObj = $tgtObj.not(function (idx, elem) {
                            if (!pair.cbCheckPair(ui.draggable, $(elem), pair.srcSelector, pair.tgtSelector)) {
                                eventContext.blacklist.pushFront(elem);
                                return true;
                            }
                        });
                    }
                    var etc = { srcSelector: pair.srcSelector, tgtSelector:pair.tgtSelector, };
                    if (! pair.nullified) {
                        pair.visualcue(e.type, ui.draggable, $tgtObj, etc);
                    }
                    pair.cbActivate(e.type, ui.draggable, $tgtObj, etc);
                }
            }
            return false;
        })
        .on("dropdeactivate.dndx", tgtClass, function(e, ui) {
            if (!eventContext) {
                return false;
            }
            var pair = grabPair(ui.draggable, $(e.target));
            if (pair && pair !== eventContext.focusedPair) {
                var iii = eventContext.pairs.indexOf(pair);
                if (iii > -1) {
                    eventContext.pairs.splice(iii, 1);
                    var $tgtObj = $(pair.tgtSelector).filter(function() {
                        return ! rejectTarget(this, eventContext);
                    });
                    var etc = { srcSelector: pair.srcSelector, tgtSelector:pair.tgtSelector, };
                    pair.visualcue(e.type, ui.draggable, $tgtObj, etc);
                    pair.cbDeactivate(e.type, ui.draggable, $tgtObj, etc);
                    if (eventContext.pairs.length === 0) {
                        eventContext = null;
                    }
                }
            }
            return false;
        })
        .on("dropover.dndx", tgtClass, function(e, ui) {
            if (rejectTarget(e.target, eventContext)) {
                return false;
            }
            var pair = grabPair(ui.draggable, $(e.target));
            if (pair) {
                var hitTargets = eventContext.hitTargets || (eventContext.hitTargets = createUniqueSequence()),
                    head = hitTargets.front(), $head = $(head), $tgt = $(e.target),
                    prevPair = eventContext.focusedPair, etc;

                eventContext.$src = ui.draggable;
                eventContext.focusedPair = pair;

                if (head && head !== e.target) {
                    // Resolve conflicts between multiple hits
                    var selected = pair.cbConflict(ui.draggable, $head, $tgt) || $head;
                    if (selected[0] === head) {
                        hitTargets.push(e.target);
                        eventContext.focusedPair = prevPair;
                        return false;
                    }
                    etc = { srcSelector:prevPair.srcSelector, tgtSelector:prevPair.tgtSelector, };
                    prevPair.visualcue("dropout", eventContext.$src, $head, etc);
                    prevPair.cbOut("dropout", eventContext.$src, $head, etc);
                }

                etc = { srcSelector:pair.srcSelector, tgtSelector:pair.tgtSelector, };
                pair.visualcue(e.type, eventContext.$src, $tgt, etc);
                pair.cbOver(e.type, eventContext.$src, $tgt, etc);

                hitTargets.pushFront(e.target);
            }
            return false;
        })
        .on("dropout.dndx", tgtClass, function(e, ui) {
            if (rejectTarget(e.target, eventContext)) {
                return false;
            }
            if (!eventContext) {
                return false;
            }
            var pair = eventContext.focusedPair, hitTargets = eventContext.hitTargets,
                head = hitTargets ? hitTargets.front() : null, $head = $(head), etc;
            if (pair) {
                hitTargets.remove(e.target);
                if (head === e.target) {
                    etc = { srcSelector:pair.srcSelector, tgtSelector:pair.tgtSelector, };
                    pair.visualcue(e.type, eventContext.$src, $head, etc);
                    pair.cbOut(e.type, eventContext.$src, $head, etc);

                    if (hitTargets.length > 1) {
                        var i, c, selected = $(hitTargets[0]);
                        for (i=1,c=hitTargets.length; i<c; ++i) {
                            selected = pair.cbConflict(eventContext.$src, selected, $(hitTargets[i])) || selected;
                        }
                        hitTargets.pushFront(selected[0]);
                    }
                    else if (hitTargets.length === 0) {
                        eventContext.focusedPair = eventContext.$src = eventContext.hitTargets = null;
                        return false;
                    }

                    head = hitTargets.front(), $head = $(head);
                    eventContext.focusedPair = pair = grabPair(eventContext.$src, $head);
                    etc = { srcSelector:pair.srcSelector, tgtSelector:pair.tgtSelector, };
                    pair.visualcue("dropover", eventContext.$src, $head, etc);
                    pair.cbOver("dropover", eventContext.$src, $head, etc);
                }
            }
            return false;
        })
        .on("drop.dndx", tgtClass, function(e, ui) {
            if (rejectTarget(e.target, eventContext)) {
                return false;
            }
            if (!eventContext.focusedPair) {
                return false;
            }
            var pair = grabPair(ui.draggable, $(e.target));
            if (pair) {
                var hitTargets = eventContext.hitTargets, head = hitTargets.front(), $head = $(head);
                if (head === e.target) {
                    var etc = { srcSelector:pair.srcSelector, tgtSelector:pair.tgtSelector, };
                    pair.visualcue(e.type, ui.draggable, $head, etc);
                    pair.cbDrop(e.type, ui.draggable, $head, etc);
                    eventContext.focusedPair = eventContext.$src = eventContext.hitTargets = null;
                }
            }
            return false;
        })
        ;
    }

    function unbindEventHandlers() {
        $("body").off(".dndx");
    }

    function triggerException(msg) {
        throw new Error(msg);
    }

    function validateSelector(selector) {
        if (typeof selector !== "string" || selector === "") {
            triggerException("Selectors should be all valid strings!");
        }
        if (configuration.strictValidation && $(selector).length === 0) {
            triggerException("DOM objects for a selector '" + selector + "' don't exist!");
        }
    }

    function validateSelectors(srcSelector, tgtSelector) {
        validateSelector(srcSelector);
        validateSelector(tgtSelector);
    }

    function createChainable(srcSelector, tgtSelector) {
        var pair;
        if (srcSelector && tgtSelector) {
            // Retrieve the requested pair.
            // Create one unless it exists
            pair = setupPair(dataStore.pairs, srcSelector, tgtSelector);
        }
        else if (srcSelector && srcSelector in dataStore.pairs === false) {
            setupSource(dataStore.pairs, srcSelector);
        }

        return Object.create(apiOwner, {
            srcSelector : { value: srcSelector, },
            tgtSelector : { value: tgtSelector, },
            pair : { value: pair, },
            source : { value : dataStore.pairs[srcSelector], },
        });
    }

    // Root API that can chain other API calls;
    // See defineAPIs() function for various APIs chainable from this function
    dndx = function(srcSelector, tgtSelector) {
        if (!apiOwner) {
            // Initialize core objects and functions
            configuration = $.extend(configuration, {});
            createDataStore();
            defineAPIs();
            bindEventHandlers();
        }

        if (arguments.length === 0) {
            srcSelector = tgtSelector = "";
        }
        else if (srcSelector && !tgtSelector) {
            validateSelector(srcSelector);
            tgtSelector = "";
        }
        else {
            validateSelectors(srcSelector, tgtSelector);
        }

        return createChainable(srcSelector, tgtSelector);
    }; 

    dndx.destroy = cleanup;

}(jQuery));

