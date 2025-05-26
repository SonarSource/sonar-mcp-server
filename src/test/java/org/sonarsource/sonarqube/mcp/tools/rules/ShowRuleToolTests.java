/*
 * SonarQube MCP Server
 * Copyright (C) 2025 SonarSource
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonarsource.sonarqube.mcp.tools.rules;

import com.github.tomakehurst.wiremock.http.Body;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.sonarsource.sonarqube.mcp.harness.MockWebServer;
import org.sonarsource.sonarqube.mcp.harness.ReceivedRequest;
import org.sonarsource.sonarqube.mcp.harness.SonarQubeMcpServerTest;
import org.sonarsource.sonarqube.mcp.harness.SonarQubeMcpServerTestHarness;
import org.sonarsource.sonarqube.mcp.serverapi.rules.RulesApi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.sonarlint.core.serverapi.UrlUtils.urlEncode;

class ShowRuleToolTests {

  @Nested
  class MissingPrerequisite {

    @SonarQubeMcpServerTest
    void it_should_return_an_error_if_the_key_parameter_is_missing(SonarQubeMcpServerTestHarness harness) {
      var mcpClient = harness.newClient(Map.of("SONARQUBE_CLOUD_TOKEN", "token", "SONARQUBE_CLOUD_ORG", "org"));

      var result = mcpClient.callTool(new McpSchema.CallToolRequest(
        ShowRuleTool.TOOL_NAME,
        Map.of()));

      assertThat(result)
        .isEqualTo(new McpSchema.CallToolResult("An error occurred during the tool execution: Missing required argument: key", true));
    }

    @SonarQubeMcpServerTest
    void it_should_return_an_error_if_not_authenticated(SonarQubeMcpServerTestHarness harness) {
      var mcpClient = harness.newClient();

      var result = mcpClient.callTool(new McpSchema.CallToolRequest(
        ShowRuleTool.TOOL_NAME,
        Map.of("key", "java:S1541")));

      assertThat(result)
        .isEqualTo(new McpSchema.CallToolResult("Not connected to SonarQube Cloud, please provide 'SONARQUBE_CLOUD_TOKEN' and 'SONARQUBE_CLOUD_ORG'", true));
    }
  }

  @Nested
  class WithServer {

    private final MockWebServer mockServer = new MockWebServer();

    @BeforeEach
    void setup() {
      mockServer.start();
    }

    @AfterEach
    void teardown() {
      mockServer.stop();
    }

    @SonarQubeMcpServerTest
    void it_should_return_an_error_if_the_request_fails_due_to_token_permission(SonarQubeMcpServerTestHarness harness) {
      mockServer.stubFor(get(RulesApi.SHOW_PATH + "?key=" + urlEncode("java:S1541") + "&organization=org").willReturn(aResponse().withStatus(403)));
      var mcpClient = harness.newClient(Map.of(
        "SONARQUBE_CLOUD_URL", mockServer.baseUrl(),
        "SONARQUBE_CLOUD_TOKEN", "token",
        "SONARQUBE_CLOUD_ORG", "org"
      ));

      var result = mcpClient.callTool(new McpSchema.CallToolRequest(
        ShowRuleTool.TOOL_NAME,
        Map.of("key", "java:S1541")));

      assertThat(result)
        .isEqualTo(new McpSchema.CallToolResult("An error occurred during the tool execution: SonarQube answered with Forbidden", true));
    }

    @SonarQubeMcpServerTest
    void it_should_return_rule_details(SonarQubeMcpServerTestHarness harness) {
      mockServer.stubFor(get(RulesApi.SHOW_PATH + "?key=" + urlEncode("java:S1541") + "&organization=org")
        .willReturn(aResponse().withResponseBody(
          Body.fromJsonBytes("""
            {
                 "rule": {
                     "key": "java:S1541",
                     "repo": "java",
                     "name": "Methods should not be too complex",
                     "createdAt": "2023-08-10T13:36:48-0500",
                     "htmlDesc": "<h2>Why is this an issue?</h2>\\n<p>The cyclomatic complexity of methods should not exceed a defined threshold.</p>\\n<p>Complex code can perform poorly and will in any case be difficult to understand and therefore to maintain.</p>\\n<h3>Exceptions</h3>\\n<p>While having a large number of fields in a class may indicate that it should be split, this rule nonetheless ignores high complexity in\\n<code>equals</code> and <code>hashCode</code> methods.</p>",
                     "mdDesc": "<h2>Why is this an issue?</h2>\\n<p>The cyclomatic complexity of methods should not exceed a defined threshold.</p>\\n<p>Complex code can perform poorly and will in any case be difficult to understand and therefore to maintain.</p>\\n<h3>Exceptions</h3>\\n<p>While having a large number of fields in a class may indicate that it should be split, this rule nonetheless ignores high complexity in\\n<code>equals</code> and <code>hashCode</code> methods.</p>",
                     "severity": "CRITICAL",
                     "status": "READY",
                     "isTemplate": false,
                     "tags": [],
                     "sysTags": [
                         "brain-overload"
                     ],
                     "lang": "java",
                     "langName": "Java",
                     "params": [
                         {
                             "key": "Threshold",
                             "htmlDesc": "The maximum authorized complexity.",
                             "defaultValue": "10",
                             "type": "INTEGER"
                         }
                     ],
                     "defaultDebtRemFnType": "LINEAR_OFFSET",
                     "defaultDebtRemFnCoeff": "1min",
                     "defaultDebtRemFnOffset": "10min",
                     "effortToFixDescription": "per complexity point above the threshold",
                     "debtOverloaded": false,
                     "debtRemFnType": "LINEAR_OFFSET",
                     "debtRemFnCoeff": "1min",
                     "debtRemFnOffset": "10min",
                     "type": "CODE_SMELL",
                     "defaultRemFnType": "LINEAR_OFFSET",
                     "defaultRemFnGapMultiplier": "1min",
                     "defaultRemFnBaseEffort": "10min",
                     "remFnType": "LINEAR_OFFSET",
                     "remFnGapMultiplier": "1min",
                     "remFnBaseEffort": "10min",
                     "remFnOverloaded": false,
                     "gapDescription": "per complexity point above the threshold",
                     "scope": "MAIN",
                     "isExternal": false,
                     "descriptionSections": [
                         {
                             "key": "root_cause",
                             "content": "<p>The cyclomatic complexity of methods should not exceed a defined threshold.</p>\\n<p>Complex code can perform poorly and will in any case be difficult to understand and therefore to maintain.</p>\\n<h3>Exceptions</h3>\\n<p>While having a large number of fields in a class may indicate that it should be split, this rule nonetheless ignores high complexity in\\n<code>equals</code> and <code>hashCode</code> methods.</p>"
                         }
                     ],
                     "educationPrinciples": [],
                     "cleanCodeAttribute": "FOCUSED",
                     "cleanCodeAttributeCategory": "ADAPTABLE",
                     "impacts": [
                         {
                             "softwareQuality": "MAINTAINABILITY",
                             "severity": "HIGH"
                         }
                     ]
                 },
                 "actives": []
             }
            """.getBytes(StandardCharsets.UTF_8))
        )));
      var mcpClient = harness.newClient(Map.of(
        "SONARQUBE_CLOUD_URL", mockServer.baseUrl(),
        "SONARQUBE_CLOUD_TOKEN", "token",
        "SONARQUBE_CLOUD_ORG", "org"
      ));

      var result = mcpClient.callTool(new McpSchema.CallToolRequest(
        ShowRuleTool.TOOL_NAME,
        Map.of("key", "java:S1541")));

      assertThat(result)
        .isEqualTo(new McpSchema.CallToolResult("""
          Rule details:
          Key: java:S1541
          Name: Methods should not be too complex
          Severity: CRITICAL
          Type: CODE_SMELL
          Language: Java (java)
          Impacts:
          - MAINTAINABILITY: HIGH
          
          Description:
          <h2>Why is this an issue?</h2>
          <p>The cyclomatic complexity of methods should not exceed a defined threshold.</p>
          <p>Complex code can perform poorly and will in any case be difficult to understand and therefore to maintain.</p>
          <h3>Exceptions</h3>
          <p>While having a large number of fields in a class may indicate that it should be split, this rule nonetheless ignores high complexity in
          <code>equals</code> and <code>hashCode</code> methods.</p>""", false));
      assertThat(mockServer.getReceivedRequests())
        .containsExactly(new ReceivedRequest("Bearer token", ""));
    }
  }
} 
