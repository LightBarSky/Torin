using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Confluent.Kafka;
using Newtonsoft.Json;
using Serilog;
using DTOs.DTO;

namespace Torin.Api.Services
{
    public class KafkaProducerService
    {
        private readonly IProducer<string, string> _producer;

        public KafkaProducerService(IConfiguration configuration)
        {
            var bootstrapServers = configuration["Kafka:BootstrapServers"] ?? "localhost:9092";
            var _producerConfig = new ProducerConfig()
            {
                BootstrapServers = bootstrapServers,
                Acks = Acks.All,
                EnableIdempotence = true,
                LingerMs = 5,
                BatchSize = 32 * 1024,
                MessageSendMaxRetries = 3
            };

            _producer = new ProducerBuilder<string, string>(_producerConfig)
            .SetErrorHandler((_, err) =>
            {
                Log.Error($"Producer error: {err}");
            }).Build();
        }

        public async Task ProduceAsync(string topic, string key, string message)
        {
            try
            {
                var result = await _producer.ProduceAsync(topic, new Message<string, string>
                {
                    Key = key,
                    Value = message
                });

                Log.Information($"Delivered to {result.TopicPartitionOffset}");
            }
            catch (ProduceException<string, string> ex)
            {
                Log.Error($"Produce failed: {ex.Error.Reason}");
            }
        }
    }
}