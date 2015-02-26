INSERT INTO GLOBAL_SETTING(SETTING_NAME, SETTING_VALUE) VALUES
('database.version', '1')
;

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

        Header 2
        --------
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
    Three or more dashes or asterisks:
    ---
    * * *
    - - - -
### Manual Line Breaks
    End a line with two or more spaces:
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752')
,(16, 'Trashed 2', STRINGDECODE('    This is another trashed fragment...'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2015-02-06 12:32:00.000')
;

INSERT INTO TAG(TAG_ID, TAG_NAME, CREATION_DATETIME, UPDATE_DATETIME) VALUES
(0, '#trash', TIMESTAMP '2015-01-13 14:23:00.000', TIMESTAMP '2015-01-13 14:23:00.000')
,(-1, '#bookmark', TIMESTAMP '2015-01-13 14:23:00.000', TIMESTAMP '2015-01-13 14:23:00.000')
,(1, 'emphasis', TIMESTAMP '2014-08-25 03:39:15.106', TIMESTAMP '2014-08-25 03:39:15.106')
,(2, 'blockquotes', TIMESTAMP '2014-11-03 19:05:59.134', TIMESTAMP '2014-11-03 19:05:59.134')
,(3, 'links', TIMESTAMP '2014-11-03 19:07:00.33', TIMESTAMP '2014-11-03 19:09:38.441')
,(4, 'images', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(5, 'headers', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(6, 'lists', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(7, 'code spans', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(8, 'empty', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(11, 'inline', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(12, 'reference', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(13, 'etc', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
,(100, 'markdown', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767')
;

INSERT INTO TAG2TAG(TAG2TAG_ID, PARENT_ID, CHILD_ID) VALUES
(1, 100, 1)
,(2, 100, 2)
,(3, 100, 3)
,(4, 100, 4)
,(5, 100, 5)
,(6, 100, 6)
,(7, 100, 7)
,(11, 100, 13)
;

INSERT INTO TAG2FRAGMENT(TAG2FRAGMENT_ID, TAG_ID, FRAGMENT_ID) VALUES
(1, 1, 1)
,(2, 2, 2)
,(3, 3, 3)
,(4, 4, 4)
,(5, 5, 5)
,(6, 0, 6)
,(7, 6, 7)
,(8, 1, 8)
,(9, 11, 3)
,(10, 11, 4)
,(11, 3, 9)
,(12, 4, 10)
,(13, 12, 9)
,(14, 12, 10)
,(15, 5, 11)
,(16, 6, 12)
,(17, 6, 13)
,(18, 7, 14)
,(19, 13, 15)
,(20, 13, 6)
,(21, 0, 16)
,(22, -1, 10)
,(23, -1, 5)
;

INSERT INTO FRAGMENT2FRAGMENT(FRAGMENT2FRAGMENT_ID, FROM_ID, TO_ID) VALUES
(1, 1, 2)
,(2, 2, 1)
,(3, 2, 4)
;

