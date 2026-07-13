using OpenTelemetry.Exporter;
using OpenTelemetry.Metrics;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Serilog;
using TelegramService.Setting;
using Torin.Api.Services;
using Torin.Api.Settings;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<KafkaTopicsOptions>(
    builder.Configuration.GetSection("Kafka"));
builder.Services
    .AddOptions<ParseGroupOptions>()
    .Bind(builder.Configuration.GetSection("Parse"))
    .ValidateDataAnnotations()
    .ValidateOnStart();

builder.Host.UseSerilog((context, services, configuration) =>
{
    configuration
    .ReadFrom.Configuration(context.Configuration)
    .ReadFrom.Services(services);
});
builder.Services.AddHealthChecks();
builder.Services.AddControllers();
builder.Services.AddSingleton<IHandlerStatusService, HandlerStatusService>();
builder.Services.AddSingleton<KafkaProducerService>();
builder.Services.AddSingleton<TelegramSessionManager>();
builder.Services.AddHostedService<HeartBeatService>();
builder.Services.AddHttpClient("dbService", client =>
{
    client.BaseAddress = new System.Uri(builder.Configuration["dbService:BaseUrl"]!);
    client.Timeout = TimeSpan.FromSeconds(30);
});
builder.Services.AddHttpClient("guiService", client =>
{
    client.BaseAddress = new System.Uri(builder.Configuration["guiService:BaseUrl"]!);
    client.Timeout = TimeSpan.FromSeconds(30);
});

builder.Services.AddOpenTelemetry()
    .ConfigureResource(resource => resource
        .AddService(serviceName: builder.Environment.ApplicationName))
    // .WithTracing(tracing => tracing
    //     .AddAspNetCoreInstrumentation()
    //     .AddHttpClientInstrumentation())
    .WithMetrics(metrics => metrics
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddRuntimeInstrumentation()
        .AddProcessInstrumentation()
        .AddOtlpExporter((exporterOptions, metricReaderOptions) =>
        {
            exporterOptions.Endpoint = new Uri(builder.Configuration["metrics:EndPoint"]!);
            exporterOptions.Protocol = OtlpExportProtocol.HttpProtobuf;
            metricReaderOptions.PeriodicExportingMetricReaderOptions.ExportIntervalMilliseconds = 1000;
        }));

var app = builder.Build();

app.UseRouting();

app.UseAuthorization();

app.MapGet("/", () => Results.Ok("API is running..."));
app.MapHealthChecks("/health");
app.MapControllers();

app.Run();
