# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

yu-ai-code-mother is an AI-powered code generation platform built with Spring Boot 3.5.4 + Java 21. It generates HTML pages, multi-file static sites, and Vue projects through natural language prompts using LangChain4j and LangGraph4j.

## Build & Run Commands

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CodeGenWorkflowTest

# Run single test method
mvn test -Dtest=CodeGenWorkflowTest#testMethodName
```

Application starts at `http://localhost:8123/api`. API docs available at `/api/doc.html` (Knife4j).

## Architecture

### Core Layers

```
controller/          - REST endpoints, SSE streaming responses
    └── AppController - Main code generation endpoint: GET /app/chat/gen/code
core/
    └── AiCodeGeneratorFacade - Unified entry point for code generation
    └── parser/      - Parse AI-generated code (HtmlCodeParser, MultiFileCodeParser)
    └── saver/       - Save parsed code to filesystem
    └── handler/     - Stream handlers (SimpleTextStreamHandler, JsonMessageStreamHandler)
ai/
    └── AiCodeGeneratorService - Interface for AI generation (HTML, MultiFile, VueProject)
    └── AiCodeGeneratorServiceFactory - Creates cached AI service instances with Redis memory
    └── AiCodeGenTypeRoutingService - Routes prompts to appropriate generation type
    └── tools/       - File operation tools (FileReadTool, FileWriteTool, FileModifyTool, etc.)
langgraph4j/
    └── CodeGenWorkflow - Orchestrates the generation pipeline
    └── node/        - Workflow nodes (ImageCollector, PromptEnhancer, Router, CodeGenerator, CodeQualityCheck, ProjectBuilder)
    └── tools/       - Image tools (ImageSearchTool, LogoGeneratorTool, MermaidDiagramTool, UndrawIllustrationTool)
    └── state/WorkflowContext - Shared state across workflow nodes
```

### Code Generation Types

| Type | Enum | Description |
|------|------|-------------|
| HTML | `CodeGenTypeEnum.HTML` | Single HTML file with embedded CSS/JS |
| Multi-file | `CodeGenTypeEnum.MULTI_FILE` | Static site with multiple HTML/CSS/JS files |
| Vue Project | `CodeGenTypeEnum.VUE_PROJECT` | Full Vue 3 project with tool calling capabilities |

### Workflow Pipeline (LangGraph4j)

```
START → ImageCollector → PromptEnhancer → Router → CodeGenerator → CodeQualityCheck → [ProjectBuilder] → END
                                                            ↓
                                        (quality check failed → back to CodeGenerator)
```

### AI Service Factory Pattern

`AiCodeGeneratorServiceFactory` creates AI services with:
- **Caffeine cache**: 1000 instances max, 30min write expiry, 10min access expiry
- **Redis chat memory**: Conversation history persisted per appId
- **Model selection**: `reasoningStreamingChatModel` for Vue projects, `openAiStreamingChatModel` for HTML/MultiFile

### Tool System

Tools implement `BaseTool` interface and are auto-registered via `ToolManager`:
- **File tools**: `FileReadTool`, `FileWriteTool`, `FileModifyTool`, `FileDeleteTool`, `FileDirReadTool`
- **Image tools**: `ImageSearchTool` (Pexels API), `LogoGeneratorTool` (DashScope), `MermaidDiagramTool`, `UndrawIllustrationTool`

### Stream Response Format

Vue project generation returns JSON messages via SSE:
- `AiResponseMessage` - Text content chunks
- `ToolRequestMessage` - Tool call initiated
- `ToolExecutedMessage` - Tool execution result

## Key Patterns

### Factory Pattern for AI Services
Services are created on-demand with appId-specific chat memory, allowing isolated conversations per application.

### Strategy Pattern for Code Parsing/Saving
`CodeParserExecutor` and `CodeFileSaverExecutor` dispatch to appropriate parsers/savers based on `CodeGenTypeEnum`.

### Prompt Templates
All system prompts stored in `src/main/resources/prompt/` and referenced via `@SystemMessage(fromResource = "...")`.

## Dependencies

- **LangChain4j 1.1.0-beta7**: AI service abstraction, tool calling
- **LangGraph4j 1.6.0-rc2**: Workflow orchestration
- **MyBatis-Flex 1.11.1**: Database ORM
- **Caffeine**: Local cache for AI service instances
- **MinIO**: Object storage for generated images
- **Selenium**: Webpage screenshots for preview
- **DashScope SDK**: Alibaba Cloud AI for image generation
