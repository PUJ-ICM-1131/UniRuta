package com.uniruta.app

import android.app.Application

class UniRutaApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
