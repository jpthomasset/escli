# Querying Elasticsearch

## Problem/Now
- Hard to query data
- Hard to create query with filter 
- Hard to create query with aggregation
- Hard to update data !

## Awesome
- Natural language to query elastic (SQL like)
- Natural language to update
- Autocomplete
- UI to query
- UI to present data

## Next Target
Basic DSL -> AST for querying
- [ ] Being able to make a request to ES (not based on a query)
- [ ] Parse Simple query

## First steps
- [ ] App skeleton with ES host params
- [X] Screen parsing libraries
- [ ] Identify basic keywords (select from)
- [ ] Identify fields in query
- [ ] Identify source (index/type)


# Notes

## Query Parsing
- http://kufli.blogspot.fr/2015/01/scala-parser-combinators-sql-parser.html
- https://github.com/scala/scala-parser-combinators

## Command line parsing
- http://docopt.org/
- https://github.com/docopt/docopt.scala
