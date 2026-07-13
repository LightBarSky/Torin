using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;

namespace Contracts.Parse
{
    public interface ILogger
    {
        event EventHandler<Log>? LogEvent;
        void Log(Log log);
        long GetHandlerId();
        void Dispose();
    }
}