INSERT INTO FRAGMENT(FRAGMENT_ID, TITLE, CONTENT, CREATION_DATETIME, UPDATE_DATETIME) VALUES

(1, 'bold', STRINGDECODE('
    **bold** or __bold__
* * *
**bold** or __bold__
'), TIMESTAMP '2014-08-25 03:39:15.1', TIMESTAMP '2014-08-25 03:39:15.1')
,(2, 'Blockquotes', STRINGDECODE('
    > Email-style angle brackets
    > are used for blockquotes.
    > > And, they can be nested.

    > >
    > * You can quote a list.
    > * Etc.
* * *
> Email-style angle brackets
> are used for blockquotes.
> > And, they can be nested.

> >
> * You can quote a list.
> * Etc.
'), TIMESTAMP '2014-11-03 19:05:59.121', TIMESTAMP '2014-11-03 19:10:25.797')

,(3, 'Links - inline', STRINGDECODE('
    [text](http://url.com/ "Title (optional)")
* * *
H2 [Quickstart](http://www.h2database.com/html/quickstart.html "H2 Quickstart")
'), TIMESTAMP '2014-11-03 19:07:00.324', TIMESTAMP '2014-11-03 19:07:00.324')

,(4, 'Images - inline', STRINGDECODE('
    ![alt text](/path/to/image "Title (optional)")
* * *
![batman](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTk-E0CVdOAiWPGhFwNQKUuTX-qZM2E0PQAebU0U45sl-6mt69h1w "Batman")
'), TIMESTAMP '2014-11-03 19:08:24.763', TIMESTAMP '2014-11-03 19:08:24.763')

,(5, 'Headers : Setext-style', STRINGDECODE('
    Header 1
    ========

    Header 2\r\n    --------
* * *
Header 1
========
\r\n
Header 2\r\n--------
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')

,(6, 'Trashed', STRINGDECODE('    This is a trashed fragment...'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')
,(7, 'Lists - ordered', STRINGDECODE('
    1.  Foo
    1.  Bar\r\n---
1.  Foo
1.  Bar
'), TIMESTAMP '2015-01-13 14:32:00.000', TIMESTAMP '2015-01-13 14:32:00.000')

,(8, 'Italic', STRINGDECODE('
    *italic* or _italic_
* * *
*italic* or _italic_
'), TIMESTAMP '2015-01-13 14:32:00.000', TIMESTAMP '2015-01-13 14:32:00.000')

,(9, 'Links - reference style', STRINGDECODE('
    H2 [Quickstart][id]. Then, anywhere else in the doc, define the link:

    [id]: http://www.h2database.com/html/quickstart.html  "H2 Quickstart"

* * *
H2 [Quickstart][id].\r\n\r\n
[id]: http://www.h2database.com/html/quickstart.html  "H2 Quickstart"
'), TIMESTAMP '2014-11-03 19:07:00.324', TIMESTAMP '2014-11-03 19:07:00.324')

,(10, 'Images - reference style', STRINGDECODE('
    ![alt text][id]  

    [id]: /path/to/image "Title"

* * *
![batman][id]\r\n\r\n
[id]: https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTk-E0CVdOAiWPGhFwNQKUuTX-qZM2E0PQAebU0U45sl-6mt69h1w "Batman"
'), TIMESTAMP '2014-11-03 19:08:24.763', TIMESTAMP '2014-11-03 19:08:24.763')

,(11, 'Headers : atx-style', STRINGDECODE('
    (closing # is optional):
    # Header 1 #
    ## Header 2
* * *
# Header 1
## Header 2
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')

,(12, 'Lists - unordered', STRINGDECODE('
    Unordered, with paragraphs: ("*" or "+" or "-" can be used)
    *   A list item.  
        With multiple paragraphs.
    *   Bar
* * *
*   A list item.  
    With multiple paragraphs.
*   Bar
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')

,(13, 'Lists - mixed & nested', STRINGDECODE('
    You can nest them:
    *   An unordered list item
        * A nested unordered list item
    *   Another list item
        0.  The first nested ordered list item
        0.  A second nested ordered list item
            * A nested unordered list
        0. This is the last nested ordered list item
    *   The last unordered list item
* * *
*   An unordered list item
    * A nested unordered list item
*   Another list item
    0.  The first nested ordered list item
    0.  A second nested ordered list item
        * A nested unordered list
    0. This is the last nested ordered list item
*   The last unordered list item
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')

,(14, 'Code span', STRINGDECODE('
    `<code>` spans are delimited by backticks. ( You can include literal backticks like `` `this` ``. )
    Use the `printf()` function.
* * *
Use the `printf()` function.
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')

,(15, 'Etc. markdown syntax', STRINGDECODE('
### Preformatted Code Blocks
    Indent every line of a code block by at least 4 spaces or 1 tab

### Horizontal Rules
    Three or more dashes or asterisks:\r\n    ---
    * * *
    - - - -
### Manual Line Breaks
    End a line with two or more spaces:
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')
,(16, 'Trashed 2', STRINGDECODE('    This is another trashed fragment...'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2015-02-06 12:32:00.000')
,(17, 'Contexts and Dependency Injection (CDI)', STRINGDECODE('
One of the most important features in Java EE is **Contexts and Dependency Injection (CDI)**.  
CDI helps bind the web tier and the business logic or transactional tier of the Java EE platform together.  
CDI is architected from two methodologies: contexts and dependency injection.
\r\n
- **Contexts**
 - provide the ability to bind the life cycle and interactions of stateful components to well-defined but extensive contexts, 
- **dependency injection** 
 - is defined as the ability to inject components into an application in a type-safe way, including the ability to choose at deployment time which implementation of a particular interface to inject.
\r\n
To make use of CDI, a developer should become familiar with a series of annotations that can be used to decorate objects and injected components.  
Since **CDI provides a high level of loose coupling**, it is an important piece of any Java enterprise application.  
Those applications that make use of CDI in the right way can become very efficient because CDI provides a
decoupling of resources, as well as strong typing, by eliminating the requirement to use String-based names for
managed resources and by using declarative Java annotations to specify just about everything.  
Although it is possible to develop Java EE applications without the use of CDI, it is very easy to use and enables enterprise applications to
become more robust and efficient than those that do not use CDI features.

'), TIMESTAMP '2015-02-28 22:04:00.000', TIMESTAMP '2015-02-28 22:04:00.000')
,(18, 'inline HTML', STRINGDECODE('<b>inline</b> html test'), TIMESTAMP '2015-03-12 20:22:00.000', TIMESTAMP '2015-03-12 20:22:00.000')
,(19, 'Github flavor Table', STRINGDECODE('
    | Left align | Right align | Center align |
    |:-----------|------------:|:------------:|
    | This       |        This |     This     
    | column     |      column |    column    
    | will       |        will |     will     
    | be         |          be |      be      
    | left       |       right |    center    
    | aligned    |     aligned |   aligned

* * *
| Left align | Right align | Center align |
|:-----------|------------:|:------------:|
| This       |        This |     This     
| column     |      column |    column    
| will       |        will |     will     
| be         |          be |      be      
| left       |       right |    center    
| aligned    |     aligned |   aligned
'), TIMESTAMP '2015-04-14 11:11:00.000', TIMESTAMP '2015-04-14 11:11:00.000')
,(20, 'Simple Table', STRINGDECODE('
    First Header  | Second Header
    ------------- | -------------
    Content Cell  | Content Cell
    Content Cell  | Content Cell
* * *
First Header  | Second Header\r\n------------- | -------------
Content Cell  | Content Cell
Content Cell  | Content Cell
'), TIMESTAMP '2015-04-14 11:11:00.000', TIMESTAMP '2015-04-14 11:11:00.000')
,(21, 'Embedding Youtube videos', STRINGDECODE('
https://www.youtube.com/watch?v=0omObKmJd4E&feature=youtu.be
\r\n
<iframe width="560" height="315" src="https://www.youtube.com/embed/0omObKmJd4E" frameborder="0" allowfullscreen>Civilizer - Tool to efficiently manage your data/knowledge/idea</iframe>
\r\n
https://youtu.be/0omObKmJd4E'
), TIMESTAMP '2015-12-22 10:55:00.000', TIMESTAMP '2015-12-22 10:59:00.000')
;

INSERT INTO TAG(TAG_ID, TAG_NAME) VALUES
(1, 'emphasis')
,(2, 'blockquotes')
,(3, 'links')
,(4, 'images')
,(5, 'headers')
,(6, 'lists')
,(7, 'code spans')
,(8, 'empty')
,(11, 'inline')
,(12, 'reference')
,(13, 'etc')
,(14, 'table')
,(15, 'multimedia')
,(16, 'videos')
,(100, 'markdown')
;

INSERT INTO FILE(FILE_NAME) VALUES
('/empty.txt')
,('/folder 0/empty.txt')
,('/folder 0/foo.txt')
,('/folder 0/folder 2/empty.txt')
,('/folder 1/bar.txt')
;

INSERT INTO TAG2TAG(PARENT_ID, CHILD_ID) VALUES
(100, 1)
,(100, 2)
,(100, 3)
,(100, 4)
,(100, 5)
,(100, 6)
,(100, 7)
,(100, 13)
,(100, 14)
,(13, 8)
,(12, 4)
,(15, 4)
,(15, 16)
;

INSERT INTO TAG2FRAGMENT(TAG_ID, FRAGMENT_ID) VALUES
(1, 1)
,(2, 2)
,(3, 3)
,(4, 4)
,(5, 5)
,(0, 6)
,(6, 7)
,(1, 8)
,(11, 3)
,(11, 4)
,(3, 9)
,(4, 10)
,(12, 9)
,(12, 10)
,(5, 11)
,(6, 12)
,(6, 13)
,(7, 14)
,(13, 15)
,(13, 6)
,(0, 16)
,(-1, 10)
,(-1, 5)
,(13, 17)
,(13, 18)
,(14, 19)
,(14, 20)
,(16, 21)
;

INSERT INTO FRAGMENT2FRAGMENT(FROM_ID, TO_ID) VALUES
(1, 2)
,(2, 1)
,(2, 3)
,(3, 2)
,(2, 4)
,(4, 2)
;

