# Sclera - SQL Tests Parser

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.scleradb/sclera-sqltests-parser_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.scleradb/sclera-sqltests-parser_2.13)
[![scaladoc](https://javadoc.io/badge2/com.scleradb/sclera-sqltests-parser_2.13/scaladoc.svg)](https://javadoc.io/doc/com.scleradb/sclera-sqltests-parser_2.13)

Incrementally parses a given test output script into a stream (iterator) of SQL queries/statements and their results.

This is useful while testing SQL based systems for correctness, identifying regressions, etc. We first obtain a "reference" script by running a suite of SQL commands and queries on a known correct system. The output, containing the SQL queries/statements and their results, is parsed using this tool. The queries/statements in the resulting iterator are then run on the system being tested, and the results obtained are checked against the reference results.
