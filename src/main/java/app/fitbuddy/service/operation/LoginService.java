package app.fitbuddy.service.operation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import app.fitbuddy.dto.appuser.AppUserResponseDTO;
import app.fitbuddy.exception.FitBuddyException;
import app.fitbuddy.service.crud.AppUserCrudService;

/**
 * Provides a service to handle the login process.
 */
@Service
public class LoginService {	
	
	private final Logger logger;
	private final AppUserCrudService appUserCrudService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	public LoginService(AppUserCrudService appUserCrudService) {
		this.appUserCrudService = appUserCrudService;
		this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
		this.logger = LoggerFactory.getLogger(LoginService.class);
	}
	
	public void login(String name, String password) {		
		// find the user
		AppUserResponseDTO appUserResponseDTO = appUserCrudService.readByName(name);
		if (appUserResponseDTO == null) {
			throw new FitBuddyException("Username not found.");
		}		
		
		// check the password
		if (!bCryptPasswordEncoder.matches(password, appUserResponseDTO.getPassword())) {
			throw new FitBuddyException("Incorrect password.");
		}
		
		// create the GrantedAuthority list
		List<GrantedAuthority> grantList = new ArrayList<>();
		
		// add the role name
		grantList.add(new SimpleGrantedAuthority(appUserResponseDTO.getRolename()));				
		
		// create a new auth
		Authentication auth = new UsernamePasswordAuthenticationToken(name, password, grantList);
		
		// login the user
		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);
		
		logger.info("Logged in: {}", appUserResponseDTO);
	}

}