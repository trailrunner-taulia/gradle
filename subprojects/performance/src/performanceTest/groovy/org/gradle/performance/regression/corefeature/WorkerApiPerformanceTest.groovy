/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.regression.corefeature

import org.gradle.performance.AbstractCrossVersionPerformanceTest

class WorkerApiPerformanceTest extends AbstractCrossVersionPerformanceTest {
    def setup() {
        runner.minimumVersion = '3.5'
        runner.targetVersions = ["5.3-20190206000050+0000"]
        runner.testProject = "workerApiProject"
    }

    def "executing tasks with no isolation with #workItems / #workers"() {
        given:
        runner.tasksToRun = ['clean', 'noIsolation', "--outputSize=$workItems"]
        runner.args = [ "--max-workers=$workers" ]

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        where:
        workers | workItems
        1       | 1
//        1       | 10
//        4       | 1
//        4       | 4
//        4       | 40
//        12      | 1
//        12      | 12
//        12      | 120
    }

    def "executing tasks with classloader isolation"() {
        given:
        runner.tasksToRun = ['clean', 'classloaderIsolation', "--outputSize=$workItems"]
        runner.args = [ "--max-workers=$workers" ]

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        where:
        workers | workItems
        1       | 1
//        1       | 10
//        4       | 1
//        4       | 4
//        4       | 40
//        12      | 1
//        12      | 12
//        12      | 120
    }

    def "executing tasks with process isolation"() {
        given:
        runner.tasksToRun = ['clean', 'classloaderIsolation', "--outputSize=$workItems"]
        runner.args = [ "--max-workers=$workers" ]

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        where:
        workers | workItems
        1       | 1
//        1       | 10
//        4       | 1
//        4       | 4
//        4       | 40
//        12      | 1
//        12      | 12
//        12      | 120
    }
}