using DTOs.DTO;
using Microsoft.AspNetCore.Mvc;
using Torin.Api.Infrastructure;
using Torin.Api.Services;

namespace Torin.Api.Controllers
{
    [ApiController]
    [Route("api/v1/handlers")]
    public class HandlerApiController : ControllerBase
    {
        private readonly IHandlerStatusService _statusService;
        private readonly IWebHostEnvironment _env;
        private readonly IConfiguration _configuration;
        private readonly string _sessionsPath;

        public HandlerApiController(IHandlerStatusService statusService, IWebHostEnvironment env, IConfiguration configuration)
        {
            _statusService = statusService;
            _env = env;
            _configuration = configuration;
            _sessionsPath = _configuration["Paths:Sessions"] ?? Path.Combine(_env.ContentRootPath, "AppData", "sessions");
        }

        [HttpPost("{id}/start")]
        public async Task<IActionResult> StartHandler(long id)
        {
            try
            {
                await _statusService.SetStatusAsync(id, Status.STARTED);
                return Ok();
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost("{id}/stop")]
        public async Task<IActionResult> StopHandler(long id)
        {
            try
            {
                await _statusService.SetStatusAsync(id, Status.STOPPED);
                return Ok();
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("sessions-all")]
        public IActionResult SessionsAll()
        {
            var sessionsPath = _sessionsPath;
            var files = Directory
                .GetFiles(sessionsPath)
                .ToList();
            
            var fileItems = files
                .Select(f => new SessionsListDTO
                {
                    Text = Path.GetFileNameWithoutExtension(f),
                    Value = Path.GetFileNameWithoutExtension(f),
                    LastModified = System.IO.File.GetLastWriteTimeUtc(f).ToString("yyyy-MM-dd HH:mm:ss")
                })
                .Where(x => !string.IsNullOrEmpty(x.Value) && x.Value.All(char.IsDigit))
                .ToList();

            return Ok(fileItems);
        }

        [HttpGet("running")]
        public IActionResult Running()
        {
            List<long> results = _statusService.GetRunning();

            return Ok(results);
        }
    }
}