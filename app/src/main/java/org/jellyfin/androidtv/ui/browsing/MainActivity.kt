package org.jellyfin.androidtv.ui.browsing

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.tv.fengymi.danmu.model.DanmuApiOption
import com.tv.fengymi.danmu.utils.DanmuUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.danmu.api.DanmuApi
import org.jellyfin.androidtv.danmu.utils.SharedPreferencesDanmuConfig
import org.jellyfin.androidtv.danmu.utils.SimpleDanmuUtil
import org.jellyfin.androidtv.databinding.ActivityMainBinding
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.navigation.NavigationAction
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.screensaver.InAppScreensaver
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.applyTheme
import org.jellyfin.androidtv.util.isMediaSessionKeyEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Objects

class MainActivity : FragmentActivity() {
	companion object {
		private const val FRAGMENT_TAG_CONTENT = "content"
	}

	private val navigationRepository by inject<NavigationRepository>()
	private val sessionRepository by inject<SessionRepository>()
	private val userRepository by inject<UserRepository>()
	private val screensaverViewModel by viewModel<ScreensaverViewModel>()
	private var inTransaction = false

	private val danmuConfig: SharedPreferencesDanmuConfig by inject()
	private val danmuApi: DanmuApi by inject()

	private lateinit var binding: ActivityMainBinding

	private val backPressedCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			if (navigationRepository.canGoBack) navigationRepository.goBack()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()

		super.onCreate(savedInstanceState)

		if (!validateAuthentication()) return

		screensaverViewModel.keepScreenOn.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
			.onEach { keepScreenOn ->
				if (keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}.launchIn(lifecycleScope)

		onBackPressedDispatcher.addCallback(this, backPressedCallback)

		supportFragmentManager.addOnBackStackChangedListener {
			if (!inTransaction && supportFragmentManager.backStackEntryCount == 0)
				navigationRepository.reset()
		}

		if (savedInstanceState == null && navigationRepository.canGoBack) navigationRepository.reset()

		navigationRepository.currentAction
			.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
			.onEach { action ->
				handleNavigationAction(action)
				backPressedCallback.isEnabled = navigationRepository.canGoBack
				screensaverViewModel.notifyInteraction(false)
			}.launchIn(lifecycleScope)

		binding = ActivityMainBinding.inflate(layoutInflater)
		binding.background.setContent { AppBackground() }
		binding.screensaver.setContent { InAppScreensaver() }
		setContentView(binding.root)
		getDefaultConfigSetting()
	}

	private fun getDefaultConfigSetting() {
		DanmuUtils.submit {
			try {
				Timber.i("配置信息 = $danmuConfig")
				val supportSites = danmuApi.getSupportSites()
				val danmuSources = supportSites.content.data ?: return@submit

				val size = danmuSources.size;
				val mergeSites:ArrayList<DanmuApiOption> = ArrayList(size)
				val localDanmuApiList = danmuConfig.danmuApiList
				for (danmuSource in danmuSources) {
					val option = DanmuApiOption()
					option.source = danmuSource.source
					option.sourceName = danmuSource.sourceName
					option.isOpened = danmuSource.opened == true

					for (localDanmuSource in localDanmuApiList) {
						if (Objects.equals(localDanmuSource.source, danmuSource.source)) {
							option.isOpened = localDanmuSource.isOpened
							break
						}
					}

					mergeSites.add(option)
				}

				danmuConfig.danmuApiList = mergeSites
				Timber.i("更新后配置信息 = ${SimpleDanmuUtil.toJsonString(danmuConfig.danmuApiList)}")
			} catch (e: Exception) {
				Timber.i(e, "getDefaultConfigSetting 异常")
				SimpleDanmuUtil.show(this@MainActivity, "弹幕配置更新失败, 忽略")
			}
		}
	}


	override fun onResume() {
		super.onResume()

		if (!validateAuthentication()) return

		applyTheme()

		screensaverViewModel.activityPaused = false
	}

	private fun validateAuthentication(): Boolean {
		if (sessionRepository.currentSession.value == null || userRepository.currentUser.value == null) {
			Timber.w("Activity ${this::class.qualifiedName} started without a session, bouncing to StartupActivity")
			startActivity(Intent(this, StartupActivity::class.java))
			finish()
			return false
		}

		return true
	}

	override fun onPause() {
		super.onPause()

		screensaverViewModel.activityPaused = true
	}

	override fun onStop() {
		super.onStop()

		lifecycleScope.launch {
			Timber.d("MainActivity stopped")
			sessionRepository.restoreSession(destroyOnly = true)
		}
	}

	private fun handleNavigationAction(action: NavigationAction) = when (action) {
		is NavigationAction.NavigateFragment -> {
			if (action.clear) {
				// Clear the current back stack
				val firstBackStackEntry = supportFragmentManager.getBackStackEntryAt(0)
				supportFragmentManager.popBackStack(firstBackStackEntry.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
			} else if (action.replace) {
				// Prevent back stack changed listener from resetting when popping to
				// the initial destination
				inTransaction = true
				supportFragmentManager.popBackStack()
				inTransaction = false
			}

			supportFragmentManager.commit {
				val destination = action.destination
				val currentFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_CONTENT)
				val isSameFragment = currentFragment != null &&
					destination.fragment.isInstance(currentFragment) &&
					currentFragment.arguments == destination.arguments

				if (!isSameFragment) {
					setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)

					replace(R.id.content_view, destination.fragment.java, destination.arguments, FRAGMENT_TAG_CONTENT)
				}

				if (action.addToBackStack) addToBackStack(null)
			}
		}

		is NavigationAction.NavigateActivity -> {
			val destination = action.destination
			val intent = Intent(this@MainActivity, destination.activity.java)
			intent.putExtras(destination.extras)
			startActivity(intent)
			action.onOpened()
		}

		NavigationAction.GoBack -> supportFragmentManager.popBackStack()

		NavigationAction.Nothing -> Unit
	}

	// Forward key events to fragments
	private fun Fragment.onKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
		var result = childFragmentManager.fragments.any { it.onKeyEvent(keyCode, event) }
		if (!result && this is View.OnKeyListener) result = onKey(currentFocus, keyCode, event)
		return result
	}

	private fun onKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
		// Ignore the key event that closes the screensaver
		if (screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event?.action == KeyEvent.ACTION_UP)
			return true
		}

		return supportFragmentManager.fragments
			.any { it.onKeyEvent(keyCode, event) }
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event)

	override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyUp(keyCode, event)

	override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyUp(keyCode, event)

	override fun onUserInteraction() {
		super.onUserInteraction()

		screensaverViewModel.notifyInteraction(false)
	}

	@Suppress("RestrictedApi") // False positive
	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		// Ignore the key event that closes the screensaver
		if (!event.isMediaSessionKeyEvent() && screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event.action == KeyEvent.ACTION_UP)
			return true
		}

		@Suppress("RestrictedApi") // False positive
		return super.dispatchKeyEvent(event)
	}

	@Suppress("RestrictedApi") // False positive
	override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
		// Ignore the key event that closes the screensaver
		if (!event.isMediaSessionKeyEvent() && screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event.action == KeyEvent.ACTION_UP)
			return true
		}

		@Suppress("RestrictedApi") // False positive
		return super.dispatchKeyShortcutEvent(event)
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		// Ignore the touch event that closes the screensaver
		if (screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(true)
			return true
		}

		return super.dispatchTouchEvent(ev)
	}
}
