using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;
using Contracts.Parse;
using DTOs.DTO;
using TelegramService.Infrastructure;

namespace TelegramService.Parse
{
    interface IWorkWithTaskChats
    {
        Task<long> GetNewOffsetMessage(long chatId);
        Task<long> GetOldOffsetMessage(long chatId);
    }
    internal class WorkWithTaskChats: IWorkWithTaskChats
    {
        private readonly ParseTelegramCore _parseTelegramCore;
        private ApiClient _api => _parseTelegramCore.ApiClient;
        private ILogger _logger => _parseTelegramCore.Logger;

        public WorkWithTaskChats(ParseTelegramCore parseTelegramCore)
        {
            _parseTelegramCore = parseTelegramCore;
        }

        public async Task<long> GetNewOffsetMessage(long chatId)
        {
            long result = -1;
            try
            {
                var task_ch = await _api.GetTaskChatByIdChat(chatId);
                result = task_ch?.OffsetIdNewMessage ?? -1;
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = "Func [NewOffsetMessage]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
            return result;
        }

        public async Task<long> GetOldOffsetMessage(long chatId)
        {
            long result = -1;
            try
            {
                var task_ch = await _api.GetTaskChatByIdChat(chatId);
                result = task_ch?.OffsetIdOldMessage ?? -1;
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = "Func [OldOffsetMessage]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
            return result;
        }
    }
}