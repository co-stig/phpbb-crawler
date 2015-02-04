phpbb-crawler
=============

A simple phpBB forum crawler/parser, designed mostly for scientific purposes. I created it as a part of my MSc research, you can find further details in [report](https://github.com/co-stig/phpbb-crawler/blob/master/master-report.pdf?raw=true) and [presentation](https://github.com/co-stig/phpbb-crawler/blob/master/master-presentation.pdf?raw=true).

Quick start
=============

1. Create JDBC database, e.g. using Oracle XE
2. Create database schema by executing ``pphbb-parser/src/ddl.sql``
3. Parse topics list by executing ``java net.kulak.psy.parser.runnable.ParseTopicsList`` (see usage)
4. Parse topics contents by executing ``java net.kulak.psy.parser.runnable.ParseTopicContents`` (see usage)
5. Now your database contains simple communication sequence (self-explanatory)
