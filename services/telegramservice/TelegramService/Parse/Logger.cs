using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;
using Contracts.Parse;

namespace TelegramService.Parse
{
    public class Logger : ILogger
    {
        private long _handlerId;
        public event EventHandler<Log>? LogEvent;

        public Logger(long handlerId)
        {
            _handlerId = handlerId;
            LogEvent += FatalErrorLog;
        }

        public void Log(Log log)
        {
            var event_temp = LogEvent;
            if (event_temp != null)
            {
                event_temp(this, log);
            }
        }

        public long GetHandlerId()
        {
            return _handlerId;
        }

        private void FatalErrorLog(object? sender, Log? log)
        {
            if (log != null && log.Level == Levels.Error)
            {
                if (log.Message.Contains("USER_DEACTIVATED_BAN"))
                {
                    Log(new Log()
                    {
                        Message = $"USER_DEACTIVATED_BAN",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.FatalError
                    });
                }
                else if (log.Message.Contains("USER_DEACTIVATED"))
                {
                    Log(new Log()
                    {
                        Message = $"USER_DEACTIVATED",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.FatalError
                    });
                }
                else if (log.Message.Contains("PHONE_NUMBER_BANNED"))
                {
                    Log(new Log()
                    {
                        Message = $"PHONE_NUMBER_BANNED",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.FatalError
                    });
                }
                else if (log.Message.Contains("verification_code"))
                {
                    Log(new Log()
                    {
                        Message = $"verification_code",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.FatalError
                    });
                }
                else if (log.Message.Contains("AUTH_KEY_UNREGISTERED"))
                {
                    Log(new Log()
                    {
                        Message = $"AUTH_KEY_UNREGISTERED",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.FatalError
                    });
                }
                else if (log.Message.Contains("FROZEN_METHOD_INVALID"))
                {
                    Log(new Log()
                    {
                        Message = $"FROZEN_METHOD_INVALID",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.FatalError
                    });
                }
            }
        }

        public void Dispose()
        {
            LogEvent -= FatalErrorLog;
        }
    }
}