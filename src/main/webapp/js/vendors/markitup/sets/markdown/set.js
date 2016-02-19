// -------------------------------------------------------------------
// markItUp!
// -------------------------------------------------------------------
// Copyright (C) 2008 Jay Salvat
// http://markitup.jaysalvat.com/
// -------------------------------------------------------------------
// MarkDown tags example
// http://en.wikipedia.org/wiki/Markdown
// http://daringfireball.net/projects/markdown/
// -------------------------------------------------------------------
// Feel free to add more tags
// -------------------------------------------------------------------
markItUpSettings = {
	previewParserPath:	'',
	onShiftEnter:		{keepDefault:false, openWith:'\n\n'},
	onTab:    		    {keepDefault:false, selectAssist:true, multiline:true, indent:4},
	onShiftTab:    		{keepDefault:false, selectAssist:true, multiline:true, outdent:4},
	markupSet: [
		{className:'miu-h1', name:'First Level Heading', selectAssist:true, onAlt:true, key:'1', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '=') } },
		{className:'miu-h2', name:'Second Level Heading', selectAssist:true, onAlt:true, key:'2', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '-') } },
		{className:'miu-h3', name:'Heading 3', selectAssist:true, onAlt:true, key:'3', openWith:'### ', placeHolder:'Your title here...', multiline:true },
		{className:'miu-h4', name:'Heading 4', selectAssist:true, onAlt:true, key:'4', openWith:'#### ', placeHolder:'Your title here...', multiline:true },
		{className:'miu-h5', name:'Heading 5', selectAssist:true, onAlt:true, key:'5', openWith:'##### ', placeHolder:'Your title here...', multiline:true },
		{className:'miu-h6', name:'Heading 6', selectAssist:true, onAlt:true, key:'6', openWith:'###### ', placeHolder:'Your title here...', multiline:true },
		{separator:'---------------' },		
		{className:'miu-b', name:'Bold', key:'B', openWith:'**', closeWith:'**', multiline:true},
		{className:'miu-i', name:'Italic', key:'I', openWith:'_', closeWith:'_', multiline:true},
		{className:'miu-s', name:'Strike through', key:'S', openWith:'{{[st] ', closeWith:' }}', multiline:true},
		{className:'miu-u', name:'Underline', key:'U', openWith:'{{[usb] ', closeWith:' }}', multiline:true},
		{separator:'---------------' },
		{className:'miu-ul', name:'Bulleted List', selectAssist:true, onShift:true, key:'B', openWith:'- ', multiline:true },
		{className:'miu-ol', name:'Numeric List', selectAssist:true, onShift: true, key:'N', multiline:true, openWith:'1. ' },
		{separator:'---------------' },
		{className:'miu-img', name:'Picture', onShift:true, key:'P', openWith:'![]([![Url:!:http://]!] "', closeWith:'")'},
		{className:'miu-link', name:'Link', onShift:true, key:'L', openWith:'[', closeWith:']([![Url:!:http://]!] "")', placeHolder:'Your text to link here...' },
		{className:'miu-frg-link', name:'Fragment Link', onShift:true, key:'F', openWith:'{{[frgm][![Fragment ID]!] ', closeWith:' }}', placeHolder:'Your text to link here...'},
		{separator:'---------------'},	
		{className:'miu-quotes', name:'Quotes', selectAssist:true, onShift:true, key:'Q', openWith:'> ', closeWith:'  ', multiline:true},
        {className:'miu-code', name:'Code', onShift:true, key:'C', multiline:true, openWith:'`', closeWith:'`'},
        {className:'miu-html', name:'HTML', selectAssist:true, onShift:true, key:'H', openBlockWith:'{{{[html]\n\n', closeBlockWith:'\n\n}}}'},
        {className:'miu-fold', name:'Fold', selectAssist:true, onShift:true, key:'End', openBlockWith:'{{{[fold{"title":"`[![FoldTitle:!:...]!]`","hide":"true"}]\n\n', closeBlockWith:'\n\n}}}'},
		{separator:'---------------'},
		{className:'miu-indent', name:'Indent (Move Right)', selectAssist:true, onAlt:true, key:'.', indent:1, multiline:true},
		{className:'miu-outdent', name:'Outdent (Move Left)', selectAssist:true, onAlt:true, key:',', outdent:1, multiline:true},
		{name:'Colors', dropMenu:[
		    {className:'miu-clr-c', name:'Cyan', openBlockWith:'{{[clr-c] ', closeBlockWith:' }}'},
		    {className:'miu-clr-b', name:'Blue', openBlockWith:'{{[clr-b] ', closeBlockWith:' }}'},
		    {className:'miu-clr-r', name:'Red', openBlockWith:'{{[clr-r] ', closeBlockWith:' }}'},
		    {className:'miu-clr-g', name:'Green', openBlockWith:'{{[clr-g] ', closeBlockWith:' }}'},
		    {className:'miu-clr-y', name:'Yellow', openBlockWith:'{{[clr-y] ', closeBlockWith:' }}'},
		    {className:'miu-clr-m', name:'Magenta', openBlockWith:'{{[clr-m] ', closeBlockWith:' }}'},
		    {className:'miu-clr-v', name:'Violet', openBlockWith:'{{[clr-v] ', closeBlockWith:' }}'},
		    {className:'miu-clr-br', name:'Brown', openBlockWith:'{{[clr-br] ', closeBlockWith:' }}'},
	    ]},
	]
}

// mIu nameSpace to avoid conflict.
miu = {
    markdownTitle : function(markItUp, ch) {
        var heading = '';
        var n = $.trim(markItUp.selection || markItUp.placeHolder).length;
        for (i = 0; i < n; i++) {
            heading += ch;
        }
        return '\n' + heading;
    }
}
