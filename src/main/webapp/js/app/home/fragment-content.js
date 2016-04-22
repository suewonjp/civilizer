function setupParser() {
    // prepare *Marked* (a Markdown parser) library; see https://github.com/chjj/marked
    marked.setOptions({
        renderer: new marked.Renderer(),
        gfm: true,
        tables: true,
        breaks: false,
        pedantic: false,
        sanitize: true,
        smartLists: true,
        smartypants: false,
    });
}

function translateFragments() {
    var fg = $("#fragment-group");
    
    // highlight any search keyword contained in fragment titles;
    fg.find(".fragment-title").each(function() {
        var $this = $(this);
        $this.html(translateSearchKeywordCommands($this.html()));
    });
    
    // translate Markdown formatted fragment contents into HTML format
    fg.find(".fragment-content").each(function() {
        var $this = $(this);
        var text = $this.text();
        $this.html(translateFragmentContent(text));
        postprocessFragmentContent($this);
        if ($this.hasClass("fp-search")) {
            // Highlight search keywords
            $this.find("*").each(function() {
               var elem = $(this), attrs = this.attributes;
               // Remove any of search keyword markups from HTML attributes
               for (var i=0; i<attrs.length; ++i) {
                   var attr = attrs[i];
                   attr.value = removeSearchKeywordCommands(attr.value);                   
                   elem.attr(attr.name, attr.value);
               }
            });
            // Translate search keyword markups
            $this.html(translateSearchKeywordCommands($this.html()));
        }
    });
    
    // add a tooltip message of updated and created time
    fg.find(".fragment-header .fa-clock-o, .fragment-header .fa-birthday-cake").each(function() {
        var $this = $(this);
        $this[0].title = formatDatetime($this.prev("span").text());
    });
    
    fg.find(".each-tag").each(function() {
        formatTagsOnFragmentHeader($(this));
    });
    
    // set click handler for every folding in fragments and fragment overlay
    $("body").off("click.fold_toggle").on("click.fold_toggle", ".-cvz-fold-handle", function(e) {
        var $this = $(this);
        $this.find(".fold-toggle-icon").toggleClass("fa-plus-square fa-minus-square");
        showOrHide($this.next(".-cvz-fold"));
        e.preventDefault();
    });
}

function populateFragmentOverlay(data) {
    // show the fragment overlay as a popup
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
    
    $("#fragment-overlay-title").text("");
    
    // Set up a link to the previous fragment if any.
    var overlayContent = $("#fragment-overlay-content");
    var prevHdr = overlayContent.find(".fragment-header");
    var backBtn = $("#fragmen-overlay-back-button");
    if (prevHdr.length > 0) {
        backBtn.show().attr("href", "fragment/"+prevHdr.attr("_fid"))
            .addClass("-cvz-frgm");
    }
    else {
        backBtn.hide();
    }
    
    overlayContent.html(data);
    
    $("#fragment-overlay-content .fragment-header .fa-clock-o, #fragment-overlay-content .fragment-header .fa-birthday-cake")
    .each(function() {
        var $this = $(this);
        $this[0].title = formatDatetime($this.prev("span").text());
    });
    
    // translate Markdown formatted fragment contents into HTML format
    overlayContent.find(".fragment-content").each(function(){
        var $this = $(this);
        $this.html(translateFragmentContent($this.text()));
        postprocessFragmentContent($this);
    });
    
    // make titles on the overlay window also draggable/droppable
    setupDraggableForFragmentTitle();
    
    setupDndForRelatingFragments();
    
    setContextMenuForFragments();
    
    setContextMenuForTags();
    
    overlayContent.find(".each-tag").draggable(baseDraggableSettings)
    .each(function() {formatTagsOnFragmentHeader($(this))});
    
    overlayFrame.off("click.cvz_frg_overlay").on("click.cvz_frg_overlay", ".-cvz-frgm", triggerFragmentOverlay);
//    setupClickHandlerForTags(overlayContent);
    
    setupQuickFragmentEditing(overlayContent.find(".fragment-header"));
}

const SEARCH_KEYWORD_PATTERN_BEGIN = /%28%7B%28%5B(.+?)%5D%20/g;
const SEARCH_KEYWORD_PATTERN_END = /%20%29%7D%29/g;
//const SEARCH_KEYWORD_PATTERN_BEGIN = /\(\{\(\[(.+?)\] /g;
//const SEARCH_KEYWORD_PATTERN_END = / \)\}\)/g;

function translateSearchKeywordCommands(html) {
    return html
        // ({([keyword] ... text ... )}) --- translated to a <span>;
        // used for one special purpose; highlighting search phrase
        .replace(SEARCH_KEYWORD_PATTERN_BEGIN, function(match, p1, pos, originalText) {
            return "<span class='-cvz-" + p1 + "'>"; 
        })
        .replace(SEARCH_KEYWORD_PATTERN_END, function(match, pos, originalText) {
            return "</span>";
        })
        ;
}

function removeSearchKeywordCommands(html) {
    return html
        .replace(SEARCH_KEYWORD_PATTERN_BEGIN, function(match, p1, pos, originalText) {
            return ""; 
        })
        .replace(SEARCH_KEYWORD_PATTERN_END, function(match, pos, originalText) {
            return "";
        })
        ;
}

