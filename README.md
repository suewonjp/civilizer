### Description

Civilizer is a **personal** web application that helps you efficiently manage your data/knowledge/idea including:

- knowledge/expertise for your job
- temporary memo/notesa
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

![civilizer](http://s6.postimg.org/ljrursgg1/civilizer.png)

### Credits 

> Civilizer has been inspired by Piggydb (http://piggydb.net/) developed by Daisuke Morita.  

### Related Materials
- Civilizer Tutorial Video
  - https://www.youtube.com/watch?v=0omObKmJd4E 
- Civilizer Twitter
  - [@civilizer_pkm](https://twitter.com/civilizer_pkm)
- Civilizer Blog 
  - https://sourceforge.net/p/civilizer/blog/
- Civilizer Wiki and online Docummentations
  - https://github.com/suewonjp/civilizer/wiki

* * *
### Prerequisites

Java Runtime Environment (JRE)
- Note that JRE 7 or above is needed
- The latest JRE is recommended

Supported platforms:

- Linux
    - Ubuntu/Linux Mint
    - Other Distros (? - will work, not fully tested though)
- OS X
    - Mavericks
    - Yosemite or above (? - will work, not fully tested though)
- Windows
    - 7/8
    - 10 or above (? - will work, not fully tested though)

Supported browsers: (note that old versions may not work correctly)

- Firefox
- Safari
- Chrome
- Opera
- Edge
- Internet Explorer
    - 9/10/11
    - ***Civilizer doesn't support IE 8 or earlier versions of IE***
    
### How To Install and Run
    
1. ***Make sure your system has JRE (version 7+) installed on it.***
    - Type and run _**java -version**_ from the command prompt to confirm your system JRE version.
1. Download a .zip file of [the latest release](https://github.com/suewonjp/civilizer/releases/latest) and extract its content on your machine.
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
    - With the loading finished, the icon color becomes white (azure color in a more correct term), and Civilizer can be accessed via your browser.
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
1. The pages you'll see after accessing Civilizer is basically its ***User Manual***.
    - The manual also has a role of exemplary data from which you can get some idea of how to create or edit your own data.
    - For now, no online manual is provided.
    
> For Advanced Users
> - Simply, run the following command from your shell at the root of the installation directory.
>   - _java -jar extra/lib/jetty-runner.jar --config jetty.xml --port 8080 --path /civilizer civilizer_
>   - This will run Civilizer with all it needs at the minimum.
>   - No tray icon or no other overhead.

### How To Uninstall

1. Just remove the extracted folder.
1. Civilizer automatically creates a folder named ***.civilizer*** in your User directory. 
    - So remove it unless you need it.
    - Note that this folder may contain your data.
        - In case you want to make a backup of your data, refer to the user manual about how to do that.
   
* * *
### Compile/Test
If you want to compile/test Civilizer on your own, you need the following as a minimum:

- JDK 1.7 or above
- Maven (https://maven.apache.org/)
- Eclipse IDE (Optional)
    - Spring Tool Suite (https://spring.io/tools) is recommended.
    - Do the following before importing the project into Eclipse IDE.  
        1. Install the latest _Maven integration in Eclipse (m2e)_ plugin.
        1. Run _mvn clean package_ command.

Using Maven, you can execute commands as follow at the root directory of the source package:

- To compile the binaries and perform unit tests:  
    - mvn test
- To build a .WAR file:
    - mvn clean package
        - Alternatively, _mvn clean package -Dmaven.test.skip=true_ to skip unit tests.
    - The output WAR can be found as _{root directory}/target/civilizer.{version-number...}.war_ file.
    - You can deploy and run the .WAR file on the Web Servers (technically, [Servlet Containers](https://en.wikipedia.org/wiki/Web_container)) such as:
        - Tomcat (http://tomcat.apache.org)
        - Jetty (http://www.eclipse.org/jetty/) or Jetty Runner (http://eclipse.org/jetty/documentation/current/runner.html)
- To compile the binary and load the application onto a Jetty server on the fly:  
    - mvn jetty:run
    
Shell scripts have been provided to build all the binaries/resources and package them all:

- Windows Users:
    - Run ***tools/build/build-all.bat*** from the command line.
    - Alternatively,
        - install Babun (http://babun.github.io)
          - It is a shell application running on top of Cygwin.
        - Run ***tools/build/build-all.sh*** from the command line powered by Babun.
- Other OS Users:
    - Run ***tools/build/build-all.sh*** from the command line.

* * *
### Limitations

- Civilizer is currently in **BETA** status.
- Layout may not be rendered correctly on some untested browsers.
- Localization is yet to be done.
- Civilizer is currently provided as a **PRIVATE EDITION** only.
    - Note that the main goal of the PRIVATE edition is helping A SINGLE USER manage his/her PRIVATE PERSONAL DATA under some relatively SECURED PRIVATE NETWORK ENVIRONMENT (e.g. home network).
    - The edition does not support access by multiple users.
    - We hope that sometime in the future, we will be able to develop a **Group or Enterprise Edition** to help a group of users share/manage data under greatly enhanced security provided.

* * *
### Copyright/License/Disclaimer

    Copyright (c) 2014-2015 Suewon Bahng, suewonjp@gmail.com
    
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
Written by Suewon Bahng   ( Last Updated 2 November, 2015 )

### Contributors
Suewon Bahng  

Other contributors are welcome!
