package com.intec.telemedicina.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.api.RickAndMortyApi
import com.intec.telemedicina.preferences.PreferencesRepository
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.utils.BASE_URL
import com.intec.telemedicina.viewmodels.MqttViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideRickAndMortyApi(): RickAndMortyApi {
        return  Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickAndMortyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(sharedPreferences: SharedPreferences): PreferencesRepository {
        return PreferencesRepository(sharedPreferences)
    }

    /*@Provides
    @Singleton
    fun provideRobotApi(): RobotApi = RobotApi.getInstance()*/

    @Provides
    @Singleton
    fun provideSkillApi(): SkillApi = SkillApi()

    @Provides
    @Singleton
    fun provideActionListener(): ActionListener {
        return ActionListener()
    }

    @Provides
    @Singleton
    fun provideMqttClient(application: Application): MqttAndroidClient {
        val persistence = MqttDefaultFilePersistence(application.filesDir.path)
        return MqttAndroidClient(application, "tcp://localhost:1883", "BasicSample", persistence)
    }

}