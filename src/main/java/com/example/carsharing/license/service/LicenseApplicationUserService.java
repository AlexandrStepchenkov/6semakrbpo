package com.example.carsharing.license.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.carsharing.model.AppUser;
import com.example.carsharing.repository.AppUserRepository;

@Service
public class LicenseApplicationUserService {

    private final AppUserRepository appUserRepository;

    public LicenseApplicationUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getActiveUserOrFail(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        boolean active = user.isAccountNonExpired()
                && user.isAccountNonLocked()
                && user.isCredentialsNonExpired()
                && user.isEnabled();

        if (!active) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }

        return user;
    }
}
