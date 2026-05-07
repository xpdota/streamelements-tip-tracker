package gg.xp

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable
import jakarta.annotation.Nullable

@CompileStatic
@Client('${streamelements.api-base}')
interface StreamElementsClient {

	@Get('/kappa/v2/channels/me')
	ChannelMe getMe(@Header('Authorization') String authHeader)

	@Get('/kappa/v2/tips/{channelId}')
	TipsResponse getTips(
			@Header('Authorization') String authHeader,
			String channelId,
			@QueryValue('offset') Integer offset,
			@QueryValue('limit') Integer limit,
			@QueryValue('sort') String sort,
			@QueryValue('after') @Nullable String after,
			@QueryValue('before') @Nullable String before
	)

	@Serdeable
	@CompileStatic
	static class ChannelMe {
		String _id
		String username
	}

	@Serdeable
	@CompileStatic
	static class TipUser {
		String username
	}

	@Serdeable
	@CompileStatic
	static class Donation {
		TipUser user
		double amount
	}

	@Serdeable
	@CompileStatic
	static class Tip {
		String _id
		Donation donation
		String createdAt
	}

	@Serdeable
	@CompileStatic
	static class TipsResponse {
		List<Tip> docs
		int total
	}
}
