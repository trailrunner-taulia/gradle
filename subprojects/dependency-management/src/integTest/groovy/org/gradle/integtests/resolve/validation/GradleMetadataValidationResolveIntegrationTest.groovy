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

package org.gradle.integtests.resolve.validation

import org.gradle.integtests.fixtures.GradleMetadataResolveRunner
import org.gradle.integtests.fixtures.RequiredFeature
import org.gradle.integtests.fixtures.RequiredFeatures
import org.gradle.integtests.resolve.AbstractModuleDependencyResolveTest
import org.gradle.test.fixtures.gradle.GradleFileModuleAdapter
import spock.lang.Unroll

class GradleMetadataValidationResolveIntegrationTest extends AbstractModuleDependencyResolveTest {

    @RequiredFeatures([
        @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value = "true")
    ])
    def "can resolve if component gav information is missing"() {
        GradleFileModuleAdapter.printComponentGAV = false
        buildFile << """
            repositories.all {
                metadataSources {
                    gradleMetadata()
                }
            }
            dependencies {
                conf 'org.test:projectA:1.1'
            }
        """

        when:
        repository {
            'org.test:projectA:1.1'()
        }
        repositoryInteractions {
            'org.test:projectA:1.1' {
                expectResolve()
            }
        }

        then:
        succeeds ":checkDeps"
        resolve.expectGraph {
            root(":", ":test:") {
                module("org.test:projectA:1.1")
            }
        }

        cleanup:
        GradleFileModuleAdapter.printComponentGAV = true
    }

    @RequiredFeatures([
        @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value = "true")
    ])
    @Unroll
    def "fails with proper error if #label is not defined"() {
        buildFile << """
            repositories.all {
                metadataSources {
                    gradleMetadata()
                }
            }
            dependencies {
                conf 'org.test:projectA:1.1'
            }
        """

        when:
        repository {
            'org.test:projectA:1.1' {
                variant("api", incompleteVariant)
            }
        }
        repositoryInteractions {
            'org.test:projectA:1.1' {
                expectGetMetadata()
            }
        }

        then:
        fails ":checkDeps"
        failure.assertHasCause("missing '$attribute' at $path")

        where:
        label                         | path                                                    | attribute | incompleteVariant
        'variant name'                | '/variants[2]'                                          | 'name'    | { name = null }

        'available-at url'            | '/variants[0]/available-at'                             | 'url'     | { availableAt(null, "g", "c", "1.0") }
        'available-at group'          | '/variants[0]/available-at'                             | 'group'   | { availableAt("../path", null, "c", "1.0") }
        'available-at module'         | '/variants[0]/available-at'                             | 'module'  | { availableAt("../path", "g", null, "1.0") }
        'available-at version'        | '/variants[0]/available-at'                             | 'version' | { availableAt("../path", "g", "c", null) }

        'dependency group'            | '/variants[0]/dependencies[0]'                          | 'group'   | { dependsOn(null, "c", "") }
        'dependency module'           | '/variants[0]/dependencies[0]'                          | 'module'  | { dependsOn("g", null, "") }

        'capability group'            | '/variants[0]/capabilities[0]'                          | 'group'   | { capability(null, "c", "1.0") }
        'capability name'             | '/variants[0]/capabilities[0]'                          | 'name'    | { capability("g", null, "1.0") }
        'capability version'          | '/variants[0]/capabilities[0]'                          | 'version' | { capability("g", "c", null) }

        'req capability group'        | '/variants[0]/dependencies[0]/requestedCapabilities[0]' | 'group'   | { dependsOn("g", "c", "1.0") { requestedCapability(null, "c2", "1.0")} }
        'req capability name'         | '/variants[0]/dependencies[0]/requestedCapabilities[0]' | 'name'    | { dependsOn("g", "c", "1.0") { requestedCapability("g", null, "1.0")} }
        'req capability version'      | '/variants[0]/dependencies[0]/requestedCapabilities[0]' | 'version' | { dependsOn("g", "c", "1.0") { requestedCapability("g", "c2", null)} }

        'dependency constraint group' | '/variants[0]/dependencyConstraints[0]'                 | 'group'   | { constraint(null, "c", "") }
        'dependency constraint module'| '/variants[0]/dependencyConstraints[0]'                 | 'module'  | { constraint("g", null, "") }

        'file name'                   | '/variants[0]/files[0]'                                 | 'name'    | { artifact(null, "url") }
        'file url'                    | '/variants[0]/files[0]'                                 | 'url'     | { artifact("name", null) }
    }
}
