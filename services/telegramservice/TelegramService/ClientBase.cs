using System.Threading.Tasks;
using Contracts.Infrastructure;
using TL;

namespace TelegramService;

public interface ICreateSession
{
    Task<string?> Login();
    Task<string?> LoginWithQR(Action<string> qrGenerator, CancellationToken cancellationToken);
}

public class ClientBase : ICreateSession, IAsyncDisposable
{
    private Func<string, string?> _config;
    private WTelegram.Client? _client = null;
    private TL.User? _user;
    private CancellationTokenSource _cancellationToken;

    public TL.User? User => _user;
    public event EventHandler<Log>? ClientLogEvent;

    public ClientBase(Func<string, string?> config)
    {
        _config = config;
        _cancellationToken = new CancellationTokenSource();
    }

    protected virtual void OnClientLogEvent(Log e)
    {
        var event_temp = ClientLogEvent;
        if (event_temp != null)
        {
            event_temp(this, e);
        }
    }

    private async Task Client_OnOther(IObject arg)
    {
        if (arg is ReactorError reactorError)
        {
            OnClientLogEvent(new Log()
            {
                Message = $"Fatal reactor error: {reactorError.Exception.Message}",
                LogDT = DateTime.UtcNow,
                Level = Levels.Error
            });
            while (!_cancellationToken.IsCancellationRequested)
            {
                if (_client != null) await _client.DisposeAsync();
                _client = null;
                await Task.Delay(5000);

                try
                {
                    await Login();
                    break;
                }
                catch (Exception ex) when (ex is not ObjectDisposedException)
                {
                    OnClientLogEvent(new Log()
                    {
                        Message = $"Connection still failing: {ex.Message}",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                }
            }
        }
    }

    private async Task CreateClient()
    {
        if (_client is not null)
        {
            await DisposeAsync();
        }
        WTelegram.Helpers.Log += (lvl, str) => { }; //надо придумать механизм сохранения в отдельный топик
        _client = new WTelegram.Client(_config)
        {
            MaxAutoReconnects = 10_000,
            PingInterval = 900
        };
        _client.OnOther += Client_OnOther;
    }

    public async Task<string?> Login()
    {
        await CreateClient();

        try
        {
            _user = await _client!.LoginUserIfNeeded();
        }
        catch (ObjectDisposedException disposeEx)
        {
            throw new ObjectDisposedException(disposeEx.ObjectName);
        }
        catch (Exception ex)
        {
            throw new Exception(message: $"Ошибка при авторизации: {ex.Message}", innerException: ex.InnerException);
        }
        return _user?.first_name + " " + _user?.last_name;
    }

    public async Task<string?> LoginWithQR(Action<string> qrGenerator, CancellationToken cancellationToken = default)
    {
        await CreateClient();

        try
        {
            _user = await _client!.LoginWithQRCode(qrGenerator, ct: cancellationToken);
        }
        catch (Exception ex)
        {
            throw new Exception(message: $"Ошибка при авторизации: {ex.Message}", innerException: ex.InnerException);
        }
        return _user?.first_name + " " + _user?.last_name;
    }

    public async Task<Payments_SavedStarGifts> GetPayments_SavedStarGifts(ChatBase chatBase, string hash)
    {
        return await _client.Payments_GetSavedStarGifts(chatBase, hash);
    }

    public async Task<Messages_MessagesBase> GetMessages_History(ChatBase chat, int offset_id)
    {
        return await _client.Messages_GetHistory(peer: chat, offset_id: offset_id);
    }

    public async Task<UpdatesBase> Channels_JoinChannel(InputChannel inputChannel)
    {
        return await _client.Channels_JoinChannel(inputChannel);
    }

    public async Task<Messages_ChatFull> GetFullChat(InputChannel inputChannel)
    {
        return await _client!.GetFullChat(inputChannel);
    }

    public async Task<Contacts_ResolvedPeer> Contacts_ResolveUsername(string username)
    {
        return await _client.Contacts_ResolveUsername(username);
    }

    public async Task<ChatInviteBase> Messages_CheckChatInvite(string hash)
    {
        return await _client.Messages_CheckChatInvite(hash);
    }

    public async Task<UpdatesBase> Messages_ImportChatInvite(string hash)
    {
        return await _client.Messages_ImportChatInvite(hash);
    }

    public async Task<Users_UserFull> GetFullUser(InputUserBase inputUserBase)
    {
        return await _client.Users_GetFullUser(inputUserBase);
    }

    public async Task<Messages_MessagesBase> GetReplies(ChatBase peer, int msg_id, int offset_id)
    {
        return await _client.Messages_GetReplies(peer: peer, msg_id: msg_id, offset_id: offset_id);
    }

    public async Task<Channels_ChannelParticipants> GetAllParticipants(InputChannelBase channel, CancellationToken cancellationToken)
    {
        return await _client!.Channels_GetAllParticipants(channel: channel, cancellationToken: cancellationToken);
    }

    public async ValueTask DisposeAsync()
    {
        _cancellationToken.Cancel();
        if (_client is not null)
        {
            await _client.DisposeAsync();
            _client = null;
        }
    }
}
