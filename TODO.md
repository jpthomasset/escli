# What would be awesome
- Natural language to query elastic (SQL like)
- Natural language to update/delete
- Autocomplete
- UI to query
- UI to present data


# Next steps
- Refactor & Add test on ElasticJsonPrinter (column selection)
- Add test on CommandHandler
- Add Signal Handler to intercept CTRL+C to abort a long running query
- Query language:
  - Add keywords: offset, desc, show indices
  - Add !=, is null, is not null, not
  - handle numbers in 'IN' clause
- Handle double quote for strings
- Use external pager for output (as in mysql)
- handle program argument cleanly

