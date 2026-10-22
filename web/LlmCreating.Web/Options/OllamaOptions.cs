namespace LlmCreating.Web.Options;

public sealed class OllamaOptions
{
    public const string SectionName = "Ollama";

    public string BaseUrl { get; set; } = "http://localhost:11434";

    public string DefaultModel { get; set; } = "llama3.2:1b";

    public int TimeoutSeconds { get; set; } = 300;
}
