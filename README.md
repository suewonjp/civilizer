### Description

Civilizer is a web application that helps you efficiently manage your data/knowledge/idea including:

- knowledge/expertise for your job
- temporary memo/notes
- schedule/plan
- detailed procedures you often forget for infrequent tasks
  - e.g. installing printer drivers on your computers, submitting a tax return
- expressions/vocabularies (e.g. when you learn a foreign language)
- ideas/inspirations
- todo list for your job or life
- insightful maxims/proverbs
- humors and etc.

Civilizer can keep your data in various forms including:

- text
    - authored in Markdown with a dedicated editor
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
> Basic concepts and design have been borrowed from Piggydb and it used to be a sample implementation for Civilizer.  

* * *
### Installation

Supported platforms:

- Linux
    - Ubuntu/Linux Mint
    - Other Distros (? - not tested yet)
- OS X
    - Mavericks
    - Yosemite (? - not tested yet)
- Windows
    - 7/8
    - 10 (? - not tested yet)
    
You need to install the following softwares on your system prior to running Civilizer

- Java Runtime Environment (JRE)
    - Note that JRE 7 or above is needed
- Java Servlet containers (web servers based on Java Servlet technology)
    - Tomcat (http://tomcat.apache.org)
    - Jetty (http://www.eclipse.org/jetty/)
    - Jetty Runner (http://eclipse.org/jetty/documentation/current/runner.html)

The most simplest way is using Jetty Runner:

1. First, make sure you have installed JRE 7 or above on your system
1. Download a latest copy of Jetty Runner
    - go to http://search.maven.org/#artifactdetails%7Corg.eclipse.jetty%7Cjetty-runner%7C9.3.0.v20150612%7Cjar
    - e.g. you may download jetty-runner-9.3.0.v20150612.jar ( as of June 21, 2015 )
1. Run Civilizer with the following command:
    - java -jar jetty-runner-x.x.x.jar --port 12345 --path /civilizer civilizer.war
        - x.x.x is a version number of Jetty Runner
        - You may assign another available port than 12345
1. Start your browser
1. Access Civilizer with the following URL:
    - http://localhost:12345/civilizer/app/home
        - If you have started Civilizer from another machine, you should specify the domain name or IP address of that machine other than localhost;
            - Read limitations section below carefully before you run Civilizer from another machine over a network.
1. When you first access the URL, the application will guide you to its authentication page.
    - Initial credential is as follows:
        - username:owner
        - password:owner
1. After authenticated, you can reach the home page of the application.
    - You may change username or password by accessing 'Change Profile' menu 
        - hover your mouse over the person icon located at the top-right corner of the home page

Supported browsers: (note that old versions may not work correctly)

- Firefox
- Safari
- Chrome
- Opera
- Internet Explorer
    - 9/10/11
    - ***Civilizer doesn't support IE 8 or earlier versions of IE***
    
* * *
### Compile/Test
If you want to compile/test Civilizer on your own, you need the followings:

- JDK 1.7 or above
- Maven (https://maven.apache.org/)
- Eclipse IDE (Optional)
    - Spring Tool Suite (https://spring.io/tools) is recommended for an IDE

Using Maven, you can execute commands as follows at the root directory of the source package:  

- To compile the binary and perform unit tests:  
        mvn test
- To build a package into .WAR file:
        mvn clean package
- To compile the binary and load the application onto a Jetty server on the fly:  
        mvn jetty:run
  - Access the application with URL http://localhost:8080/civilizer/app/home
  - Press Ctrl-C to stop the server

* * *
### Limitations

- Civilizer is currently in **ALPHA** status.
- Layout may not be rendered correctly by some untested browsers.
- Localization is yet to be done.
- Civilizer is currently provided as a **PRIVATE EDITION** only.
    - The edition does not support access by multiple users.
    - DONT'T apply neither of the following usages with a PRIVATE edition of Civilizer:
        - Install and run the application on a public domain machine where anonymous Internet users can access.
        - Use the application over a public WIFI network or other insecure networks.
    - The main goal of the PRIVATE edition is helping A SINGLE USER manage his/her PRIVATE PERSONAL DATA under some relatively SECURED PRIVATE NETWORK ENVIRONMENT (e.g. home network).
        - In that sense, the private edition has limited security features
        - future versions also might remain the same.
    - We hope that sometime in the future, we will be able to develop a **Group or Enterprise Edition** to help many users share/manage data under greatly enhanced security provided.
    
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
Written by Suewon Bahng   ( 21 June, 2015 )