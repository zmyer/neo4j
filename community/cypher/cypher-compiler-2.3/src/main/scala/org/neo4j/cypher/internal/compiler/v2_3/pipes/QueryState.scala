/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_3.pipes

import java.util.{Set, UUID}

import org.neo4j.cypher.internal.compiler.v2_3._
import org.neo4j.cypher.internal.compiler.v2_3.commands.expressions.PathValueBuilder
import org.neo4j.cypher.internal.compiler.v2_3.spi.QueryContext

import scala.collection.mutable

case class QueryState(query: QueryContext,
                      resources: ExternalResource,
                      params: Map[String, Any],
                      decorator: PipeDecorator,
                      timeReader: TimeReader = new TimeReader,
                      var initialContext: Option[ExecutionContext] = None,
                      queryId: AnyRef = UUID.randomUUID().toString,
                      triadicSets: mutable.Map[String, Set[Long]] = new mutable.HashMap[String, Set[Long]]()) {
  private var _pathValueBuilder: PathValueBuilder = null

  def clearPathValueBuilder = {
    if (_pathValueBuilder == null) {
      _pathValueBuilder = new PathValueBuilder()
    }
    _pathValueBuilder.clear()
  }

  def readTimeStamp(): Long = timeReader.getTime

  def getParam(key: String): Any =
    params.getOrElse(key, throw new ParameterNotFoundException("Expected a parameter named " + key))

  def getStatistics = query.getOptStatistics.getOrElse(QueryState.defaultStatistics)

  def withDecorator(decorator: PipeDecorator) = copy(decorator = decorator)
}

object QueryState {
  val defaultStatistics = InternalQueryStatistics()
}

class TimeReader {
  lazy val getTime = System.currentTimeMillis()
}
