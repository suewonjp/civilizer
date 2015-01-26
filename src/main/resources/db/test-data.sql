INSERT INTO GLOBAL_SETTING(SETTING_NAME, SETTING_VALUE) VALUES
('database.version', '1');

INSERT INTO FRAGMENT(FRAGMENT_ID, TITLE, CONTENT, CREATION_DATETIME, UPDATE_DATETIME, FILE_NAME, FILE_TYPE, FILE_SIZE, CHILDREN_ORDERED_BY, CHILDREN_ORDERED_IN_ASC, PASSWORD, CREATOR, UPDATER, TAG_ID) VALUES
(1, 'bold', STRINGDECODE('
    **bold** or __bold__
* * *
**bold** or __bold__
'), TIMESTAMP '2014-08-25 03:39:15.1', TIMESTAMP '2014-08-25 03:39:15.1', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', NULL, NULL),
(2, 'Blockquotes', STRINGDECODE('
    > Email-style angle brackets
    > are used for blockquotes.
    > > And, they can be nested.
* * *
    > >
    > * You can quote a list.
    > * Etc.

> Email-style angle brackets
> are used for blockquotes.
> > And, they can be nested.

> >
> * You can quote a list.
> * Etc.
'), TIMESTAMP '2014-11-03 19:05:59.121', TIMESTAMP '2014-11-03 19:10:25.797', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', 'owner', NULL),
(3, 'Links', STRINGDECODE('
    [text](http://url.com/ "Title (optional)")
* * *
H2 [Quickstart](http://www.h2database.com/html/quickstart.html "H2 Quickstart")
'), TIMESTAMP '2014-11-03 19:07:00.324', TIMESTAMP '2014-11-03 19:07:00.324', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', NULL, NULL),
(4, 'Images', STRINGDECODE('
    ![alt text](/path/to/image "Title (optional)")
* * *
![batman](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTk-E0CVdOAiWPGhFwNQKUuTX-qZM2E0PQAebU0U45sl-6mt69h1w "Batman")
'), TIMESTAMP '2014-11-03 19:08:24.763', TIMESTAMP '2014-11-03 19:08:24.763', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', NULL, NULL),
(5, 'Headers', STRINGDECODE('
    Setext-style:
        Header 1
        ========

        Header 2
        --------
* * *
Header 1
========
\r\n
Header 2\r\n--------
'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', NULL, NULL),
(6, 'Trashed', STRINGDECODE('    This is a trashed fragment...'), TIMESTAMP '2014-11-03 19:12:44.752', TIMESTAMP '2014-11-03 19:12:44.752', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', NULL, NULL),
(7, 'Lists - ordered', STRINGDECODE('
    1.  Foo
    1.  Bar\r\n---
1.  Foo
1.  Bar
'), TIMESTAMP '2015-01-13 14:32:00.000', TIMESTAMP '2015-01-13 14:32:00.000', NULL, NULL, NULL, 1, FALSE, NULL, 'owner', NULL, NULL);

INSERT INTO TAG(TAG_ID, TAG_NAME, CREATION_DATETIME, UPDATE_DATETIME, CREATOR, UPDATER, FRAGMENT_ID) VALUES
(0, '#trash', TIMESTAMP '2015-01-13 14:23:00.000', TIMESTAMP '2015-01-13 14:23:00.000', 'system', NULL, NULL),
(1, 'emphasis', TIMESTAMP '2014-08-25 03:39:15.106', TIMESTAMP '2014-08-25 03:39:15.106', 'owner', NULL, NULL),
(2, 'blockquotes', TIMESTAMP '2014-11-03 19:05:59.134', TIMESTAMP '2014-11-03 19:05:59.134', 'owner', NULL, NULL),
(3, 'links', TIMESTAMP '2014-11-03 19:07:00.33', TIMESTAMP '2014-11-03 19:09:38.441', 'owner', 'owner', NULL),
(4, 'images', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(5, 'headers', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(6, 'lists', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(7, 'code spans', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(8, 'preformatted code blocks', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(9, 'horizontal rules', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(10, 'manual line breaks', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL),
(11, 'markdown', TIMESTAMP '2014-11-03 19:08:24.767', TIMESTAMP '2014-11-03 19:08:24.767', 'owner', NULL, NULL);

INSERT INTO TAG2TAG(TAG2TAG_ID, PARENT_ID, CHILD_ID) VALUES
(1, 11, 1),
(2, 11, 2),
(3, 11, 3),
(4, 11, 4),
(5, 11, 5),
(6, 11, 6),
(7, 11, 7),
(8, 11, 8),
(9, 11, 9),
(10, 11, 10);

INSERT INTO TAG2FRAGMENT(TAG2FRAGMENT_ID, TAG_ID, FRAGMENT_ID) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5),
(6, 0, 6),
(7, 6, 7);

INSERT INTO FRAGMENT2FRAGMENT(FRAGMENT2FRAGMENT_ID, FROM_ID, TO_ID, CREATION_DATETIME, UPDATE_DATETIME, CREATOR, UPDATER, PRIORITY) VALUES
(1, 1, 2, TIMESTAMP '2014-11-03 19:07:21.641', TIMESTAMP '2014-11-03 19:07:21.641', 'owner', NULL, 0),
(2, 2, 1, TIMESTAMP '2014-11-03 19:07:21.644', TIMESTAMP '2014-11-03 19:07:21.644', 'owner', NULL, 0),
(3, 2, 4, TIMESTAMP '2014-11-03 19:08:51.216', TIMESTAMP '2014-11-03 19:08:51.216', 'owner', NULL, 0);

