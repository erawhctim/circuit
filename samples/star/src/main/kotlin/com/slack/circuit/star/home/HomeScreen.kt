// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.star.home

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.slack.circuit.CircuitUiEvent
import com.slack.circuit.CircuitUiState
import com.slack.circuit.NavEvent
import com.slack.circuit.NavigableCircuitContent
import com.slack.circuit.Navigator
import com.slack.circuit.Screen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.onNavEvent
import com.slack.circuit.push
import com.slack.circuit.rememberCircuitNavigator
import com.slack.circuit.star.di.AppScope
import com.slack.circuit.star.home.HomeScreen.Event.ChildNav
import com.slack.circuit.star.home.HomeScreen.Event.HomeEvent
import com.slack.circuit.star.navigator.AndroidScreen
import com.slack.circuit.star.navigator.AndroidSupportingNavigator
import com.slack.circuit.star.ui.StarTheme
import kotlinx.collections.immutable.toPersistentList
import kotlinx.parcelize.Parcelize

@Parcelize
object HomeScreen : Screen {
  data class State(
    val homeNavState: HomeNavScreen.State,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  sealed interface Event : CircuitUiEvent {
    class HomeEvent(val event: HomeNavScreen.Event) : Event
    class ChildNav(val navEvent: NavEvent) : Event
  }
}

@CircuitInject(screen = HomeScreen::class, scope = AppScope::class)
@Composable
fun HomePresenter(navigator: Navigator): HomeScreen.State {
  val homeNavState = HomeNavPresenter()
  return HomeScreen.State(homeNavState) { event ->
    when (event) {
      is HomeEvent -> homeNavState.eventSink(event.event)
      is ChildNav -> navigator.onNavEvent(event.navEvent)
    }
  }
}

@CircuitInject(screen = HomeScreen::class, scope = AppScope::class)
@Composable
fun HomeContent(state: HomeScreen.State, modifier: Modifier = Modifier) {
  val systemUiController = rememberSystemUiController()
  systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
  systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.primaryContainer)

  val navItems = HomeNavScreen.HOME_NAV_ITEMS
  val backstacks = navItems.map { rememberSaveableBackStack { push(it.screen) } }.toPersistentList()

  val eventSink = state.eventSink
  Scaffold(
    modifier = modifier.navigationBarsPadding().systemBarsPadding().fillMaxWidth(),
    bottomBar = {
      StarTheme(useDarkTheme = true) {
        BottomNavigationBar(navItems, selectedIndex = state.homeNavState.index) { index ->
          eventSink(HomeEvent(HomeNavScreen.Event.ClickNavItem(index)))
        }
      }
    }
  ) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
      val context = LocalContext.current
      val backstack = backstacks[state.homeNavState.index]

      val circuitNavigator = key (state.homeNavState.index) { rememberCircuitNavigator(backstack) }
      val navigator = remember(circuitNavigator) {
        AndroidSupportingNavigator(circuitNavigator) { androidScreen ->
          goTo(context, androidScreen)
        }
      }

      NavigableCircuitContent(navigator, backstack)
    }
  }
}

@Composable
private fun BottomNavigationBar(
  items: List<BottomNavItem>,
  selectedIndex: Int,
  onSelectedIndex: (Int) -> Unit
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
  ) {
    items.forEachIndexed { index, item ->
      NavigationBarItem(
        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
        label = { Text(text = item.title) },
        alwaysShowLabel = true,
        selected = selectedIndex == index,
        onClick = { onSelectedIndex(index) }
      )
    }
  }
}

private fun goTo(context: Context, screen: AndroidScreen) =
  when (screen) {
    is AndroidScreen.CustomTabsIntentScreen -> goTo(context, screen)
    is AndroidScreen.IntentScreen -> TODO()
  }

private fun goTo(context: Context, screen: AndroidScreen.CustomTabsIntentScreen) {
  val scheme = CustomTabColorSchemeParams.Builder().setToolbarColor(0x000000).build()
  CustomTabsIntent.Builder()
    .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, scheme)
    .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, scheme)
    .setShowTitle(true)
    .build()
    .launchUrl(context, Uri.parse(screen.url))
}