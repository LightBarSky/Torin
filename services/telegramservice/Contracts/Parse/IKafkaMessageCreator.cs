using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Contracts.Parse
{
    public interface IKafkaMessageCreator
    {
        (string type, string key, string mode, string serializeObjects) CreateQuery();
    }
}