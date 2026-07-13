package com.torin.prod.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import com.torin.prod.dto.HandlerDto;
import com.torin.prod.service.*;

import reactor.util.function.Tuple2;

@Controller
public class HomeController {
    @Value("${api.db.url}")
    private String apiUrl;
    @Value("${api.telegram.url}")
    private String apiTelegramUrl;
    @Value("${api.base.url}")
    private String apiBaseUrl;
    @Value("${key-for-logs-to-listener}")
    private String keyForLogsToListener;
    @Value("${services.nodes.dbService.key}")
    private String dbServiceKey;
    private final ServiceStatusHolder serviceStatusHolder;
    private final HomeService homeService;

    public HomeController(ServiceHealthChecker serviceHealthChecker, ServiceStatusHolder serviceStatusHolder,
            WebClient client, HomeService homeService,
            CacheService cacheService) {
        this.serviceStatusHolder = serviceStatusHolder;
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Tuple2<List<HandlerDto>, Integer> handlersTuple = homeService.getHandlers().block();
        Boolean isRunningListener = homeService.getRunningListener().block();

        List<HandlerDto> handlers = handlersTuple.getT1();
        Integer runningCount = handlersTuple.getT2();
        model.addAttribute("handlers", handlers);
        model.addAttribute("handlersCount", handlers.size());
        model.addAttribute("runningCount", runningCount);
        model.addAttribute("runningListener", isRunningListener);
        model.addAttribute("dbService", serviceStatusHolder.isAvailability(dbServiceKey));
        model.addAttribute("keyForLogsListener", keyForLogsToListener);

        return "home";
    }

    @GetMapping("/logs")
    public String logs(@RequestParam(required = false) String handlerId,
            @RequestParam(required = false) String filter, Model model) {
        model.addAttribute("handlerId", handlerId);
        model.addAttribute("filter", filter);
        model.addAttribute("title", handlerId != null && filter != null
                ? handlerId + "_" + filter
                : handlerId != null ? handlerId : filter);
        return "log";
    }
}
