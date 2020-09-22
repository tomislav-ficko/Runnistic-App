package com.ficko.runnisticapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // This tells our app that we want to inject dependencies using Dagger Hilt
class BaseApplication : Application() {
}