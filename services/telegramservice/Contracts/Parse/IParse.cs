using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;

namespace Contracts.Parse
{
    public interface IParse
    {
        Task Start();
        Task Stop();
        Task Restart();
        ILogger GetLogger();
    }
}