package com.intec.telemedicina.robotinterface

import com.ainirobot.coreservice.client.RobotApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RobotApiModule {
    @Provides
    @Singleton
    fun provideRobotApi(): RobotApi = RobotApi.getInstance()
    
}