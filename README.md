escli
======

This program is a command line client allowing to run basic queries against elastic search using a language somewhat similar to SQL. 

*This is a work in progress* : the program only support a limited set of commands.


Usage
-----
From the sbt command line, type ```run``` optionnaly followed by the base address of an elastic instance. If no address is given, 'http://127.0.0.1:9200' is used.

```
%> sbt
> run
```


```
%> sbt
> run http://myelastic.host.com:9200
```

Supported commands
----------------------

All commands must be ended by ';' to be interpreted.

### Select

```SQL
SELECT [select_list] FROM [source] <where_clause> <limit_clause>
```
Where
- ```[select_list]``` is either a star (*) to fetch all fields or a comma separated list of field, optionnaly using pattern similar to the one used in elastic. (Ex. ```*``` or ```field1, field2``` or ```field*```.
- ```[source]``` can be a star (*) or an index name or pattern and optionally a type. (Ex. ```*``` or ```myindexname``` or ```myindexname WITH TYPE mytype```
- ```<where_clause>``` is similar to SQL where clause but limited to the following predicates
  - comparison: =, >, >=, <, <=
  - range: ```BETWEEN x AND y```
  - terms: ```IN ('value1', 'value2')```
  - compound: OR, AND
- ```<limit_clause>``` indicates a limit of the result set. The default limit is the result set limit of elastic query.

#### Limitations
- equality with string literal and terms comparison is transform to a term/terms elastic query, see elastic documentation for details
- IN keyword only allows string literals at the moment.

#### Examples
```SQL
SELECT * FROM *;

SELECT * FROM myindex;

SELECT * FROM myindex WITH TYPE mytype;

SELECT * FROM someindex* WITH TYPE mytype;

SELECT field1, field2 FROM myindex;

SELECT _id, field* FROM myindex;

SELECT _id, field* FROM myindex WHERE field1=12.45 AND field3='value';

SELECT _id, field* FROM myindex WHERE field1 BETWEEN 12 and 13;

SELECT _id, field* FROM myindex WHERE field3 IN ('value1', 'value2');

### Explain

Prefixing a query with ```explain``` will display the json request generated without issuing the query.

Prefixing a query with ```explain result``` will display the json request generated and also the json response from the server.

### Exit

Does what it says.
