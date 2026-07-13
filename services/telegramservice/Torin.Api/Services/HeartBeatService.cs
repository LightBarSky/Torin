using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.ConstrainedExecution;
using System.Threading.Tasks;
using DTOs.DTO;
using Newtonsoft.Json;

namespace Torin.Api.Services
{
    public class HeartBeatService : BackgroundService
    {
        private readonly KafkaProducerService kafkaProducerService;
        private readonly IConfiguration configuration;
        private readonly string topicName;
        private readonly string serviceName;

        public HeartBeatService(KafkaProducerService kafkaProducerService, IConfiguration configuration)
        {
            this.kafkaProducerService = kafkaProducerService;
            this.configuration = configuration;
            topicName = configuration["heartbeat:topicName"]!;
            serviceName = configuration["heartbeat:serviceName"]!;
        }
        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                await kafkaProducerService.ProduceAsync(topicName, serviceName, JsonConvert.SerializeObject(new HeartBeatDTO()
                {
                    ServiceName = serviceName,
                    Timestamp = DateTime.UtcNow
                }));
                await Task.Delay(3000);
            }
        }
    }
}