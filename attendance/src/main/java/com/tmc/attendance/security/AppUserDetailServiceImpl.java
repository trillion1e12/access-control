package com.tmc.attendance.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tmc.attendance.user.AppUser;
import com.tmc.attendance.user.UserRepository;

@Service
public class AppUserDetailServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public AppUserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(username).orElseThrow();
        return new AppUserAdapter(appUser);
    }

}
