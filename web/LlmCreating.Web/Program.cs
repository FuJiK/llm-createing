using LlmCreating.Web.Models;
using LlmCreating.Web.Options;
using LlmCreating.Web.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<OllamaOptions>(builder.Configuration.GetSection(OllamaOptions.SectionName));
builder.Services.AddHttpClient<OllamaClient>((sp, client) =>
{
    var options = sp.GetRequiredService<Microsoft.Extensions.Options.IOptions<OllamaOptions>>().Value;
    client.BaseAddress = new Uri(options.BaseUrl.TrimEnd('/') + "/");
    client.Timeout = TimeSpan.FromSeconds(options.TimeoutSeconds);
});

var app = builder.Build();

app.UseDefaultFiles();
app.UseStaticFiles();

app.MapGet("/api/health", async (OllamaClient ollama, Microsoft.Extensions.Options.IOptions<OllamaOptions> options) =>
{
    var reachable = await ollama.IsHealthyAsync(CancellationToken.None);
    return Results.Ok(new HealthResponse(reachable, options.Value.BaseUrl, options.Value.DefaultModel));
});

app.MapPost("/api/chat", async (ChatRequest request, OllamaClient ollama, Microsoft.Extensions.Options.IOptions<OllamaOptions> options, CancellationToken cancellationToken) =>
{
    var model = string.IsNullOrWhiteSpace(request.Model)
        ? options.Value.DefaultModel
        : request.Model.Trim();

    IReadOnlyList<ChatMessageDto> messages;
    if (request.Messages is { Count: > 0 } history)
    {
        messages = history;
    }
    else if (!string.IsNullOrWhiteSpace(request.Prompt))
    {
        messages = [new ChatMessageDto("user", request.Prompt.Trim())];
    }
    else
    {
        return Results.BadRequest(new { error = "prompt or messages is required." });
    }

    try
    {
        var content = await ollama.ChatAsync(model, messages, cancellationToken);
        return Results.Ok(new ChatResponse(content, model));
    }
    catch (HttpRequestException ex)
    {
        return Results.Problem(
            detail: "Ollama に接続できません。Docker で Ollama が起動しているか確認してください。",
            statusCode: StatusCodes.Status502BadGateway,
            title: ex.Message);
    }
    catch (TaskCanceledException)
    {
        return Results.Problem(
            detail: "Ollama 応答がタイムアウトしました。",
            statusCode: StatusCodes.Status504GatewayTimeout);
    }
});

app.Run();
