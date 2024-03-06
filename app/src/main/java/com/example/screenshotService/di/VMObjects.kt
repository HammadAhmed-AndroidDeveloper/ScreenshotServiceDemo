package com.example.screenshotService.di

import com.example.screenshotService.repo.ImageRepository
import com.example.screenshotService.repo.ImageViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object VMObjects {

    @Provides
    @ViewModelScoped
    fun provideVM(repository: ImageRepository): ImageViewModel {
        return ImageViewModel(repository)
    }
}