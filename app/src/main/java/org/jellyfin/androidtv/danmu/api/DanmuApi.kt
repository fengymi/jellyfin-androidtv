package org.jellyfin.androidtv.danmu.api

import kotlinx.coroutines.runBlocking
import org.jellyfin.androidtv.danmu.model.DanmuParams
import org.jellyfin.androidtv.danmu.model.DanmuResult
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.get
import org.jellyfin.sdk.api.client.extensions.post
import org.jellyfin.sdk.api.operations.Api
import org.jellyfin.sdk.model.UUID
import timber.log.Timber

//import org.jellyfin.apiclient.interaction.ApiClient;
// 注入
// import static org.koin.java.KoinJavaComponent.inject;
// org.jellyfin.sdk.api.operations.VideosApi

class DanmuApi(private val api: ApiClient) : Api {

	fun getDanmuById(itemId: UUID, sites: Collection<String>): Response<DanmuResult> {
		return runBlocking{getSDanmuById(itemId, sites)}
	}

	fun getSupportSites(): Response<DanmuResult> {
		return runBlocking{getSSupportSites()}
	}

	suspend fun getSDanmuById(itemId: UUID, sites: Collection<String>): Response<DanmuResult> {
		// org.jellyfin.sdk.api.operations.VideosApi
		val body = DanmuParams(needSites = sites)
		val response = api.post<DanmuResult>("/api/danmu/$itemId/json", requestBody = body)

		Timber.i("获取弹幕结果: ${response.content.data?.size}")
		return response;
	}

	suspend fun getSSupportSites(): Response<DanmuResult> {
		// org.jellyfin.sdk.api.operations.VideosApi
		val response = api.get<DanmuResult>("/api/danmu/supportsites")
		Timber.i("获取弹幕结果: ${response.content}")
		return response;
	}
}
