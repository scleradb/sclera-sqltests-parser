# Sclera - SQL Tests Parser

Incrementally parses a given test output script into a stream (iterator) of SQL queries/statements and their results.

This is useful while testing SQL based systems for correctness, identifying regressions, etc. We first obtain a "reference" script by running a suite of SQL commands and queries on a known correct system. The output, containing the SQL queries/statements and their results, is parsed using this tool. The queries/statements in the resulting iterator are then run on the system being tested, and the results obtained are checked against the reference results.
