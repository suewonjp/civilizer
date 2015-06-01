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
	onTab:    		    {keepDefault:false, multiline:true, openWith:'    '},
	onShiftTab:    		{keepDefault:false, multiline:true, swapFrom:/(^    )|(^\t)/, swapTo:'' },
	markupSet: [
		{name:'First Level Heading', key:'1', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '=') } },
		{name:'Second Level Heading', key:'2', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '-') } },
		{name:'Heading 3', key:'3', openWith:'### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 4', key:'4', openWith:'#### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 5', key:'5', openWith:'##### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 6', key:'6', openWith:'###### ', placeHolder:'Your title here...', multiline:true },
		{separator:'---------------' },		
		{name:'Bold', key:'B', openWith:'**', closeWith:'**', multiline:true},
		{name:'Italic', key:'I', openWith:'_', closeWith:'_', multiline:true},
		{name:'Strike through', key:'S', openWith:'{{[st]', closeWith:'}}', multiline:true},
		{name:'Underline', key:'U', openWith:'{{[usb]', closeWith:'}}', multiline:true, faIcon:'fa-underline'},
		{separator:'---------------' },
		{name:'Bulleted List', openWith:'- ', multiline:true },
		{name:'Numeric List', multiline:true, openWith:function(markItUp) {
			return markItUp.line+'. ';
		}},
		{separator:'---------------' },
		{name:'Picture', replaceWith:'![]([![Url:!:http://]!] "[![Title]!]")'},
//		{name:'Picture', replaceWith:'![[![Alternative text]!]]([![Url:!:http://]!] "[![Title]!]")'},
		{name:'Link', openWith:'[', closeWith:']([![Url:!:http://]!] "[![Title]!]")', placeHolder:'Your text to link here...' },
		{name:'Fragment Link', key:'F', openWith:'{{[frgm][![Fragment ID]!] ', closeWith:'}}', placeHolder:'Your text to link here...', faIcon:'fa-link fa-lg' },
		{separator:'---------------'},	
		{name:'Quotes', key:'Q', openWith:'> ', closeWith:'  ', multiline:true},
        {name:'Code', multiline:true, openWith:'`', closeWith:'`'},
//        {name:'Code Block / Code', multiline:true, openWith:'(!(\t|!|`)!)', closeWith:'(!(`)!)'},
		{separator:'---------------'},
		{name:'Indent (Move Right)', key:'R', openWith:' ', multiline:true, faIcon:'fa-indent fa-lg'},
		{name:'Dedent (Move Left)', key:'L', swapFrom:/^[ \t]/, swapTo:'', multiline:true, faIcon:'fa-dedent fa-lg'},
			{name:'Colors', dropMenu:[
			    {name:'Cyan', multiline:true, openWith:'{{[clr-c]', closeWith:'}}'},
			    {name:'Blue', multiline:true, openWith:'{{[clr-b]', closeWith:'}}'},
			    {name:'Red', multiline:true, openWith:'{{[clr-r]', closeWith:'}}'},
			    {name:'Green', multiline:true, openWith:'{{[clr-g]', closeWith:'}}'},
			    {name:'Yellow', multiline:true, openWith:'{{[clr-y]', closeWith:'}}'},
			    {name:'Magenta', multiline:true, openWith:'{{[clr-m]', closeWith:'}}'},
			    {name:'Violet', multiline:true, openWith:'{{[clr-v]', closeWith:'}}'},
			    {name:'Brown', multiline:true, openWith:'{{[clr-br]', closeWith:'}}'},
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
