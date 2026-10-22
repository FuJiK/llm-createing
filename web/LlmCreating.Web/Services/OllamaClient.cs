using System.Net.Http.Json;
using System.Text.Json.Serialization;
using LlmCreating.Web.Models;
using LlmCreating.Web.Options;
using Microsoft.Extensions.Options;

namespace LlmCreating.Web.Services;

public sealed class OllamaClient
{
    private readonly HttpClient _http;
    private readonly OllamaOptions _options;

    public OllamaClient(HttpClient http, IOptions<OllamaOptions> options)
    {
        _http = http;
        _options = options.Value;
    }

    public async Task<bool> IsHealthyAsync(CancellationToken cancellationToken)
    {
        try
        {
            using var response = await _http.GetAsync("/api/tags", cancellationToken);
            return response.IsSuccessStatusCode;
        }
        catch (HttpRequestException)
        {
            return false;
        }
        catch (TaskCanceledException)
        {
            return false;
        }
    }

    public async Task<string> ChatAsync(
        string model,
        IReadOnlyList<ChatMessageDto> messages,
        CancellationToken cancellationToken)
    {
        var ollamaMessages = messages
            .Select(m => new OllamaMessage(m.Role, m.Content))
            .ToList();
        var payload = new OllamaChatRequest(model, ollamaMessages, Stream: false);
        using var response = await _http.PostAsJsonAsync("/api/chat", payload, cancellationToken);
        response.EnsureSuccessStatusCode();

        var body = await response.Content.ReadFromJsonAsync<OllamaChatResponse>(cancellationToken);
        if (body?.Message?.Content is not { Length: > 0 } content)
        {
            throw new InvalidOperationException("Ollama returned an empty response.");
        }

        return content;
    }
}

internal sealed record OllamaMessage(
    [property: JsonPropertyName("role")] string Role,
    [property: JsonPropertyName("content")] string Content);

internal sealed record OllamaChatRequest(
    [property: JsonPropertyName("model")] string Model,
    [property: JsonPropertyName("messages")] IReadOnlyList<OllamaMessage> Messages,
    [property: JsonPropertyName("stream")] bool Stream);

internal sealed record OllamaChatResponse(
    [property: JsonPropertyName("message")] OllamaMessage? Message);
