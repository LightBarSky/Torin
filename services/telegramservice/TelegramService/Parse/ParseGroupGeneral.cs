using System.Collections.Concurrent;
using System.Data.Common;
using System.IO.Compression;
using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;
using System.Text.RegularExpressions;
using Newtonsoft.Json;
using TelegramService.Infrastructure;
using TL;
using Contracts.Parse;
using WTelegram;
using DTOs.DTO;
using System.Net.Http.Json;
using TelegramService.Infrastructure.ClassesForWrite;
using Contracts.Infrastructure;
using TelegramService.Parse.Fillers;
using TelegramService.Parse;
using TelegramService.Contract;

namespace TelegramService;

internal class ParseGroupGeneral : IParseTelegram, IAsyncDisposable
{
    private ILogger _logger;
    private IWriters _writers;
    private IWorkWithGroups _workWithGroups;
    private IWorkWithTaskChats _workWithTaskChats;
    private IWorkWithParse _workWithParse;
    private readonly ParseTelegramCore _parseTelegramCore;
    private CancellationToken _cancellationToken => _parseTelegramCore.CancellationToken;
    private long _handlerId;
    private DateTime _restartPoint;
    private Random _random;
    private int? _delayRestartM;

    public long HandlerId => _handlerId;

    public ParseGroupGeneral(long handlerId, int? delayRestartM, ILogger logger, ParseTelegramCore parseTelegramCore)
    {
        _handlerId = handlerId;
        _random = new Random();
        _delayRestartM = delayRestartM;
        _restartPoint = DateTime.UtcNow.AddMinutes(_random.Next(1, 61));
        _logger = logger;
        _parseTelegramCore = parseTelegramCore;

        //======================================================================================================
        _writers = new Writers(_parseTelegramCore);
        _workWithGroups = new WorkWithGroups(_parseTelegramCore);
        _workWithTaskChats = new WorkWithTaskChats(_parseTelegramCore);
        _workWithParse = new WorkWithParse(_parseTelegramCore, _writers, _workWithTaskChats, _workWithGroups);
    }

    public async Task Start()
    {
        try
        {
            await _parseTelegramCore.PreStart();
            _parseTelegramCore.AddTask(_workWithGroups.LoaderGroup);
            _parseTelegramCore.AddTask(_workWithGroups.UpdateGroup);
            _parseTelegramCore.AddTask(_workWithParse.Parse);
            _parseTelegramCore.AddTask(Restart);
        }
        catch (Exception ex)
        {
            _logger.Log(new Log()
            {
                Message = "Func [StartParse]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                LogDT = DateTime.UtcNow,
                Level = Levels.FatalError
            });
            throw new Exception("Func [StartParse]" + "\nError: " + ex.Message + "\n" + ex.InnerException);
        }
    }

    public async Task Stop()
    {
        await DisposeAsync();
    }

    public ILogger GetLogger()
    {
        return _logger;
    }

    public async Task Restart()
    {
        try
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                if (DateTime.UtcNow - _restartPoint > TimeSpan.FromMinutes((int)_delayRestartM!))
                {
                    _logger.Log(new Log()
                    {
                        Message = "Начинаем restart...",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Warning,
                        Command = Commands.Restart
                    });
                    return;
                }
                await Task.Delay(5_000, cancellationToken: _cancellationToken);
            }
        }
        catch (Exception ex)
        {
            _logger.Log(new Log()
            {
                Message = "Func [Restart]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                LogDT = DateTime.UtcNow,
                Level = Levels.Error
            });
        }
    }

    public async ValueTask DisposeAsync()
    {
        _logger.Dispose();
        await _parseTelegramCore.DisposeAsync();
    }
}
