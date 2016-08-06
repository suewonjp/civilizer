### DESCRIPTION

**Civilizer** is a web application that helps you efficiently manage your personal data/knowledge/idea including:

- knowledge/expertise/tips/tricks for your job
- temporary memo/notes
- schedule/plan
- detailed procedures you often forget for infrequent tasks
  - e.g., installing printer drivers on your computers, submitting a tax return, ...
- expressions/vocabularies (e.g. when you learn a foreign language)
- ideas/inspirations
- todo list for your job or life
- insightful maxims/proverbs/humors
- and whatever knowledge/information you may want to access later.

Civilizer can keep your data in various forms including:

- text
    - can be authored in [Markdown](https://en.wikipedia.org/wiki/Markdown) with a dedicated editor
    - eventually rendered in HTML
- hyper links
- images
- files

It can help you efficiently organize/structure your data by:

- tagging your data
- relating similar/contrary data with one another
- bookmarking data you access frequently
- organizing your files in a directory structure

Civilizer has a full text search functionality to greatly help you access right data at the right time

**See [ screenshots ](http://suewonjp.github.io/civilizer/#screenshots) or [tutorial video](http://suewonjp.github.io/civilizer/#videos)**

### CREDITS 

> Civilizer has been inspired by [Piggydb](http://piggydb.net/) developed by Daisuke Morita.  

### RELATED LINKS
- [civilizer official page](http://suewonjp.github.io/civilizer/)
- [civilizer github page](https://github.com/suewonjp/civilizer)
- [civilizer sourceforge page](https://sourceforge.net/projects/civilizer/)
- [civilizer twitter - @civilizer_pkm](https://twitter.com/civilizer_pkm)
- [civilizer blog](http://suewonjp.github.io/civilizer/blog/)
- [civilizer tutorial video](https://www.youtube.com/watch?v=0omObKmJd4E&feature=youtu.be)
- [civilizer online manual/docummentations](https://github.com/suewonjp/civilizer/wiki)
- [feature request & bug report & other issues](https://github.com/suewonjp/civilizer/issues)

* * *

### PREREQUISITES

Java Runtime Environment (JRE)

- JRE 7 or higher

Supported platforms:

- Linux
- OS X
    - Mavericks or higher
- Windows
    - 7/8/10 or higher

Supported browsers: (note that old versions may not work correctly)

- Firefox
- Safari
- Chrome
- Opera
- Edge
- Internet Explorer
    - 9/10/11 ( 9 or 10 may not render some styles correctly )
    - ***IE 8 won't work***
    
### HOW TO INSTALL AND RUN
    
1. ***Make sure your system has JRE (version 7+) installed on it.***
    - Type and run _**java -version**_ from the command prompt to confirm your system JRE version.
1. Download [the latest release](http://suewonjp.github.io/civilizer/#download) and extract its content on your machine.
1. Run it with the following instruction.
    - Windows Users:
        - Just click ***civilizer-win32.exe***.
        - Alternatively, execute ***run-civilizer.bat*** from the command line.
    - Other OS Users:
        - Execute ***run-civilizer.sh*** from your shell command line.
1. You'll see a ***System Tray Icon*** appearing **unless you run it on Linux systems**.
    - ***[For Linux Users] Currently, system tray icon is not supported on Linux because underlying systems (OS or JVM or both) apparently have technical problems in supporting Java's system tray icon feature.***
    - <span style="color:red">The icon will stay in red during the loading process.</span>
    - It'll take a while to complete the loading.
    - With the loading finished, the icon color changes, and Civilizer can be accessed via your browser.
1. Access Civilizer. 
    - If everything has gone well, your default browser will automatically launch or be focused on most of major systems.
    - If your browser doesn't respond, access Civilizer manually.
        - Right-click on Civilizer's system tray icon (maybe left-click depending on your OS), and choose the menu saying ***Browse***.
        - Alternatively, manually access the following URL with your browser.
            - ***`http://localhost:8080/civilizer/app/home`***
1. When you first access Civilizer, the application will guide you to its authentication page.
    - Initial credential is as follows:
        - _username:owner_
        - _password:owner_
    - After authenticated, you can reach the default page of the application.
        - You may change username or password by accessing ***'Change Profile'*** menu.
            - Click on the man icon located at the top-right corner of the page. 
1. The pages you'll see after accessing Civilizer is the table of content for its [User Manual](https://github.com/suewonjp/civilizer/wiki)
    
### HOW TO UNINSTALL

1. Just remove the extracted folder.
1. Remove the  ***.civilizer*** folder (containing user settings and data) in your _user directory_ if it exists.
   
* * *

### HOW TO BUILD

***In fact, building Civilizer from the latest source code is quite easy.***

1. Download the latest source code
     - Press ***Clone or download*** button above
1. Go to the source directory uncompressed or cloned
1. Execute the following command from the command line
    - Windows Users:
        - _gradlew.bat all_
    - Other OS Users:
        - _./gradlew all_
    - Refer to [this page](https://github.com/suewonjp/civilizer/wiki/Building-Civilizer) unless it works well.
1. And just wait. It might take more than several minutes especially for the first time to build
1. You can find the output package in the **build** directory
    - e.g., {uncompressed-folder}/build/civilizer.{version-notation}.zip

Refer to [this page](https://github.com/suewonjp/civilizer/wiki/Building-Civilizer) for more advanced topics about build or development.

* * *

### LIMITATIONS

- Layout may not be rendered correctly on some untested browsers.
- Localization is yet to be done.
- Civilizer is currently provided as a **PRIVATE EDITION** only.
    - Note that the main goal of the PRIVATE edition is helping A SINGLE USER manage his/her PRIVATE PERSONAL DATA under some relatively SECURED PRIVATE NETWORK ENVIRONMENT (e.g. home network).
    - The edition does not support access by multiple users.
    - Or don't care if you want to use Civilizer just like a _desktop app_. That usage is absolutely fine.
- We hope that sometime in the future, we will be able to develop a **Group or Enterprise Edition** to help a group of users share/manage data under greatly enhanced security provided.

* * *

### COPYRIGHT/LICENSE/DISCLAIMER

    Copyright (c) 2014-2016 Suewon Bahng, suewonjp@gmail.com
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

* * *
Written by Suewon Bahng   ( Last Updated 26 November, 2015 )

### CONTRIBUTORS
Suewon Bahng  

Other contributors are welcome!
