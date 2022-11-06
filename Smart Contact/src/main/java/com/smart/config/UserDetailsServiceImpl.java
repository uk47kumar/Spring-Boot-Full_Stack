package com.smart.config;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // fetching user from database

        User user = userRepository.getUserByUserName(username);
        if(user == null){
            throw new UsernameNotFoundException("could not find user!!");
        }
        CustomUserDetail customUserDetail = new CustomUserDetail(user);
        return customUserDetail;
    }
}
