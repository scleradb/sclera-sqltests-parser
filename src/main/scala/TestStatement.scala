/**
* Sclera - Tests
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

sealed abstract class TestStatement {
    val statement: String
    val comments: List[String]
}

sealed abstract class TestUpdate extends TestStatement

case class TestUpdateSuccess(
    override val statement: String,
    override val comments: List[String]
) extends TestUpdate

case class TestUpdateError(
    override val statement: String,
    override val comments: List[String],
    message: List[String]
) extends TestUpdate

sealed abstract class TestQuery extends TestStatement

case class TestQuerySuccess(
    override val statement: String,
    override val comments: List[String],
    results: TestResult
) extends TestQuery

case class TestQueryError(
    override val statement: String,
    override val comments: List[String],
    message: List[String]
) extends TestQuery

case class TestIgnored(
    override val statement: String,
    override val comments: List[String],
    resultsOpt: Option[TestResult],
    message: List[String]
) extends TestStatement

case class TestIssue(
    issue: Int,
    testStatement: TestStatement
) extends TestStatement {
    override val statement: String = testStatement.statement
    override val comments: List[String] = testStatement.comments
}

case class TestNotSupported(
    testStatement: TestStatement
) extends TestStatement {
    override val statement: String = testStatement.statement
    override val comments: List[String] = testStatement.comments
}
