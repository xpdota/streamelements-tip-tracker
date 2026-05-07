package gg.xp

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.util.concurrent.CopyOnWriteArrayList

@Singleton
@CompileStatic
@Context
@Slf4j
class TipService {

	@Inject
	StreamElementsConfig config

	@Inject
	StreamElementsClient client

	private List<TipperSum> cachedTips = new CopyOnWriteArrayList<>()

	@Scheduled(fixedDelay = "1m", initialDelay = "5s")
	void refreshTips() {
		log.info("Refreshing tips from StreamElements (API Base: {}, Start: {})", config.apiBase, config.startTimestamp)
		try {
			cachedTips = fetchTopTippers()
			log.info("Successfully refreshed ${cachedTips.size()} tippers")
		}
		catch (Exception e) {
			log.error("Failed to refresh tips", e)
		}
	}

	List<TipperSum> getTopTippers() {
		return cachedTips
	}

	private List<TipperSum> fetchTopTippers() {
		String authHeader = "Bearer ${config.jwtToken}"
		def me = client.getMe(authHeader)

		List<StreamElementsClient.Tip> allTips = []
		int offset = 0
		int limit = 100

		while (true) {
			def response = client.getTips(authHeader, me._id, offset, limit, "createdAt", config.startTimestamp)
			if (!response.docs) {
				log.info("No more tips found at offset {}", offset)
				break
			}
			allTips.addAll(response.docs)
			log.info("Fetched {} tips, total so far: {}", response.docs.size(), allTips.size())
			offset += response.docs.size()
			if (offset >= response.total) {
				break
			}
		}

		Map<String, Double> sums = allTips.findAll { it.donation?.user?.username }
				.groupBy { it.donation.user.username }
				.collectEntries { username, tips ->
					[username, tips.sum { it.donation.amount } as Double]
				}

		return sums.collect { username, amount ->
			new TipperSum(username: username, amount: amount)
		}.sort { -it.amount }.take(10)
	}

	static class TipperSum {
		String username
		double amount
	}
}
