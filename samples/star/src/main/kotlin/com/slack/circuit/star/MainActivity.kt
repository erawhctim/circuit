// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.star

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_DARK
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_LIGHT
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import com.slack.circuit.CircuitCompositionLocals
import com.slack.circuit.CircuitConfig
import com.slack.circuit.CircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.continuityRetainedStateRegistry
import com.slack.circuit.star.di.ActivityKey
import com.slack.circuit.star.di.AppScope
import com.slack.circuit.star.home.HomeScreen
import com.slack.circuit.star.navigator.AndroidScreen
import com.slack.circuit.star.ui.StarTheme
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

@ContributesMultibinding(AppScope::class, boundType = Activity::class)
@ActivityKey(MainActivity::class)
class MainActivity
@Inject
constructor(
  private val viewModelProviderFactory: ViewModelProvider.Factory,
  private val circuitConfig: CircuitConfig
) : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /*
     * Temporarily disabling deeplink support, as it's not entirely relevant to this
     * example branch demonstrating multiple backstacks.
     */
//    if (intent.data != null) {
//      val httpUrl = intent.data.toString().toHttpUrl()
//      val animalId = httpUrl.pathSegments[1].substringAfterLast("-").toLong()
//      val petDetailScreen = PetDetailScreen(animalId, null)
//      backStack = persistentListOf(HomeScreen, petDetailScreen)
//    }

    setContent {
      StarTheme {
        // TODO why isn't the windowBackground enough so we don't need to do this?
        Surface(color = MaterialTheme.colorScheme.background) {
          CircuitCompositionLocals(circuitConfig) {
            ContentWithOverlays {
              CompositionLocalProvider(
                LocalRetainedStateRegistry provides continuityRetainedStateRegistry(),
              ) {
                CircuitContent(HomeScreen,  circuitConfig)
              }
            }
          }
        }
      }
    }
  }

  override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
    return viewModelProviderFactory
  }
}
