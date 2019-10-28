package com.liveramp.workflow_ui.security;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;


/**
 * Set configurations in {@link com.liveramp.workflow_ui.WorkflowDbWebServer#WORKFLOW_UI_PROPERTIES}
 */
@Configuration
@EnableWebSecurity
@EnableSpringHttpSession
@PropertySource("file:${workflow.ui.properties}")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  Environment environment;
  private String authMethod;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (getAuthMethod().equals("none")) {
      http.authorizeRequests()
          .antMatchers("/*")
          .permitAll()
          .and()
          .csrf()
          .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    } else {
      http
          .authorizeRequests()
          .antMatchers("/login*", "/resources/**", "/css/**", "/fonts/**", "/images/**").permitAll()
          .anyRequest().authenticated()
          .and()
          .formLogin()
          .loginPage("/login.html")
          .and()
          .httpBasic()
          .and()
          .csrf()
          .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    if (getAuthMethod().equals("ldap")) {
      auth
          .ldapAuthentication()
          .userSearchBase(environment.getProperty("ui.auth.ldap.userSearchBase"))
          .userSearchFilter(environment.getProperty("ui.auth.ldap.userSearchFilter"))
          .groupSearchBase(environment.getProperty("ui.auth.ldap.groupSearchBase"))
          .groupSearchFilter(environment.getProperty("ui.auth.ldap.groupSearchFilter"))
          .contextSource(contextSource());
    } else if (getAuthMethod().equals("fixed")) {

      String userList = environment.getProperty("ui.auth.method.fixed.users");
      String[] users = userList.split(",");

      InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

      for (String user : users) {
        String[] usernamePassword = user.split(":");
        manager.createUser(new User(usernamePassword[0], usernamePassword[1], Collections.emptySet()));
      }

      auth.userDetailsService(manager);

    } else if (getAuthMethod().equals("none")) {

    }
    //  TODO add OAuth2
    else {
      throw new RuntimeException("No authentication configured: found " + getAuthMethod());
    }

  }

  private String getAuthMethod() {
    if (authMethod == null) {
      authMethod = environment.getProperty("ui.auth.method");
    }
    return authMethod;
  }

  @Bean
  public DefaultSpringSecurityContextSource contextSource() {

    String authMethod = environment.getProperty("ui.auth.method");

    if (authMethod.equals("ldap")) {

      return new DefaultSpringSecurityContextSource(
          Arrays.asList(environment.getProperty("ui.auth.ldap.providerURLs").split(",")),
          environment.getProperty("ui.auth.ldap.baseDN")
      );
    }

    return null;

  }

  @Bean
  public SessionRepository sessionRepository() {
    return new MapSessionRepository();
  }

  @Bean
  public HeaderHttpSessionStrategy sessionStrategy() {
    return new HeaderHttpSessionStrategy();
  }

}
