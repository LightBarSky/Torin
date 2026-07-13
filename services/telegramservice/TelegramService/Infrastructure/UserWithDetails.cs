using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace TelegramService.Infrastructure
{
    public record UserAndFullUser(TL.User User, TL.UserFull? FullUser);
}