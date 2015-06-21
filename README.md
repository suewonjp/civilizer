### Introduction

Civilizer is a web application that helps you efficiently manage your data/knowledge/idea including:
- knowledge/expertise for your job
- personal memo/notes
- schedule
- detailed guidelines/procedures you often forget
- expressions/vocabularies (e.g. when you learn a foreign language)
- ideas/inspirations
- todo list

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

Civilizer has a full text search functionality to help you access right data at the right time

> [NOTICE] Civilizer has been inspired by Piggydb (http://piggydb.net/) developed by Daisuke Morita.    
> Basic concepts have been coming from Piggydb and it was a implementation reference for Civilizer.  
> Nevertheless, what to focus and design/implementation details differ. 
 
* * *
### Installation

You need to install the following softwares on your system prior to running Civilizer
- Java Runtime Environment (JRE)
    - note that JRE 7 or above is needed
- Java Servlet containers (web servers based on Java Servlet technology)
    - Tomcat (http://tomcat.apache.org)
    - Jetty (http://www.eclipse.org/jetty/)
    - Jetty Runner (http://eclipse.org/jetty/documentation/current/runner.html)

As of now, the most simplest way is using Jetty Runner:
1. First, make sure you have installed JRE 7 or above on your system
1. Download a copy of Jetty Runner
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
            - Read limitations section carefully before you run Civilizer from another machine over a network.
1. When you first access the URL, the application will guide you to its authentication page.
    - Initial credential is as follows:
        - username:owner
        - password:owner
1. Then, you can reach the home page of the application.
    - You may change username or password by accessing the user menu located at the top-right corner of the home page

Supported browsers: (note that old versions may not work)
- Firefox
- Safari
- Chrome
- Opera
- Internet Explorer
    - 11
    - 8/9/10 (? - not tested yet)

Supported OS:
- Linux
    - Ubuntu/Linux Mint
    - Other Distros (? - not tested yet)
- OS X
    - Mavericks
    - Yosemite (? - not tested yet)
- Windows
    - 7/8
    - 10 (? - not tested yet)
* * *
### Limitations

- Civilizer is currently in ALPHA status.
- Layout may not be rendered correctly by some untested browsers.
    - e.g. IE 8/9/10
- Civilizer is currently provided as a PRIVATE EDITION only.
    - The edition does not support access by multiple users.
    - DONT'T apply neither of the following usage with a PRIVATE edition of Civilizer:
        - Install and run the application on a public domain machine where anonymous internet users can access.
        - Use the application over a public WIFI network or other insecure networks.
    - Note that this doesn't have to do with any licensing/charging conditions.
        - The private edition is free to use for whatever usages.
        - This concern is merely based on SECURITY considerations.
    - The main goal of the PRIVATE edition is helping manage PRIVATE PERSONAL DATA under some relatively SECURED PRIVATE NETWORK ENVIRONMENT (e.g. home network).
        - In that sense, the private edition has limited security features only and future versions also might remain the same.
        - We hope that sometime in the future, we will be able to develop a Group or Enterprise edition to help many users share/manage data under greatly enhanced security provided.
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