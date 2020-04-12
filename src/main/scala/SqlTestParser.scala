/**
* Sclera - SQL Tests Parser
* Copyright 2012 - 2020 Sclera, Inc.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*     http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.scleradb.sqltests.parser

import java.sql.SQLSyntaxErrorException

import java.io.{InputStream, InputStreamReader}
import scala.util.parsing.input.{StreamReader, Reader, Position}

import scala.util.parsing.combinator.RegexParsers

object SqlTestParser extends RegexParsers {
    override def skipWhitespace: Boolean = false

    def testStatement: Parser[TestStatement] =
        """\s*""".r ~> commentsOpt ~ testStatementBody <~ """\s*""".r ^^ {
            case cOpt~stmt => stmt(cOpt)
        } |
        err("Syntax error")

    def commentsOpt: Parser[List[String]] = rep(commentBodyOpt <~ eol)

    def commentBodyOpt: Parser[String] = """--[^\r\n]*""".r

    def testStatementBody: Parser[List[String] => TestStatement] =
        updateStatement | queryStatement |
        "(?i)ISSUE".r ~> """\s*""".r ~>
        ("[" ~> """\d+""".r <~ "]" <~ """\s*""".r) ~ 
        (updateStatement | queryStatement) ^^ {
            case issue~stmt =>
                (cmnts: List[String]) => TestIssue(issue.toInt, stmt(cmnts))
        } |
        "(?i)NOTSUPPORTED".r ~> """\s*""".r ~>
        (updateStatement | queryStatement) ^^ {
            stmt => (cmnts: List[String]) => TestNotSupported(stmt(cmnts))
        } |
        ignoredStatement ^^ {
            stmt => (cmnts: List[String]) => TestNotSupported(stmt(cmnts))
        }

    def updateStatement: Parser[List[String] => TestUpdate] =
        ("(?i)CREATE".r | "(?i)DROP".r |
         "(?i)UPDATE".r | "(?i)INSERT".r | "(?i)DELETE".r) ~
        stmtBody ~ rep(statementError) ^^ {
            case kwd~rem~Nil =>
                val stmt: String = kwd + rem
                (cmnts: List[String]) => TestUpdateSuccess(stmt, cmnts)
            case kwd~rem~msgs =>
                val stmt: String = kwd + rem
                (cmnts: List[String]) => TestUpdateError(stmt, cmnts, msgs)
        }

    def queryStatement: Parser[List[String] => TestQuery] =
        "(?i)SELECT".r ~ stmtBody ~ queryResponse ^^ {
            case kwd~rem~response => response(kwd + rem)
        } |
        ("[" ~> "(?i)QUERY".r <~ "]") ~> stmtBody ~ queryResponse ^^ {
            case stmt~response => response(stmt)
        }

    def stmtBody: Parser[String] =
        """(?m)(("[^"]*")|[^";])*;""".r <~ ws <~ opt(commentBodyOpt) <~ eol

    def queryResponse: Parser[String => List[String] => TestQuery] =
        queryResults ^^ { res =>
            (stmt: String) =>
                (cmnts: List[String]) => TestQuerySuccess(stmt, cmnts, res)
        } |
        rep1(statementError) ^^ { msgs =>
            (stmt: String) =>
                (cmnts: List[String]) => TestQueryError(stmt, cmnts, msgs)
        }

    def queryResults: Parser[TestResult] =
        opt(divider) ~> (row <~ divider) ~
        rep(row) ~ (opt(divider) ~> card) ^^ {
            case resCols~resRows~resCard =>
                resRows.foreach { resRow =>
                    assert(
                        resRow.size == resCols.size,
                        "Expecting " + resCols.size +
                        " columns, found " + resRow.size
                    )
                }
                assert(
                    resRows.size == resCard,
                    "Expecting " + resCard + " rows, found " + resRows.size
                )

                TestResult(resCols, resRows)
        }

    def statementError: Parser[String] =
        ("ERROR:" | "LINE " | "HINT:" | "DETAIL:" | "NOTICE:") ~
        """[^\r\n]*""".r <~ eol <~ opt(pointer <~ eol) ^^ {
            case kwd~rem => kwd + rem
        }

    def divider: Parser[String] = """(-|\+)+""".r <~ eol

    def row: Parser[List[String]] = " " ~> rep1sep(col, "|") <~ eol

    def col: Parser[String] = ws ~> """[^\|\s\n]*([ \t]+[^\|\s\n]+)*""".r <~ ws

    def card: Parser[Int] =
        "(" ~> """\d+""".r <~ """ rows?""".r <~ ")" <~ eol ^^ { s => s.toInt }

    def ws: Parser[String] = """[ \t]*""".r
    def eol: Parser[String] = ws ~> """\r?\n""".r
    def pointer: Parser[String] = """[ \t\^]+""".r

    def ignoredStatement: Parser[List[String] => TestIgnored] =
        ("(?i)(RE)?SET".r | "(?i)BEGIN".r | "(?i)ROLLBACK".r | "(?i)EXECUTE".r |
         "(?i)ANALYZE".r | "(?i)EXPLAIN".r | "(?i)PREPARE".r | "(?i)COPY".r) ~
        stmtBody ~ opt(queryResults)~rep(statementError) ^^ {
            case kwd~rem~resOpt~msgs =>
                val stmt: String = kwd + rem
                (cmnts: List[String]) => TestIgnored(stmt, cmnts, resOpt, msgs)
        }

    def testStatements(reader: Reader[Elem]): Iterator[(TestStatement, Int)] =
        if( reader.atEnd ) Iterator.empty else {
            val line: Int = reader.pos.line
            parse(testStatement, reader) match {
                case Success(parsed, in) =>
                    Iterator((parsed, line)) ++ testStatements(in)
                case NoSuccess(msg, in) =>
                    throw new SQLSyntaxErrorException(
                        s"[$line] ERROR: $msg (${in.pos})\n${in.pos.longString}"
                    )
            }
        }

    def main(args: Array[String]): Unit = args.foreach { scriptFile =>
        val scriptStreamOpt: Option[InputStream] =
            Option(getClass().getResourceAsStream(scriptFile))

        if( scriptStreamOpt.isEmpty ) {
            println("Could not access script at " + scriptFile)
        } else {
            val scriptReader: StreamReader =
                StreamReader(new InputStreamReader(scriptStreamOpt.get))

            testStatements(scriptReader).foreach {
                case (s, i) => println("[%d] %s".format(i, s))
            }
        }
    }
}
