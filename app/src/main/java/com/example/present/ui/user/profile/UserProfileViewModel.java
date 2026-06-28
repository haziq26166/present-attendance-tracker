package com.example.present.ui.user.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.present.data.model.User;
import com.example.present.data.repository.AuthRepository;

public class UserProfileViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final AuthRepository authRepository;

    public UserProfileViewModel() {
        this.authRepository = new AuthRepository();
        loadUser();
    }

    private void loadUser() {
        authRepository.fetchUserData(authRepository.getCurrentUser().getUid(), user::setValue, e -> {});
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void logout() {
        authRepository.logout();
    }
}