using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using DTOs.DTO;
using Microsoft.AspNetCore.SignalR;
using TelegramService;

namespace Torin.Api.Services
{
    public class TelegramSessionManager
    {
        private readonly ConcurrentDictionary<string, TaskCompletionSource<string>> _waiters = new();
        private ClientBase? _clientBase = null;
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly HttpClient _httpClient;

        public TelegramSessionManager(IHttpClientFactory httpClientFactory)
        {
            _httpClientFactory = httpClientFactory;
            _httpClient = _httpClientFactory.CreateClient("guiService");
        }

        public async Task<string?> AskConfig(string request, CancellationToken ct = default)
        {
            await _httpClient.PostAsJsonAsync($"/api/v1/notify/session-request", new InputSessionModalDTO()
            {
                Type = request,
                Value = ""
            });

            TaskCompletionSource<string> tcs;

            if (!_waiters.TryGetValue(request, out tcs!))
            {
                tcs = new TaskCompletionSource<string>(
                    TaskCreationOptions.RunContinuationsAsynchronously);
                _waiters[request] = tcs;
            }

            using (ct.Register(() => tcs.TrySetCanceled(ct)))
            {
                return await tcs.Task.ConfigureAwait(false);
            }
        }

        public bool ProvideValue(string type, string value)
        {
            if (_waiters.TryGetValue(type, out var tcs))
            {
                tcs.SetResult(value);
                _waiters.Remove(type, out _);
                return true;
            }
            else
            {
                return false;
            }
        }

        public void SetClient(ClientBase? clientBase)
        {
            _clientBase = clientBase;
        }

        public async Task CleanAsync()
        {
            if (_clientBase is not null)
                await _clientBase.DisposeAsync();
            _waiters.Clear();
        }

        public ClientBase? ClientBase => _clientBase;
    }
}