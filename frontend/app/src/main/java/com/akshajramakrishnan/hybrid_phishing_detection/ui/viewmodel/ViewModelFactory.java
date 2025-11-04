package com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public ViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (AndroidViewModelDetector.isAndroidViewModelWithAppConstructor(modelClass)) {
            try {
                return (T) modelClass.getConstructor(Application.class).newInstance(application);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create AndroidViewModel " + modelClass.getName(), e);
            }
        }

        try {
            return modelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create ViewModel: " + modelClass.getName() +
                    ". Make sure it has either a (Application) constructor or a no-arg constructor.", e);
        }
    }


     //helper to detect if type has constructor
    private static class AndroidViewModelDetector {
        static boolean isAndroidViewModelWithAppConstructor(Class<?> cls) {
            try {
                cls.getConstructor(Application.class);
                return true;
            } catch (NoSuchMethodException ignored) {
                return false;
            }
        }
    }
}
