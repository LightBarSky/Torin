using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Contracts.Infrastructure
{
    public enum Levels
    {
        Info,
        Warning,
        Error,
        FatalError
    }

    public enum Commands
    {
        Start,
        Stop,
        Restart
    }

    public class Log
    {
        public required string Message { get; set; } = string.Empty;
        public required DateTime LogDT { get; set; }
        public required Levels Level { get; set; } = Levels.Info;
        public Commands? Command { get; set; }
    }
}