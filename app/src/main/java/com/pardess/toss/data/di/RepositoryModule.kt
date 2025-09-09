package com.pardess.toss.data.di

import com.pardess.toss.data.repository.TossRepositoryImpl
import com.pardess.toss.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTodoRepository(tossRepositoryImpl: TossRepositoryImpl): TodoRepository
}