function translateCustomMarkupCommands(html) {
    return html
    // {{{[keyword] ... text ... }}} --- translated to a <div> block
    .replace(/\{\{\{\[(.+?)({.*})?\]\s?/g, function(match, p1, p2, pos, originalText) {
        var output = "<div class='-cvz-" + p1 + "'"; 
        if (p2) {
            output += " args='" + p2.trim() + "'";
        }
        return output + ">"; 
    })
    .replace(/\s?\}\}\}/g, function(match, pos, originalText) {
        return "</div>";
    })
    // {{[keyword] ... text ... }} --- translated to a <span>
    .replace(/\{\{\[([^,]+?)\]\s?/g, function(match, p1, pos, originalText) {
        return "<span class='-cvz-" + p1 + "'>";
    })
    .replace(/\s?\}\}/g, function(match, pos, originalText) {
        return "</span>";
    })
    .replace(/((&amp;#125;){2,3})/g, function(match, p1) {
        var output = "";
        for (var i=0,c=p1.length; i<c; ++i) {
            if (p1[i] === "&") output += "}";
        }
        return output;
    })
    ;
}

function translateFragmentContent(content) {
    // translate Markdown text into HTML;
    var outputHtml = parseMarkdown(content);
    
    // take care of custom markup commands
    return translateCustomMarkupCommands(outputHtml);
}

function setupFragmentLinks(content) {
    content.find("span.-cvz-frgm").each(function() {
        var $this = $(this);
        var txt = $this.text();
        var patt = /\s*(\d+)\s+(.*)/g;
        if (txt.match(patt)) {
            var newTag = $("<a class='-cvz-frgm' href='fragment/" + RegExp.$1 + "'>" + RegExp.$2 + "</a>");
            $this.replaceWith(newTag);
        }
    });
}

function processFoldings(content) {
    content.find(".-cvz-fold").each(function() {
        var $this = $(this), args = parseJsonArgs($this);
        
        // search keyword commands may touch folding parameters.
        // when that happens, we need to restore original key names;
        for (var k in args) {
            var v = args[k];
            k = removeSearchKeywordCommands(k);
            args[k] = v;
        }
        
        $this.wrapInner("<blockquote>");
        var handle = $("<a href='#' class='-cvz-fold-handle'>");
        var icon = $("<span class='fold-toggle-icon fa fa-plus-square'>");
        if (args.title) {
            handle.append($("<span class='-cvz-fold-title'>"+args.title+"</span>"));
        }
        if (args.hide == "true") {
            handle.click();
            // with the only above code, the target element would be hidden.
            // but it doesn't work on Safari, so make sure it becomes hidden.
            $this.hide();
        }
        $this.before(handle.prepend(icon));
    });
}

function processEmbeddedFragments(content) {
    content.find(".-cvz-frgm-embed").each(function() {
        var $this = $(this);
        $.get("fragment/"+$this.text().trim(), "", function(data) {
            var content = $(data).find(".fragment-content").text();
            $this.html(translateFragmentContent(content));
            postprocessFragmentContent($this);
        });
    })
}

function unsanitizeHtml(content) {
    content.find(".-cvz-html").each(function() {
        var $this = $(this);
        var res = $this.text()
            .replace(/<(\/?)(.+)>/g, function(match, cg1, cg2, pos, originalText) {
//                var cg1 = RegExp.$1;
//                var cg2 = RegExp.$2;
                if (cg2.indexOf("html") == 0
                   || cg2.indexOf("head") == 0
                   || cg2.indexOf("body") == 0
                   || cg2.indexOf("link") == 0
                   || cg2.indexOf("script") == 0
                   )
                    // these tags should be sanitized for safety
                    return "&lt;"+cg1+cg2+"&gt";
                return match;
            });
        $this.text(null).html(res);
    });
}

function embedYoutubeVideo(content) {
    function kickYoutubeVideo() {
        var $this = $(this);
        var iframe = $("<iframe src='//www.youtube.com/embed/"
            + $this.attr("_vid") + "' frameborder='0' allowfullscreen>");
        $this.replaceWith(iframe);
        return false;
    }
    
    content.find("iframe[src*='www.youtube.com/embed/'], a[href*='youtu.be/'], a[href*='www.youtube.com/watch?']")
    .each(function() {
        var $this = $(this), videoId;
        if ($this.is("iframe"))
            videoId = suffix($this.attr("src"), "/", true);
        else if ($this.is("a[href*='youtu.be/']"))
            videoId = suffix($this.attr("href"), "/", true);
        else if ($this.is("a[href*='www.youtube.com/watch?']"))
            videoId = inbetween($this.attr("href"), "v=", "&")
                || suffix($this.attr("href"), "v=", true);
        var div = $("<div class='yt-video' _vid='"+videoId+"'>" +
            '<img class="yt-thumb" title="' + $this.text() +
            '" src="//i.ytimg.com/vi/' + videoId +
            '/mqdefault.jpg"/><div class="fa fa-youtube-play"></div></div>')
            .click(kickYoutubeVideo);
        $this.replaceWith(div);
    });
}

function postprocessFragmentContent(content) {
    // translate foldings
    // Rule - {{{[fold]...}}}
    processFoldings(content);

    // translate embedded fragemnts
    // Rule - {{[frgm-embed]...}}
    processEmbeddedFragments(content);
    
    // translate fragemnt links
    // Rule - {{[frgm]...}}
    setupFragmentLinks(content);

    // translate tag links
    // Rule - {{[tag]...}}
    setupTagLinks(content);
    
    // convert links to images into <img> tags
    content.find("a").each(function() {
        var $this = $(this);
        var href = $this.attr("href");
        if (isImage(suffix(href, ".")))
            $this.replaceWith("<img src='"+href+"'>");
    });
    
    // translate file box elements into HTML links
    // Rule - {{[file]...}}
    processFileClasses(content);
    
    // unsanitize HTML code if any
    // Rule - {{[html]...}}
    unsanitizeHtml(content);
    
    embedYoutubeVideo(content);
}
