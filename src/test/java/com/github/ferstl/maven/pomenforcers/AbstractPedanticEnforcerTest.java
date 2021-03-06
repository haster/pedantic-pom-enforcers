/*
 * Copyright (c) 2012 - 2015 by Stefan Ferstl <st.ferstl@gmail.com>
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

import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.ferstl.maven.pomenforcers.model.DependencyModel;
import com.github.ferstl.maven.pomenforcers.model.DependencyScope;
import com.github.ferstl.maven.pomenforcers.model.PluginModel;
import com.github.ferstl.maven.pomenforcers.model.ProjectModel;

import static com.github.ferstl.maven.pomenforcers.ErrorReportMatcher.hasErrors;
import static com.github.ferstl.maven.pomenforcers.ErrorReportMatcher.hasNoErrors;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class AbstractPedanticEnforcerTest<T extends AbstractPedanticEnforcer> {

  private static final String DEFAULT_VERSION = "1.0";

  private EnforcerRuleHelper mockHelper;
  ProjectModel projectModel;
  MavenProject mockMavenProject;
  T testRule;
  ErrorReport report;

  @Before
  public void setup() throws Exception {
    this.mockHelper = mock(EnforcerRuleHelper.class);
    this.projectModel = mock(ProjectModel.class);
    this.mockMavenProject = mock(MavenProject.class);

    when(this.projectModel.getDependencies()).thenReturn(new LinkedList<DependencyModel>());
    when(this.projectModel.getManagedDependencies()).thenReturn(new LinkedList<DependencyModel>());
    when(this.projectModel.getPlugins()).thenReturn(new LinkedList<PluginModel>());
    when(this.projectModel.getManagedPlugins()).thenReturn(new LinkedList<PluginModel>());
    when(this.mockMavenProject.getDependencies()).thenReturn(new LinkedList<Dependency>());
    DependencyManagement depMgmtMock = mock(DependencyManagement.class);
    PluginManagement pluginMgmtMock = mock(PluginManagement.class);
    when(depMgmtMock.getDependencies()).thenReturn(new LinkedList<Dependency>());
    when(pluginMgmtMock.getPlugins()).thenReturn(new LinkedList<Plugin>());
    when(this.mockMavenProject.getDependencyManagement()).thenReturn(depMgmtMock);
    when(this.mockMavenProject.getPluginManagement()).thenReturn(pluginMgmtMock);

    ConsoleLogger plexusLogger = new ConsoleLogger(Logger.LEVEL_DEBUG, "testLogger");
    when(this.mockHelper.getLog()).thenReturn(new DefaultLog(plexusLogger));

    when(this.mockHelper.evaluate("${project}")).thenReturn(this.mockMavenProject);

    this.testRule = createRule();
    this.testRule.initialize(this.mockHelper, createEmptyPom(), this.projectModel);
    this.report = new ErrorReport(this.testRule.getDescription());
  }

  abstract T createRule();

  @Test
  public abstract void getDescription();

  @Test
  public abstract void accept();

  protected void executeRuleAndCheckReport(boolean hasErrors) {
    this.testRule.doEnforce(this.report);

    if (hasErrors) {
      assertThat(this.report, hasErrors());
    } else {
      assertThat(this.report, hasNoErrors());
    }
  }

  protected void addDependency(String groupId, String artifactId, DependencyScope scope) {
    String version = DEFAULT_VERSION;

    Dependency mavenDependency = createMavenDependency(groupId, artifactId, scope, version);
    DependencyModel dependency = createDependencyModel(groupId, artifactId, version);

    this.mockMavenProject.getDependencies().add(mavenDependency);
    this.projectModel.getDependencies().add(dependency);
  }

  protected void addManagedDependency(String groupId, String artifactId, DependencyScope scope) {
    String version = DEFAULT_VERSION;

    Dependency mavenDependency = createMavenDependency(groupId, artifactId, scope, version);
    DependencyModel dependency = createDependencyModel(groupId, artifactId, version);

    this.mockMavenProject.getDependencyManagement().getDependencies().add(mavenDependency);
    this.projectModel.getManagedDependencies().add(dependency);
  }

  private static DependencyModel createDependencyModel(String groupId, String artifactId, String version) {
    return new DependencyModel(groupId, artifactId, version, null, null, null);
  }

  private static Dependency createMavenDependency(String groupId, String artifactId, DependencyScope scope, String version) {
    Dependency mavenDependency = new Dependency();
    mavenDependency.setGroupId(groupId);
    mavenDependency.setArtifactId(artifactId);
    mavenDependency.setVersion(version);
    mavenDependency.setScope(scope.getScopeName());
    return mavenDependency;
  }

  private static Document createEmptyPom() {
    DocumentBuilder docBuilder = createDocumentBuilder();
    Document document = docBuilder.newDocument();
    Element rootElement = document.createElement("project");

    document.appendChild(rootElement);

    return document;
  }

  private static DocumentBuilder createDocumentBuilder() {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Cannot create document builder", e);
    }
  }
}
