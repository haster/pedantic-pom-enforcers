/*
 * Copyright (c) 2012 by The Author(s)
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
package com.github.ferstl.maven.pomenforcers;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;

import com.github.ferstl.maven.pomenforcers.artifact.StringToArtifactTransformer;
import com.github.ferstl.maven.pomenforcers.model.ArtifactModel;
import com.github.ferstl.maven.pomenforcers.util.EnforcerRuleUtils;

import static com.github.ferstl.maven.pomenforcers.util.CommaSeparatorUtils.splitAndAddToCollection;

/**
 * Enforces that only a well-defined set of POMs may declare plugin management.
 * <pre>
 * ### Example
 *     &lt;rules&gt;
 *       &lt;dependencyManagementLocation implementation=&quot;com.github.ferstl.maven.pomenforcers.PedanticDependencyManagementLocationEnforcer&quot;&gt;
 *         &lt;!-- Only these POMs may declare dependency management --&gt;
 *         &lt;dependencyManagingPoms&gt;com.example.myproject:parent,com.example.myproject:subparent&lt;/dependencyManagingPoms&gt;
 *       &lt;/dependencyManagementLocation&gt;
 *     &lt;/rules&gt;
 * </pre>
 * @id {@link PedanticEnforcerRule#DEPENDENCY_MANAGEMENT_LOCATION}
 */
public class PedanticDependencyManagementLocationEnforcer extends AbstractPedanticEnforcer {

  private final Set<ArtifactModel> dependencyManagingPoms;

  public PedanticDependencyManagementLocationEnforcer() {
    this.dependencyManagingPoms = new HashSet<>();
  }

  @Override
  protected void doEnforce(EnforcerRuleHelper helper) throws EnforcerRuleException {
    MavenProject mavenProject = EnforcerRuleUtils.getMavenProject(helper);
    if (containsDependencyManagement(getPom()) && !isDependencyManagementAllowed(mavenProject)) {
      throw new EnforcerRuleException("One does not simply declare dependency management. " +
          "Only these POMs are allowed to manage dependencies: " + this.dependencyManagingPoms);
    }
  }

  /**
   * Comma separated list of POMs that may declare <code>&lt;dependencyManagement&gt;</code>.
   * Each POM has to be defined in the format <code>groupId:artifactId</code>.
   * @param dependencyManagingPoms Comma separated list of POMs that may declare plugin management.
   * @configParam
   * @default n/a
   */
  public void setDependencyManagingPoms(String dependencyManagingPoms) {
    StringToArtifactTransformer stringToArtifactTransformer = new StringToArtifactTransformer();
    splitAndAddToCollection(dependencyManagingPoms, this.dependencyManagingPoms, stringToArtifactTransformer);
  }

  private boolean containsDependencyManagement(Document pom) {
    return !getProjectModel().getManagedDependencies().isEmpty();
  }

  private boolean isDependencyManagementAllowed(MavenProject project) {
    ArtifactModel projectInfo = new ArtifactModel(project.getGroupId(), project.getArtifactId(), project.getVersion());
    return this.dependencyManagingPoms.contains(projectInfo);
  }

  @Override
  protected void accept(PedanticEnforcerVisitor visitor) {
    visitor.visit(this);
  }

}
