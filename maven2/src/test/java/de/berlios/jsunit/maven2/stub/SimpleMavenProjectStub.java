/*
 * Copyright (C) 2006,2007 Joerg Schaible
 * Created on 02.10.2006 by Joerg Schaible
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

package de.berlios.jsunit.maven2.stub;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;


/**
 * A simple Stub for a MavenProject.
 * 
 * @author J&ouml;rg Schaible
 */
public class SimpleMavenProjectStub extends MavenProjectStub {
    // ~ Methods ----------------------------------------------------------------

    /**
     * Create an artifact
     * 
     * @param groupId the groupId
     * @param artifactId the artifactId
     * @param version the version
     * @param type the type
     * @param classifier the classifier
     * @param file the file itself
     * @return the artifact
     */
    protected Artifact createArtifact(
                                      final String groupId, final String artifactId,
                                      final String version, final String type,
                                      final String classifier, final File file) {
        final Artifact artifact = new DefaultArtifact(
            groupId, artifactId, VersionRange.createFromVersion(version), "compile", type,
            classifier, new DefaultArtifactHandler(type));
        if (file != null) {
            artifact.setFile(file);
        }

        return artifact;
    }
}
