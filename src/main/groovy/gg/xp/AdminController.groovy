package gg.xp

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.views.View
import jakarta.inject.Inject

@Controller("/admin")
@ExecuteOn(TaskExecutors.VIRTUAL)
@CompileStatic
@Slf4j
class AdminController {

	@Inject
	SettingsService settingsService

	@Inject
	StreamElementsClient client

	@Inject
	TipService tipService

	@Inject
	EmbeddedServer embeddedServer

	@Get("/")
	@View("admin")
	Map<String, Object> index() {
		String ts = settingsService.getStartTimestamp()
		String et = settingsService.getEndTimestamp()

		return ([
				startTimestamp    : ts,
				startLocalDateTime: toLocalDateTime(ts),
				endTimestamp      : et,
				endLocalDateTime  : toLocalDateTime(et)
		] as Map<String, Object>)
	}

	private static String toLocalDateTime(String iso) {
		try {
			if (iso && iso.length() >= 16) {
				return iso.substring(0, 16)
			}
		}
		catch (Exception e) {
			log.warn("Failed to parse timestamp for display: {}", iso)
		}
		return ""
	}

	@Post("/save-token")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@View("fragments/admin-feedback")
	Map<String, Object> saveToken(String jwtToken) {
		log.info("Attempting to verify and save new JWT token...")
		try {
			if (!jwtToken) {
				return ([success: false, message: "JWT token is required."] as Map<String, Object>)
			}

			String authHeader = "Bearer ${jwtToken}"
			try {
				client.getMe(authHeader)
			}
			catch (Exception e) {
				log.error("Invalid JWT token test failed", e)
				return ([success: false, message: "Invalid JWT token: could not verify with StreamElements API."] as Map<String, Object>)
			}

			settingsService.saveJwt(jwtToken)
			tipService.refreshTips()

			return ([success: true, message: "JWT token successfully saved and verified!"] as Map<String, Object>)
		}
		catch (Exception e) {
			log.error("Error saving JWT token", e)
			return ([success: false, message: "Error saving JWT token: ${e.message}"] as Map<String, Object>)
		}
	}

	@Post("/save-time-range")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@View("fragments/admin-feedback")
	Map<String, Object> saveTimeRange(String startTimestamp, String endTimestamp) {
		log.info("Attempting to save time range... (Start: {}, End: {})", startTimestamp, endTimestamp)
		try {
			String formattedStart = formatTimestamp(startTimestamp)
			String formattedEnd = formatTimestamp(endTimestamp)

			if (formattedStart && !isValidIso(formattedStart)) {
				return ([success: false, message: "Invalid start date format."] as Map<String, Object>)
			}
			if (formattedEnd && !isValidIso(formattedEnd)) {
				return ([success: false, message: "Invalid end date format."] as Map<String, Object>)
			}

			settingsService.saveTimeRange(formattedStart, formattedEnd)
			tipService.refreshTips()

			return ([success: true, message: "Time range successfully saved! Tip list is being updated."] as Map<String, Object>)
		}
		catch (Exception e) {
			log.error("Error saving time range", e)
			return ([success: false, message: "Error saving time range: ${e.message}"] as Map<String, Object>)
		}
	}

	@Post("/refresh")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@View("fragments/admin-feedback")
	Map<String, Object> refresh() {
		log.info("Manual tip refresh requested")
		try {
			tipService.refreshTips()
			return ([success: true, message: "Tips refreshed successfully!"] as Map<String, Object>)
		}
		catch (Exception e) {
			log.error("Manual refresh failed", e)
			return ([success: false, message: "Refresh failed: ${e.message}"] as Map<String, Object>)
		}
	}

	@Post("/shutdown")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@View("fragments/admin-feedback")
	Map<String, Object> shutdown() {
		log.info("Shutdown requested")
		new Thread({
			Thread.sleep(1000)
			log.info("Exiting application...")
			System.exit(0)
		}).start()
		return ([success: true, message: "Server is shutting down..."] as Map<String, Object>)
	}

	private static String formatTimestamp(String input) {
		if (!input) return ""
		if (input.matches(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/)) {
			return input + ":00Z"
		}
		return input
	}

	private static boolean isValidIso(String iso) {
		return iso.matches(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/)
	}
}
