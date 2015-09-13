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
	onTab:    		    {keepDefault:false, multiline:true, indent:4},
	onShiftTab:    		{keepDefault:false, multiline:true, outdent:4},
	markupSet: [
		{name:'First Level Heading', onAlt:true, key:'1', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '=') } },
		{name:'Second Level Heading', onAlt:true, key:'2', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '-') } },
		{name:'Heading 3', onAlt:true, key:'3', openWith:'### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 4', onAlt:true, key:'4', openWith:'#### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 5', onAlt:true, key:'5', openWith:'##### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 6', onAlt:true, key:'6', openWith:'###### ', placeHolder:'Your title here...', multiline:true },
		{separator:'---------------' },		
		{name:'Bold', key:'B', openWith:'**', closeWith:'**', multiline:true},
		{name:'Italic', key:'I', openWith:'_', closeWith:'_', multiline:true},
		{name:'Strike through', key:'S', openWith:'{{[st] ', closeWith:' }}', multiline:true},
		{name:'Underline', key:'U', openWith:'{{[usb] ', closeWith:' }}', multiline:true, faIcon:'fa-underline'},
		{separator:'---------------' },
		{name:'Bulleted List', onShift:true, key:'B', openWith:'- ', multiline:true },
		{name:'Numeric List', onShift: true, key:'N', multiline:true, openWith:'1. ' },
		{separator:'---------------' },
		{name:'Picture', onShift:true, key:'P', replaceWith:'![]([![Url:!:http://]!])'},
		{name:'Link', onShift:true, key:'L', openWith:'[', closeWith:']([![Url:!:http://]!])', placeHolder:'Your text to link here...' },
		{name:'Fragment Link', onShift:true, key:'F', openWith:'{{[frgm][![Fragment ID]!] ', closeWith:' }}', placeHolder:'Your text to link here...', faIcon:'fa-link fa-lg' },
		{separator:'---------------'},	
		{name:'Quotes', onShift:true, key:'Q', openWith:'> ', closeWith:'  ', multiline:true},
        {name:'Code', onShift:true, key:'C', multiline:true, openWith:'`', closeWith:'`'},
        {name:'HTML', onShift:true, key:'H', openBlockWith:'{{{[html]\n', closeBlockWith:'\n}}}', faIcon:'fa-code fa-lg'},
		{separator:'---------------'},
		{name:'Indent (Move Right)', onShift:true, key:'Right', indent:1, multiline:true, faIcon:'fa-indent fa-lg'},
		{name:'Outdent (Move Left)', onShift:true, key:'Left', outdent:1, multiline:true, faIcon:'fa-outdent fa-lg'},
			{name:'Colors', dropMenu:[
			    {name:'Cyan', multiline:true, openWith:'{{[clr-c] ', closeWith:' }}'},
			    {name:'Blue', multiline:true, openWith:'{{[clr-b] ', closeWith:' }}'},
			    {name:'Red', multiline:true, openWith:'{{[clr-r] ', closeWith:' }}'},
			    {name:'Green', multiline:true, openWith:'{{[clr-g] ', closeWith:' }}'},
			    {name:'Yellow', multiline:true, openWith:'{{[clr-y] ', closeWith:' }}'},
			    {name:'Magenta', multiline:true, openWith:'{{[clr-m] ', closeWith:' }}'},
			    {name:'Violet', multiline:true, openWith:'{{[clr-v] ', closeWith:' }}'},
			    {name:'Brown', multiline:true, openWith:'{{[clr-br] ', closeWith:' }}'},
		    ]},
	]
}

// mIu nameSpace to avoid conflict.
miu = {
	markdownTitle: function(markItUp, char) {
		heading = '';
		n = $.trim(markItUp.selection||markItUp.placeHolder).length;
		for(i = 0; i < n; i++) {
			heading += char;
		}
		return '\n'+heading;
	}
}
