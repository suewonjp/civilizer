INSERT INTO PUBLIC.FRAGMENT(FRAGMENT_ID, TITLE, CONTENT, CREATION_DATETIME, UPDATE_DATETIME) VALUES
(1, 'markdown - Phrase Emphasis', STRINGDECODE('    **bold** or __bold__\r\n\r\n    *italic* or _italic_\r\n\r\n    ***bold_and_italic*** or ___bold_and_italic___ \r\n\r\n###### Output:\r\n\r\n**bold** or __bold__\r\n\r\n*italic* or _italic_\r\n\r\n***bold_and_italic*** or ___bold_and_italic___ '), TIMESTAMP '2015-06-13 16:37:54.137', TIMESTAMP '2015-06-13 16:47:20.312'),
(2, 'markdown - Headers', STRINGDECODE('    Big Header\r\n    =======\r\n    \r\n    Smaller Header \r\n    --------\r\n    \r\n    # Header 1\r\n    ## Header 2\r\n    ### Header 3\r\n    #### Header 4\r\n    ##### Header 5\r\n    ###### Header 6\r\n\r\n###### Output:\r\n\r\nBig Header\r\n=======\r\n\r\nSmaller Header \r\n--------\r\n\r\n# Header 1\r\n## Header 2\r\n### Header 3\r\n#### Header 4\r\n##### Header 5\r\n###### Header 6\r\n'), TIMESTAMP '2015-06-13 16:44:38.168', TIMESTAMP '2015-06-13 16:44:38.168'),
(3, 'markdown - Lists', STRINGDECODE('    Ordered, without paragraphs: \r\n        1.  Foo\r\n        1.  Bar\r\n\r\n    Unordered, with paragraphs: (''*'' or ''+'' or ''-'' can be used)\r\n        - Foo.  \r\n          With multiple paragraphs.\r\n        - Bar\r\n\r\n    You can nest them:\r\n        -   An unordered list item\r\n          - A nested unordered list item\r\n        -   Another list item\r\n            1.  The first nested ordered list item\r\n            1.  A second nested ordered list item\r\n                * A nested unordered list\r\n            1. This is the last nested ordered list item\r\n        -   The last unordered list item\r\n###### Output:\r\n\r\nOrdered, without paragraphs: \r\n1.  Foo\r\n1.  Bar\r\n\r\nUnordered, with paragraphs: (''*'' or ''+'' or ''-'' can be used)\r\n- Foo.  \r\n  With multiple paragraphs.\r\n- Bar\r\n\r\nYou can nest them:\r\n-   An unordered list item\r\n  - A nested unordered list item\r\n-   Another list item\r\n    1.  The first nested ordered list item\r\n    1.  A second nested ordered list item\r\n        * A nested unordered list\r\n    1. This is the last nested ordered list item\r\n-   The last unordered list item'), TIMESTAMP '2015-06-13 16:51:45.537', TIMESTAMP '2015-06-13 16:51:45.537'),
(4, 'markdown - Blockquotes', STRINGDECODE('    > Email-style angle brackets  \r\n    > are used for blockquotes.\r\n    > > And, they can be nested.\r\n    >\r\n    > * You can quote a list.\r\n    > * Etc.\r\n###### Output:\r\n\r\n> Email-style angle brackets  \r\n> are used for blockquotes.\r\n> > And, they can be nested.\r\n>\r\n> * You can quote a list.\r\n> * Etc.'), TIMESTAMP '2015-06-13 16:54:05.735', TIMESTAMP '2015-06-13 16:57:01.217'),
(5, 'markdown - Literal (preformatted) text', STRINGDECODE('    Text surrounded by backticks will be rendered literally:  \r\n    `*** not bold, just shows as it is ***`  \r\n    ( You can include literal backticks like `` `this` `` )\r\n\r\n    Alternatively, you can just indent every line by at least 4 spaces like so:\r\n\r\n        ##### This text will not be rendered as headers\r\n        ##### This one neither\r\n\r\n###### Output:\r\n\r\nText surrounded by backticks will be rendered literally:  \r\n`*** not bold, just shows as it is ***`  \r\n( You can include literal backticks like `` `this` `` )\r\n\r\nAlternatively, you can just indent every line by at least 4 spaces like so:\r\n\r\n    ##### This text will not be rendered as headers\r\n    ##### This one neither'), TIMESTAMP '2015-06-13 17:09:23.397', TIMESTAMP '2015-06-13 17:17:02.674'),
(6, 'markdown - Links', STRINGDECODE('    Inline style:  [text](URL \"optional title\")  \r\n\r\n    Example:  \r\n      Go to [Google](http://www.google.com \"Google Page\")\r\n###### Output:\r\nGo to [Google](http://www.google.com \"Google Page\")\r\n* * *\r\n    Reference style: [text][label]\r\n    Then, anywhere else in the doc, define the links like so:\r\n        [label]: URL \"optional title\"\r\n\r\n    Example:  \r\n      Go to [Google][0]\r\n\r\n      [0]: http://www.google.com\r\n\r\n###### Output:\r\nGo to [Google][0]\r\n\r\n\r\n[0]: http://www.google.com'), TIMESTAMP '2015-06-13 17:27:30.349', TIMESTAMP '2015-06-13 17:35:14.56');

