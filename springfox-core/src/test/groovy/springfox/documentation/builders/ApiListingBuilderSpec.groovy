/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.builders
import com.google.common.collect.Ordering
import spock.lang.Specification
import spock.lang.Unroll
import springfox.documentation.schema.Model
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.SecurityReference
import springfox.documentation.service.Tag

import static springfox.documentation.builders.BuilderDefaults.nullToEmptySet

class ApiListingBuilderSpec extends Specification {
  def "Setting properties on the builder with non-null values"() {
    given:
      def orderingMock = Mock(Ordering)
      def sut = new ApiListingBuilder(orderingMock)
    and:
      orderingMock.sortedCopy(value) >> value
    when:
      sut."$builderMethod"(value)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod       | value                         | property
      'apiVersion'        | '1.0'                         | 'apiVersion'
      'basePath'          | 'urn:base-path'               | 'basePath'
      'resourcePath'      | 'urn:resource-path'           | 'resourcePath'
      'description'       | 'test'                        | 'description'
      'position'          | 1                             | 'position'
      'produces'          | ['app/json'] as Set           | 'produces'
      'consumes'          | ['app/json'] as Set           | 'consumes'
      'host'              | 'test'                        | 'host'
      'protocols'         | ['https']  as Set             | 'protocols'
      'securityReferences'| [Mock(SecurityReference)]     | 'securityReferences'
      'apis'              | [Mock(ApiDescription)]        | 'apis'
      'models'            | [m1: Mock(Model)]             | 'models'
      'tags'              | [new Tag("test", "")] as Set  | 'tags'
  }

  def "Setting properties on the builder with null values preserves existing values"() {
    given:
      def orderingMock = Mock(Ordering)
      def sut = new ApiListingBuilder(orderingMock)
    and:
      orderingMock.sortedCopy(value) >> value
    when:
      sut."$builderMethod"(value)
    and:
      sut."$builderMethod"(null)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod       | value                         | property
      'apiVersion'        | '1.0'                         | 'apiVersion'
      'basePath'          | 'urn:base-path'               | 'basePath'
      'resourcePath'      | 'urn:resource-path'           | 'resourcePath'
      'description'       | 'test'                        | 'description'
      'produces'          | ['app/json'] as Set           | 'produces'
      'consumes'          | ['app/json'] as Set           | 'consumes'
      'host'              | 'test'                        | 'host'
      'protocols'         | ['https'] as Set              | 'protocols'
      'securityReferences'| [Mock(SecurityReference)]     | 'securityReferences'
      'apis'              | [Mock(ApiDescription)]        | 'apis'
      'models'            | [m1: Mock(Model)]             | 'models'
      'tags'              | [new Tag("test", "")] as Set  | 'tags'
  }

  @Unroll
  def "Appending to properties on the builder"() {
    given:
      def orderingMock = Mock(Ordering)
      def sut = new ApiListingBuilder(orderingMock)
    and:
      orderingMock.sortedCopy(value) >> value
    when:
      sut."$builderMethod"(value)
    and:
      sut."$builderMethod"(null)
    and:
      def built = sut.build()
    then:
      built."$property" == nullToEmptySet(value as Set)

    where:
    builderMethod       | value   | property
    'appendProduces'    | ["a"]   | 'produces'
    'appendConsumes'    | ["a"]   | 'consumes'
    'appendProduces'    | []      | 'produces'
    'appendConsumes'    | []      | 'consumes'
    'appendProduces'    | [null]  | 'produces'
    'appendConsumes'    | [null]  | 'consumes'
    'appendProduces'    | null    | 'produces'
    'appendConsumes'    | null    | 'consumes'

  }

  @Unroll
  def "Available tags only uses unique values"() {
    given:
      def sut = new ApiListingBuilder(Mock(Ordering))
    when:
      sut.availableTags(tags)
          .tagNames(["test"] as Set)
          .description("Default")
    and:
      def built = sut.build()
    then:
      built.tags.size() == expectedCount
      if (built.tags.size() == 1) {
        built.tags.first().description == description
      } else {
        built.tags.any{
          it.name == 1 && it.description == "Description 1"
        }
      }

    where:
      tags                              | description         | expectedCount
      [new Tag("test", "")] as Set      | ""                  | 1
      [new Tag("test", "Test")] as Set  | "Test"              | 1
      [] as Set                         | "Default"           | 1
      null                              | "Default"           | 1
      [tag("1"), tag("2")] as Set       | "Default"           | 1
      [tag("test"), tag("2")] as Set    | "Description test"  | 1
  }

  def tag(name) {
    new Tag(name, "Description $name")
  }

  @Unroll
  def "Tag names are converted to tags"() {
    given:
      def orderingMock = Mock(Ordering)
      def sut = new ApiListingBuilder(orderingMock).description("Some Description")
    and:
      orderingMock.sortedCopy(value) >> value
    when:
      sut.tagNames(value as Set);
    and:
      def built = sut.build()
    then:
      built.tags.size() == expectedSize

    where:
    expectedSize | value
    1            | ["test", ""]
    1            | ["test"]
    1            | ["test", null]
    1            | [null, "test"]
    2            | [null, "test", "test2", ""]
    2            | [null, "test", "test2", ""]
  }
}
