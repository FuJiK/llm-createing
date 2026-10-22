namespace LlmCreating.Web.Models;

public sealed record ChatMessageDto(string Role, string Content);

public sealed record ChatRequest(
    string? Prompt,
    IReadOnlyList<ChatMessageDto>? Messages,
    string? Model);

public sealed record ChatResponse(string Content, string Model);

public sealed record HealthResponse(bool OllamaReachable, string BaseUrl, string DefaultModel);
