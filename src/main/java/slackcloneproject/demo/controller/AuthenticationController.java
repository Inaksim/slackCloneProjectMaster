package slackcloneproject.demo.controller;


import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import javax.servlet.http.Cookie;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.util.WebUtils;
import slackcloneproject.demo.dto.AuthUserDTO;
import slackcloneproject.demo.dto.JwtDTO;
import slackcloneproject.demo.dto.user.GroupDTO;
import slackcloneproject.demo.dto.user.InitUserDTO;
import slackcloneproject.demo.entity.GroupEntity;
import slackcloneproject.demo.entity.UserEntity;
import slackcloneproject.demo.mapper.GroupMapper;
import slackcloneproject.demo.mapper.UserMapper;
import slackcloneproject.demo.service.CustomUserDetailsService;
import slackcloneproject.demo.service.GroupService;
import slackcloneproject.demo.service.UserService;
import slackcloneproject.demo.utils.JwtUtil;
import slackcloneproject.demo.utils.StaticVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api")
public class AuthenticationController {
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private CustomUserDetailsService userDetailsService;
    private UserMapper userMapper;
    private UserService userService;
    private GroupService groupService;
    private GroupMapper groupMapper;


    @PostMapping(value = "/auth")
    public AuthUserDTO createAuthenticationToken(@RequestBody JwtDTO authenticationRequest, HttpServletResponse response) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        UserEntity user = userService.findByNameOrEmail(authenticationRequest.getPassword(), authenticationRequest.getPassword());
        String token = jwtUtil.generateToken(userDetails);
        Cookie jwtAuthToken = new Cookie(StaticVariable.SECURE_COOKIE, token);
        jwtAuthToken.setHttpOnly(true);
        jwtAuthToken.setSecure(false);
        jwtAuthToken.setPath("/");

        jwtAuthToken.setMaxAge(7*24*60*60);
        response.addCookie(jwtAuthToken);
        return userMapper.toLightUserDTO(user);
    }

    @GetMapping(value = "/logout")
    public ResponseEntity<?> fetchInformation(HttpServletResponse response) {
        Cookie cookie = new Cookie(StaticVariable.SECURE_COOKIE, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/fetch")
    public InitUserDTO fetchInformation(HttpServletRequest request) {
        return userMapper.toUserDTO(getUserEntity(request));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @PostMapping(value = "/create")
    public GroupDTO createGroupChat(HttpServletRequest request, @RequestBody String payload) {
        UserEntity user = getUserEntity(request);
        Gson gson = new Gson();
        GroupDTO groupDTO = gson.fromJson(payload, GroupDTO.class);
        GroupEntity groupEntity = groupService.createGroup(user.getId(), groupDTO.getName());
        return groupMapper.toGroupDTO(groupEntity, user.getId());

    }

    private UserEntity getUserEntity(HttpServletRequest request) {
        String username;
        String jwtToken;
        UserEntity user = new UserEntity();
        Cookie cookie = WebUtils.getCookie(request, StaticVariable.SECURE_COOKIE);
        if(cookie != null) {
            jwtToken = cookie.getValue();
            username = jwtUtil.getUserNameFromJwtToken(jwtToken);
            user = userService.findByNameOrEmail(username, username);
        }
        return user;
    }


}
