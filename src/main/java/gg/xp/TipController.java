package gg.xp;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@ExecuteOn(TaskExecutors.VIRTUAL)
public class TipController {

    @Inject
    StreamElementsConfig config;

    @Inject
    StreamElementsClient client;

    @Get("/")
    @View("index")
    public Map<String, Object> index() {
        Map<String, Object> model = new HashMap<>();
        model.put("tips", getTopTippers());
        return model;
    }

    @Get("/tips/table")
    @View("fragments/tips-table")
    public Map<String, Object> tipsTable() {
        Map<String, Object> model = new HashMap<>();
        model.put("tips", getTopTippers());
        return model;
    }

    private List<TipperSum> getTopTippers() {
        String authHeader = "Bearer " + config.jwtToken();
        StreamElementsClient.ChannelMe me = client.getMe(authHeader);
        
        List<StreamElementsClient.Tip> allTips = new ArrayList<>();
        int offset = 0;
        int limit = 100;
        
        while (true) {
            StreamElementsClient.TipsResponse response = client.getTips(authHeader, me._id(), offset, limit, "createdAt", config.startTimestamp());
            if (response.docs() == null || response.docs().isEmpty()) {
                break;
            }
            allTips.addAll(response.docs());
            offset += response.docs().size();
            if (offset >= response.total()) {
                break;
            }
        }

        Map<String, Double> sums = allTips.stream()
                .filter(tip -> tip.donation() != null && tip.donation().user() != null && tip.donation().user().username() != null)
                .collect(Collectors.groupingBy(
                        tip -> tip.donation().user().username(),
                        Collectors.summingDouble(tip -> tip.donation().amount())
                ));

        return sums.entrySet().stream()
                .map(e -> new TipperSum(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(TipperSum::amount).reversed())
                .collect(Collectors.toList());
    }

    public record TipperSum(String username, double amount) {}
}
