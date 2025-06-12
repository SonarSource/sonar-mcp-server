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
package org.sonarsource.sonarqube.mcp.serverapi.system.response;

import java.util.List;
import javax.annotation.Nullable;

public record HealthResponse(String health, @Nullable List<Cause> causes, @Nullable List<Node> nodes) {

  public record Cause(String message) {
  }

  public record Node(String name, String type, String host, int port, String startedAt, String health, @Nullable List<Cause> causes) {
  }

}
