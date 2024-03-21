package com.intec.t2ocina.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.t2o.preferences.PreferencesRepository
import com.intec.t2o.robotinterface.RobotConnectionService
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.robotinterface.SkillApiService
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
    fun provideRobotManager(@ApplicationContext appContext: Context, robotConnectionService: RobotConnectionService): RobotManager {
        return RobotManager(robotConnectionService, appContext) // Asumiendo que RobotManager no tiene dependencias en su constructor
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

    @Singleton
    @Provides
    fun provideRobotConnectionService(@ApplicationContext context: Context): RobotConnectionService {
        return RobotConnectionService(context, skillApi = SkillApi())
    }

    @Singleton
    @Provides
    fun provideSkillApi(@ApplicationContext context: Context): SkillApiService {
        // Suponiendo que SkillApi tiene un método estático `getInstance()` y requiere inicialización
        return SkillApiService(context)
    }

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