using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Json;
using System.Threading.Tasks;
using DTOs.DTO;

namespace TelegramService.Parse
{
    public class ApiClient
    {
        private readonly HttpClient _http;

        public ApiClient(HttpClient httpClient)
        {
            _http = httpClient;
        }

        //======================== WordGroupAll ===================================
        public async Task PatchGroupRecalculate(WordGroupAllDTO dto)
        {
            await SendAsync(HttpMethod.Patch, $"api/v1/word-group-all/{dto.Id}/recalculate-hand-sr-dp", dto);
        }

        public async Task PatchGroupLastHandle(long id, DateTime lastHandle)
        {
            await SendAsync(HttpMethod.Patch, $"api/v1/word-group-all/{id}/last-handle",
            new PatchLastHandleRequestDTO { LastHandle = lastHandle.ToUniversalTime() });
        }

        public async Task<WordGroupAllDTO?> GetGroupByIdGroup(long idGroup)
        {
            return await SendAsync<WordGroupAllDTO>(HttpMethod.Get, $"api/v1/word-group-all/by-id-group/{idGroup}");
        }

        public async Task<List<WordGroupAllDTO>?> GetGroupBatch(long handlerId, long offsetId, int limit)
        {
            return await SendAsync<List<WordGroupAllDTO>>(HttpMethod.Get, $"api/v1/word-group-all/batch?handlersId={handlerId}&offsetId={offsetId}&limit={limit}");
        }

        public async Task<WordGroupAllDTO?> GetGroupByIdGroupAndHandlersId(long idGroup, long handlersId)
        {
            return await SendAsync<WordGroupAllDTO>(HttpMethod.Get, $"api/v1/word-group-all/by-id-group/{idGroup}/by-handlers-id/{handlersId}");
        }

        public async Task<WordGroupAllDTO?> GetGroupByHashAndHandlersId(string hash, long handlersId)
        {
            return await SendAsync<WordGroupAllDTO>(HttpMethod.Get, $"api/v1/word-group-all/by-hash/{Uri.EscapeDataString(hash)}/by-handlers-id/{handlersId}");
        }

        public async Task DeleteGroupById(long id)
        {
            await SendAsync(HttpMethod.Delete, $"api/v1/word-group-all/{id}");
        }

        public async Task<WordGroupAllDTO?> PutGroup(long id, WordGroupAllDTO dto)
        {
            return await SendAsync<WordGroupAllDTO>(HttpMethod.Put, $"api/v1/word-group-all/{id}", dto);
        }

        public async Task PostGroup(WordGroupAllDTO dto)
        {
            await SendAsync(HttpMethod.Post, $"api/v1/word-group-all", dto);
        }

        //======================= Task Chats =======================================
        public async Task<TaskChatDTO?> GetTaskChatByIdChat(long idChat)
        {
            return await SendAsync<TaskChatDTO>(HttpMethod.Get, $"api/v1/task-chats/{idChat}");
        }

        public async Task PostTaskChat(TaskChatDTO dto)
        {
            await SendAsync(HttpMethod.Post, $"api/v1/task-chats", dto);
        }

        public async Task<TaskChatDTO?> PutTaskChat(long id, TaskChatDTO dto)
        {
            return await SendAsync<TaskChatDTO>(HttpMethod.Put, $"api/v1/task-chats/{id}", dto);
        }

        public async Task PatchTaskChatsParseUser(long idChat, DateTime parseUser)
        {
            await SendAsync(HttpMethod.Patch, $"api/v1/task-chats/{idChat}/date-parse-user",
            new PatchParseUserDTO { ParseUser = parseUser.ToUniversalTime() });
        }

        //======================= Handlers =======================================
        public async Task<List<long>?> GetHandlersIdsByCategory(string category)
        {
            return await SendAsync<List<long>>(HttpMethod.Get, $"api/v1/handlers/by-category?category={Uri.EscapeDataString(category)}");
        }

        public async Task<HandlerDTO?> GetHandlersById(long id)
        {
            return await SendAsync<HandlerDTO>(HttpMethod.Get, $"api/v1/handlers/{id}");
        }


        private async Task SendAsync(HttpMethod method, string url, object? body = null)
        {
            var request = new HttpRequestMessage(method, url);

            if (body != null)
                request.Content = JsonContent.Create(body);

            var response = await _http.SendAsync(request);

            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Error [ApiClient] {response.StatusCode}: {error}");
            }
        }

        private async Task<T?> SendAsync<T>(HttpMethod method, string url, object? body = null)
        {
            var request = new HttpRequestMessage(method, url);

            if (body != null)
                request.Content = JsonContent.Create(body);

            var response = await _http.SendAsync(request);

            if (!response.IsSuccessStatusCode)
            {
                if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
                {
                    return default;
                }
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Error [ApiClient] {response.StatusCode}: {error}");
            }

            if (response.Content.Headers.ContentLength > 0)
            {
                return await response.Content.ReadFromJsonAsync<T>();
            }

            return default;
        }
    }
}