INSERT INTO PUBLIC.FRAGMENT(FRAGMENT_ID, TITLE, CONTENT, CREATION_DATETIME, UPDATE_DATETIME) VALUES
(7, 'markdown - image', STRINGDECODE('    Inline style:\r\n        ![alternative text](URL_to_image \"optional title\")\r\n\r\n    Example:\r\n        ![Linux Penguin](https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRwUqkWOMq9PEo1qZUyjjaMcToXvwNjJyYH1w982alY0iivYo0k)\r\n###### Output:\r\n![Linux Penguin](https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRwUqkWOMq9PEo1qZUyjjaMcToXvwNjJyYH1w982alY0iivYo0k)\r\n* * *\r\n    Reference style:\r\n        ![alternative text][label]\r\n\r\n\r\n        [label]: URL_to_image \"optional title\"\r\n\r\nExample:  \r\n`![Batman][000]`\r\n\r\n`[000]: https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTk-E0CVdOAiWPGhFwNQKUuTX-qZM2E0PQAebU0U45sl-6mt69h1w \"Batman\"`  \r\n\r\n###### Output:\r\n![Batman][000]\r\n\r\n[000]: https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTk-E0CVdOAiWPGhFwNQKUuTX-qZM2E0PQAebU0U45sl-6mt69h1w \"Batman\"'), TIMESTAMP '2015-06-13 17:43:09.831', TIMESTAMP '2015-06-13 17:54:44.339'),
(8, 'markdown - Etc.', STRINGDECODE('    Horizontal Lines:\r\n        * * *\r\n        - - -\r\n###### Output:\r\n* * *\r\n- - -\r\n\r\n    Manual Line Breaks: End a line with two or more spaces:\r\n        This line has two spaces at the end,    \r\n        So the next line will break like this;\r\n\r\n        Without spaces at the end,\r\n        it won''t break at all.\r\n        \r\n###### Output:\r\nThis line has two spaces at the end,    \r\nSo the next line will break like this;\r\n\r\nWithout spaces at the end,\r\nit won''t break at all.'), TIMESTAMP '2015-06-13 18:02:00.926', TIMESTAMP '2015-06-13 18:02:55.075'),
(9, 'markdown - Table (standard style)', STRINGDECODE('    First Header  | Second Header\r\n    ------------- | -------------\r\n    Content Cell  | Content Cell\r\n    Content Cell  | Content Cell\r\n###### Output:\r\nFirst Header  | Second Header\r\n------------- | -------------\r\nContent Cell  | Content Cell\r\nContent Cell  | Content Cell\r\n'), TIMESTAMP '2015-06-13 18:29:20.161', TIMESTAMP '2015-06-13 18:37:16.392'),
(10, 'markdown - Table (Github style)', STRINGDECODE('    | Left align | Right align | Center align |\r\n    |:-----------|------------:|:------------:|\r\n    |This        |This         |This     \r\n    |column      |column       |column    \r\n    |will        |will         |will     \r\n    |be          |be           |be      \r\n    |left        |right        |center    \r\n    |aligned     |aligned      |aligned\r\n\r\n###### Output:\r\n| Left align | Right align | Center align |\r\n|:-----------|------------:|:------------:|\r\n|This        |This         |This     \r\n|column      |column       |column    \r\n|will        |will         |will     \r\n|be          |be           |be      \r\n|left        |right        |center    \r\n|aligned     |aligned      |aligned'), TIMESTAMP '2015-06-13 18:31:43.561', TIMESTAMP '2015-06-13 18:37:31.893'),
(11, 'Backslash escapes', STRINGDECODE('Markdown provides backslash escapes for the following characters:\r\n\r\n    \\   backslash\r\n    `   backtick\r\n    *   asterisk\r\n    _   underscore\r\n    {}  curly braces\r\n    []  square brackets\r\n    ()  parentheses\r\n    #   hash mark\r\n    +   plus sign\r\n    -   minus sign (hyphen)\r\n    .   dot\r\n    !   exclamation mark'), TIMESTAMP '2015-06-24 17:01:38.77', TIMESTAMP '2015-07-09 08:30:33.219'),
(12, 'How to insert a literal | character inside table cells', STRINGDECODE('{{[clr-r] The | character is used to format tables in Markdown, but it is not escapable by backslash. }}\r\n\r\nFor instance, the following table won''t be rendered correctly\r\n\r\nInput:  \r\n\r\n    AND  | OR\r\n    -----|----\r\n    &&   | \\|\\|\r\n\r\nOutput:\r\n\r\nAND  | OR\r\n-----|----\r\n&&   | \\|\\|\r\n\r\n* * *\r\n[Solution] You can use a HTML entity number {{[clr-b] `&#124;` }} for the literal | in this case.\r\n\r\nInput:  \r\n\r\n    AND  | OR\r\n    -----|----\r\n    &&   | &#124;&#124;\r\n\r\nOutput:\r\n\r\nAND  | OR\r\n-----|----\r\n&&   | &#124;&#124;'), TIMESTAMP '2015-07-09 07:58:13.305', TIMESTAMP '2015-07-09 08:35:50.232');

INSERT INTO PUBLIC.TAG(TAG_ID, TAG_NAME) VALUES
(1, 'markdown'),
(2, 'tips');

INSERT INTO PUBLIC.TAG2FRAGMENT(TAG2FRAGMENT_ID, TAG_ID, FRAGMENT_ID) VALUES
(3, 1, 2),
(4, 1, 1),
(5, 1, 3),
(8, 1, 4),
(14, 1, 5),
(20, 1, 6),
(24, 1, 7),
(27, 1, 8),
(31, 1, 9),
(32, 1, 10),
(46, 1, 11),
(47, 2, 11),
(54, 1, 12),
(55, 2, 12);

INSERT INTO PUBLIC.FRAGMENT2FRAGMENT(FRAGMENT2FRAGMENT_ID, FROM_ID, TO_ID) VALUES
(1, 7, 6),
(2, 6, 7),
(5, 2, 5),
(6, 5, 2),
(7, 1, 5),
(8, 5, 1),
(9, 9, 10),
(10, 10, 9),
(11, 11, 12),
(12, 12, 11),
(15, 9, 12),
(16, 12, 9);

