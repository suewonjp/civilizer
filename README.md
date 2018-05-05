- [civilizer official page](http://suewonjp.github.io/civilizer/)
- [civilizer github page](https://github.com/suewonjp/civilizer)
- [civilizer sourceforge page](https://sourceforge.net/projects/civilizer/)
- [civilizer twitter - @civilizer_pkm](https://twitter.com/civilizer_pkm)
- [civilizer blog](http://suewonjp.github.io/civilizer/blog/)
- [civilizer tutorial video](https://www.youtube.com/watch?v=0omObKmJd4E&feature=youtu.be)
- [civilizer online manual/docummentations](https://github.com/suewonjp/civilizer/wiki)
- [feature request & bug report & other issues](https://github.com/suewonjp/civilizer/issues)
- [シビライザー日本語版Githubページ](https://github.com/suewonjp/civilizer-jpdoc)
- [シビライザー日本語版ドキュメント](https://github.com/suewonjp/civilizer-jpdoc/wiki)

### :coffee: DESCRIPTION

**Civilizer** is a web application for **Notetaking or Personal Knowledge Management (PKM)** 

- knowledge/expertise/tips/tricks for your job
- temporary memo/notes
- schedules/plans
- detailed procedures you often forget for infrequent tasks
  - e.g., installing printer drivers on your computers, submitting a tax return, ...
- expressions/vocabularies (e.g. when you learn a foreign language)
- ideas/inspirations
- todo list for your job or life
- insightful maxims/proverbs/humors
- and whatever knowledge/information you may want to access later.

Civilizer looks like a notetaking application like Evernote.  
But unlike a native desktop application, it runs on your browser, so more Web-Friendly.  

### :coffee: DESCUSSION
Civilizer can keep your data in various forms including:

- text
    - can be authored in [Markdown](https://en.wikipedia.org/wiki/Markdown) with a dedicated editor
- hyper links
- images
- videos
- and other file types

It can help you efficiently organize/structure your data by:

- tagging your data
- relating data with one another
- bookmarking data you access frequently
- organizing your files in a directory structure

**Civilizer has a Full Text Search functionality** to greatly help you access right data at the right time

**See [ screenshots ](http://suewonjp.github.io/civilizer/#screenshots) or [tutorial video](http://suewonjp.github.io/civilizer/#videos)**

* * *

### :coffee: PREREQUISITES

Java Runtime Environment (JRE)

- JRE 7 or later

Supported platforms:

- Linux
- OS X
    - Mavericks or later
- Windows
    - 7/8/10 or later

Supported browsers: (note that old versions may not work correctly)

- Firefox
- Safari
- Chrome
- Opera
- Edge
- Internet Explorer
    - 9/10/11 ( 9 or 10 may not render some styles correctly )
    - ***Doesn't support IE 8***
    
### :coffee: HOW TO INSTALL AND RUN
    
1. ***Make sure your system has JRE (version 7+) installed prior to launching the app.***
    - Type and run `java -version` from the command prompt to confirm your system JRE version.
1. Download [the latest release of Civilizer](http://suewonjp.github.io/civilizer/#services) and extract its content on your machine.
1. Run it with the following instruction.
    - Windows Users:
        - Just execute `civilizer-win32.exe`.
        - Alternatively, execute `run-civilizer.bat` from the command line.
    - Other OS Users:
        - Execute `run-civilizer.sh` from your shell command line.
1. You'll see a ***System Tray Icon*** appearing **unless you run it on Linux systems**.
    - ***[For Linux Users] Currently, system tray icon is not supported on Linux because underlying systems (OS or JVM or both) apparently have technical problems in supporting Java's system tray icon feature.***
    - The icon will stay in red during the loading process.
    - It'll take a while to complete the loading.
    - With the loading finished, the icon color changes, and Civilizer can be accessed via your browser.
1. Access Civilizer. 
    - If everything has gone well, your default browser will automatically launch or be focused on most of major systems.
    - If your browser doesn't respond, access Civilizer manually.
        - Right-click on Civilizer's system tray icon (maybe left-click depending on your OS), and choose the menu saying ***`Browse`***.
        - Alternatively, manually access the following URL with your browser.
            - ***`http://localhost:8080/civilizer/app/home`***
1. When you first access Civilizer, the application will guide you to its authentication page.
    - Initial credential is as follows:
        - _username:owner_
        - _password:owner_
    - After authenticated, you can reach the default page of the application.
        - Click on the man icon located at the top-right corner of the page. 
        - Change username or password by accessing ***'Change Profile'*** menu.
    
### :coffee: HOW TO UNINSTALL

1. Just remove the extracted folder.
1. Remove the  `.civilizer` folder (containing user settings and data) in your **user directory** if it exists.
   
### :coffee: HOW TO BUILD

Refer to [this page](https://github.com/suewonjp/civilizer/wiki/Building-Civilizer)

* * *
### :coffee: OTHER PROJECTS FROM THE AUTHOR

- [Mac OS X application enhancing your Shortcut/Hotkey usability](https://github.com/suewonjp/Hooky)
- [Javascript library enhancing Drag & Drop User Inferface interactions](https://github.com/suewonjp/DNDX.JS)
- [Bash utility helping quickly Search arbitrary Files or Search Text from Files](https://github.com/suewonjp/lf.sh)
- [Vim Plugin for quickly Jumping around your content](https://github.com/suewonjp/vim-jumparound)

### :coffee: CREDITS 

Civilizer has been inspired by [Piggydb](http://piggydb.net/) developed by Daisuke Morita.  


### :copyright: COPYRIGHT/LICENSE/DISCLAIMER

    Copyright (c) 2014-2018 Suewon Bahng, suewonjp@gmail.com
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

### :busts_in_silhouette: CONTRIBUTORS
Suewon Bahng  

Other contributors are welcome!

* * *
Updated by Suewon Bahng ( May 2018 )

