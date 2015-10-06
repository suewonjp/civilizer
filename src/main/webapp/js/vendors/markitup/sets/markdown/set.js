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
		{name:'First Level Heading', selectAssist:true, onAlt:true, key:'1', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '=') } },
		{name:'Second Level Heading', selectAssist:true, onAlt:true, key:'2', placeHolder:'Your title here...', multiline:true, closeWith:function(markItUp) { return miu.markdownTitle(markItUp, '-') } },
		{name:'Heading 3', selectAssist:true, onAlt:true, key:'3', openWith:'### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 4', selectAssist:true, onAlt:true, key:'4', openWith:'#### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 5', selectAssist:true, onAlt:true, key:'5', openWith:'##### ', placeHolder:'Your title here...', multiline:true },
		{name:'Heading 6', selectAssist:true, onAlt:true, key:'6', openWith:'###### ', placeHolder:'Your title here...', multiline:true },
		{separator:'---------------' },		
		{name:'Bold', key:'B', openWith:'**', closeWith:'**', multiline:true},
		{name:'Italic', key:'I', openWith:'_', closeWith:'_', multiline:true},
		{name:'Strike through', key:'S', openWith:'{{[st] ', closeWith:' }}', multiline:true},
		{name:'Underline', key:'U', openWith:'{{[usb] ', closeWith:' }}', multiline:true, faIcon:'fa-underline'},
		{separator:'---------------' },
		{name:'Bulleted List', selectAssist:true, onShift:true, key:'B', openWith:'- ', multiline:true },
		{name:'Numeric List', selectAssist:true, onShift: true, key:'N', multiline:true, openWith:'1. ' },
		{separator:'---------------' },
		{name:'Picture', onShift:true, key:'P', openWith:'![]([![Url:!:http://]!] "', closeWith:'")'},
		{name:'Link', onShift:true, key:'L', openWith:'[', closeWith:']([![Url:!:http://]!])', placeHolder:'Your text to link here...' },
		{name:'Fragment Link', onShift:true, key:'F', openWith:'{{[frgm][![Fragment ID]!] ', closeWith:' }}', placeHolder:'Your text to link here...', faIcon:'fa-link fa-lg' },
		{separator:'---------------'},	
		{name:'Quotes', selectAssist:true, onShift:true, key:'Q', openWith:'> ', closeWith:'  ', multiline:true},
        {name:'Code', onShift:true, key:'C', multiline:true, openWith:'`', closeWith:'`'},
        {name:'HTML', selectAssist:true, onShift:true, key:'H', openBlockWith:'{{{[html]\n\n', closeBlockWith:'\n\n}}}', faIcon:'fa-code fa-lg'},
        {name:'Fold', selectAssist:true, onShift:true, key:'End', openBlockWith:'{{{[fold{"title":"[![Title:!:...]!]","hide":"true"}]\n\n', closeBlockWith:'\n\n}}}', faIcon:'fa-plus-square-o fa-lg'},
		{separator:'---------------'},
		{name:'Indent (Move Right)', selectAssist:true, onAlt:true, key:'.', indent:1, multiline:true, faIcon:'fa-indent fa-lg'},
		{name:'Outdent (Move Left)', selectAssist:true, onAlt:true, key:',', outdent:1, multiline:true, faIcon:'fa-outdent fa-lg'},
			{name:'Colors', dropMenu:[
			    {name:'Cyan', openBlockWith:'{{[clr-c] ', closeBlockWith:' }}'},
			    {name:'Blue', openBlockWith:'{{[clr-b] ', closeBlockWith:' }}'},
			    {name:'Red', openBlockWith:'{{[clr-r] ', closeBlockWith:' }}'},
			    {name:'Green', openBlockWith:'{{[clr-g] ', closeBlockWith:' }}'},
			    {name:'Yellow', openBlockWith:'{{[clr-y] ', closeBlockWith:' }}'},
			    {name:'Magenta', openBlockWith:'{{[clr-m] ', closeBlockWith:' }}'},
			    {name:'Violet', openBlockWith:'{{[clr-v] ', closeBlockWith:' }}'},
			    {name:'Brown', openBlockWith:'{{[clr-br] ', closeBlockWith:' }}'},
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
