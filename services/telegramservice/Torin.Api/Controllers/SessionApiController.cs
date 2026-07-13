using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using TelegramService;
using Microsoft.AspNetCore.SignalR;
using QRCoder;
using Torin.Api.Services;
using DTOs.DTO;

namespace Torin.MVC.Controllers
{
    [ApiController]
    [Route("api/v1/session")]
    public class SessionApiController : ControllerBase
    {
        private readonly TelegramSessionManager telegramSessionManager;
        private readonly IWebHostEnvironment _env;
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly HttpClient _httpClient;

        public SessionApiController(TelegramSessionManager telegramSessionManager,
        IHttpClientFactory httpClientFactory, IWebHostEnvironment env)
        {
            this.telegramSessionManager = telegramSessionManager;
            _httpClientFactory = httpClientFactory;
            _httpClient = _httpClientFactory.CreateClient("guiService");
            _env = env;
        }

        [HttpPost("start")]
        public async Task<IActionResult> Start([FromBody] StartSessionModalDTO startModel, CancellationToken cancellationToken)
        {
            string? res = string.Empty;
            try
            {
                Func<string, string?> config = x =>
                {
                    return x switch
                    {
                        "api_id" => startModel.ApiId.ToString(),
                        "api_hash" => startModel.Hash,
                        "phone_number" => startModel.PhoneNumber,
                        "session_pathname" => Path.Combine(_env.ContentRootPath, "App_Data", "sessions",
                        startModel.PhoneNumber + ".session"),
                        "verification_code" => telegramSessionManager.AskConfig(x, cancellationToken).Result,
                        "password" => telegramSessionManager.AskConfig(x, cancellationToken).Result,
                        _ => null
                    };
                };

                telegramSessionManager.SetClient(new ClientBase(config));

                if (startModel.WithQR is not null && startModel.WithQR == "on")
                {
                    res = await telegramSessionManager.ClientBase!.LoginWithQR(async qrtext =>
                    {
                        using var qrGenerator = new QRCodeGenerator();
                        using var qrCodeData = qrGenerator.CreateQrCode(qrtext, QRCodeGenerator.ECCLevel.Q);
                        using var qrCode = new Base64QRCode(qrCodeData);
                        await _httpClient.PostAsJsonAsync<String>($"/api/v1/notify/show-qr", $"data:image/png;base64,{qrCode.GetGraphic(3)}");
                    }, cancellationToken);
                }
                else
                {
                    res = await telegramSessionManager.ClientBase!.Login();
                }
            }
            catch (Exception ex)
            {
                await telegramSessionManager.CleanAsync();
                return BadRequest(ex.Message);
            }
            return Ok($"Вы вошли как {res}");
        }

        [HttpPost("input")]
        public IActionResult Input([FromBody] InputSessionModalDTO inputModel)
        {
            var success = telegramSessionManager.ProvideValue(inputModel.Type, inputModel.Value);
            return success ? Ok() : BadRequest("No input requested");
        }

        [HttpPost("stop")]
        public async Task<IActionResult> Stop()
        {
            await telegramSessionManager.CleanAsync();
            return Ok();
        }
    }
}