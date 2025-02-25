/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: RADIUS.java,v 1.3 2009-06-18 18:48:23 bigfatrat Exp $
 *
 */

package com.sun.identity.authentication.modules.radius;

import java.io.*;
import java.util.*;
import java.net.*;
import com.iplanet.am.util.*;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

import com.sun.identity.authentication.spi.*;

import com.sun.identity.authentication.spi.InvalidPasswordException;

import java.security.Principal;

import com.iplanet.dpro.session.*;
import com.sun.identity.authentication.modules.radius.client.*;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.util.ISAuthConstants;
import java.io.IOException;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;

public class RADIUS extends AMLoginModule {
    // initial state
    private Map sharedState;
    private static String adminDN;
    private static String hostName;
    private String userTokenId = null;

    private String challengeID;
    private static HashSet orgHash = new HashSet(); 

// configurable option
    private boolean primary = true;

// the authentication status
    private boolean succeeded = false;
    private boolean commitSucceeded = false;
    private RADIUSPrincipal userPrincipal = null;

    private String username;
    private static final int MSG_INFORMATION = 0;
    private static final int MSG_WARNING = 1;
    private static final int MSG_ERROR = 2;

    private static boolean helperConfigDone = false;
    private static com.iplanet.am.util.Locale locale = null;
    private ResourceBundle bundle = null;
    private static Debug debug = null;

    private static final String DEFAULT_TIMEOUT = "5";
    private static final String  DEFAULT_SERVER_PORT = "1645";

    private String server1;
    private String server2;
    private String sharedSecret;
    private int    iServerPort = 1645;
    private int iTimeOut = 5;
    private RadiusConn _radiusConn = null;
    private int screenState;
    private boolean radiusSSL = false;
    private static final String amAuthRadius = "amAuthRadius";
    private boolean getCredentialsFromSharedState;
    private ChallengeException cException = null;

    static {
        if (debug == null) {
            debug = Debug.getInstance(amAuthRadius);
        }
    }
    /**
     * Initializes this <code>LoginModule</code>.
     *
     * @param subject the <code>Subject</code> to be authenticated.
     * @param sharedState shared <code>LoginModule</code> state.
     * @param options options specified in the login.
     *		<code>Configuration</code> for this particular
     *		<code>LoginModule</code>.
     */
    public void init(Subject subject, Map sharedState, Map options) {
        try {
	    bundle = amCache.getResBundle(amAuthRadius, getLoginLocale());
	    if (debug.messageEnabled()) {
		debug.message("Radius resbundle locale="+getLoginLocale());
	    }
	    this.sharedState = sharedState;

            if(options != null) {
                try {
                    server1 = Misc.getServerMapAttr(options, 
                        "iplanet-am-auth-radius-server1");
                    if (server1 == null) {
                        server1 = "localhost";
                        debug.error("Error: primary server attribute " + 
                            "misconfigured using localhost");
                    }
                    server2 = Misc.getServerMapAttr(options, 
                        "iplanet-am-auth-radius-server2");
                    if (server1 == null) {
                        server1 = "localhost";
                        debug.error("Error: primary server attribute " + 
                            "misconfigured using localhost");
                    }
                    sharedSecret = Misc.getMapAttr(options, 
                        "iplanet-am-auth-radius-secret");
                    String serverPort = Misc.getMapAttr(options,
                        "iplanet-am-auth-radius-server-port",
                        DEFAULT_SERVER_PORT);
                    iServerPort = Integer.parseInt(serverPort);
                    String timeOut = Misc.getMapAttr(options, 
                        "iplanet-am-auth-radius-timeout", 
                        DEFAULT_TIMEOUT);
                    iTimeOut = Integer.parseInt(timeOut);
                    String authLevel = Misc.getMapAttr(options, 
                        "iplanet-am-auth-radius-auth-level");
                    if (authLevel != null) {
                        try {
                            setAuthLevel(Integer.parseInt(authLevel));
                        } catch (Exception e) {
                            debug.error("Unable to set auth level " + 
                            authLevel);
                        }
                    }
                    if (debug.messageEnabled()) {
                        debug.message("server1: "+server1
			    +" server2: " + server2 
                            + " serverPort: " + serverPort 
			    + " timeOut: "+ timeOut 
                            + " authLevel: " + authLevel );
                    }

                    if ((sharedSecret == null) || 
                        (sharedSecret.length() == 0)) {
                        debug.error (
                            "RADIUS initialization failure; no Shared Secret");
                    }
                } catch (Exception ex) {
                    debug.error("RADIUS parameters initialization failure", ex);
                }
            } else {
                debug.error("options not initialized");
            }

        } catch (Exception e) {
            debug.error("RADIUS init Error....", e);
        }
    }

    private void setDynamicText(int state) throws AuthLoginException {
        Callback[] callbacks = getCallback(state);
        String prompt = ((PasswordCallback)callbacks[0]).getPrompt();
        boolean echo = ((PasswordCallback)callbacks[0]).isEchoOn();
        if (challengeID != null) {
            prompt += "[" + challengeID + "]: ";
        }
        callbacks[0] = new PasswordCallback(prompt, echo);
        replaceCallback(state, 0, callbacks[0]);
    }

    /**
     * Takes an array of submitted <code>Callback</code>,
     * process them and decide the order of next state to go.
     * Return STATE_SUCCEED if the login is successful, return STATE_FAILED
     * if the LoginModule should be ignored.
     *
     * @param callbacks an array of <code>Callback</code> for this Login state
     * @param state order of state. State order starts with 1.
     * @return int order of next state. Return STATE_SUCCEED if authentication
     *		is successful, return STATE_FAILED if the
     *		LoginModule should be ignored.
     * @throws AuthLoginException
     */
    public int process(Callback[] callbacks, int state) 
	throws AuthLoginException {
	String tmp_passwd = null;
        String sState;

        switch (state) {
          case ISAuthConstants.LOGIN_START:  
              try {
                  _radiusConn = new RadiusConn(server1, server2, 
                      iServerPort, sharedSecret, iTimeOut);
              } catch (SocketException se) {
                  debug.error ("RADIUS login failure; Socket Exception se == ",
                      se);
                  shutdown();
                  throw new AuthLoginException(amAuthRadius, "RadiusNoServer",
			null);
              } catch (Exception e) {
                  debug.error (
                      "RADIUS login failure; Can't connect to RADIUS server",
                      e);
                  shutdown();
                  throw new AuthLoginException(amAuthRadius, "RadiusNoServer",
			null);                   
              }
	      if (callbacks !=null && callbacks.length == 0) {		
	          username = (String) sharedState.get(getUserKey());
		  tmp_passwd = (String) sharedState.get(getPwdKey());
		  if (username == null || tmp_passwd == null) {
		     return ISAuthConstants.LOGIN_START;
		  }
		  getCredentialsFromSharedState = true;
			
	      } else {
                  username = ((NameCallback)callbacks[0]).getName();
                  tmp_passwd = charToString(
                  ((PasswordCallback)callbacks[1]).getPassword(),callbacks[1]);

                  if (debug.messageEnabled()) {
                      debug.message("username: " + username );
                  }
              }
            storeUsernamePasswd(username, tmp_passwd);
              try {
                  succeeded = false;
                  _radiusConn.authenticate( username, tmp_passwd);
              } catch (RejectException re) {
		  if (getCredentialsFromSharedState 
                      && !isUseFirstPassEnabled()) {
		     getCredentialsFromSharedState = false;
		     return ISAuthConstants.LOGIN_START;
		  }
                  debug.message("Radius login request rejected", re );
                  shutdown();
		  setFailureID(username);
                  throw new InvalidPasswordException(amAuthRadius, 
                     "RadiusLoginFailed", null, username, re);
              } catch (IOException ioe) {
		  if (getCredentialsFromSharedState 
                      && !isUseFirstPassEnabled()) {
		     getCredentialsFromSharedState = false;
		     return ISAuthConstants.LOGIN_START;
		  }
                  debug.error("Radius request IOException", ioe);
                  shutdown();
		  setFailureID(username);
                  throw new AuthLoginException(amAuthRadius,
                      "RadiusLoginFailed", null);
              } catch (java.security.NoSuchAlgorithmException ne) {
		  if (getCredentialsFromSharedState 
                      && !isUseFirstPassEnabled()) {
		     getCredentialsFromSharedState = false;
		     return ISAuthConstants.LOGIN_START;
		  }
                  debug.error("Radius No Such Algorithm Exception", ne );
                  shutdown();
		  setFailureID(username);
                  throw new AuthLoginException(amAuthRadius,
                      "RadiusLoginFailed", null);
              } catch (ChallengeException ce) {
		  if (getCredentialsFromSharedState 
                      && !isUseFirstPassEnabled()) {
		     getCredentialsFromSharedState = false;
		     return ISAuthConstants.LOGIN_START;
		  }
                  cException = ce;
                  sState = ce.getState();
                  if (sState == null) {
                      debug.error(
                          "Radius failure - no state returned in challenge");
                      shutdown();
		      setFailureID(username);
                      throw new AuthLoginException(amAuthRadius, 
			  "RadiusAuth", null);
                  }
                  challengeID = ce.getReplyMessage();
                  if (debug.messageEnabled()) {
                      debug.message("Server challenge with "+
                          "challengeID: "+challengeID);
                  }
                  setDynamicText(2);
                  return ISAuthConstants.LOGIN_CHALLENGE;
              } catch ( Exception e ) {
		  if (getCredentialsFromSharedState 
                      && !isUseFirstPassEnabled()) {
		     getCredentialsFromSharedState = false;
		     return ISAuthConstants.LOGIN_START;
		  }
                  shutdown();
		  setFailureID(username);
                  throw new AuthLoginException(amAuthRadius, 
                      "RadiusLoginFailed", null, e);
              } 

          succeeded = true;
          break;

          case ISAuthConstants.LOGIN_CHALLENGE: 
              String passwd = getChallengePassword(callbacks);
              if (debug.messageEnabled()) {
                  debug.message("reply to challenge--username: "+username);
              }
              try {
                  succeeded = false;
                  _radiusConn.replyChallenge(username, passwd, cException);
              } catch ( ChallengeException ce ) {
                  sState = ce.getState();
                  if (sState== null) {
                      debug.error(
                          "handle Challenge failure - no state returned");
                      shutdown();
		      setFailureID(username);
                      throw new AuthLoginException(amAuthRadius, 
                          "RadiusLoginFailed", null);
                  }
                  resetCallback(2, 0);
                  challengeID = ce.getReplyMessage();
                  if (debug.messageEnabled()) {
                      debug.message("Server challenge again"+
                          " with challengeID: "+challengeID);
                  }
                  cException = ce;  // save it for next replyChallenge
                  setDynamicText(2);
                  // note that cException is reused
                  return ISAuthConstants.LOGIN_CHALLENGE;
              } catch (RejectException ex) {
                  debug.error("Radius challenge response rejected", ex);
                  shutdown();
		  setFailureID(username);
                  throw new InvalidPasswordException(amAuthRadius, 
                     "RadiusLoginFailed", null, username, ex);
              }  catch (IOException ioe) {
                  debug.error("Radius challenge IOException", ioe);
                  shutdown();
		  setFailureID(username);
                  throw new AuthLoginException(amAuthRadius, 
                      "RadiusLoginFailed", null);
              }  catch (java.security.NoSuchAlgorithmException ex) {
                  debug.error("Radius No Such Algorithm Exception", ex);
                  shutdown();
		  setFailureID(username);
                  throw new AuthLoginException(amAuthRadius,
                      "RadiusLoginFailed", null);
              }  catch (Exception e ) {
                  debug.error(
                      "RADIUS challenge Authentication Failed ", e);
                  shutdown();
		  setFailureID(username);
                  throw new AuthLoginException(amAuthRadius,
                      "RadiusLoginFailed", null);
              }
              succeeded = true;
          break;

          default:
              debug.error(
                  "RADIUS Authentication Failed - invalid state" +
                  state);
              shutdown();
              succeeded = false;
	      setFailureID(username);
              throw new AuthLoginException(amAuthRadius,
                  "RadiusLoginFailed", null);
        }
        if ( succeeded == true ) {
            if (debug.messageEnabled()) {
                debug.message("RADIUS authentication successful");
            }
 	    if (username != null) {
                StringTokenizer usernameToken =
                                new StringTokenizer(username,",");
                userTokenId = usernameToken.nextToken();
            }
            if (debug.messageEnabled()) {
              debug.message("userTokenID: " + userTokenId);
            }
            shutdown();
            return ISAuthConstants.LOGIN_SUCCEED;
        } else {
            if (debug.messageEnabled()) {
                debug.message("RADIUS authentication to be ignored");
            }
            return ISAuthConstants.LOGIN_IGNORE;
        }
    }
        
    /**
     * Returns <code>java.security.Principal</code>.
     *
     * @return <code>java.security.Principal</code>
     */
    public java.security.Principal getPrincipal() {
        if (userPrincipal != null) {
            return userPrincipal;
        } else if (userTokenId != null) {
            userPrincipal = new RADIUSPrincipal(userTokenId);
            return userPrincipal;
        } else {
            return null;
        }
    }

    /**
     * Destroy the module state
     */
    public void destroyModuleState(){
	userTokenId = null;
        userPrincipal = null;
    }

    /**
     * Set all the used variables to null
     */
    public void nullifyUsedVars() {
        sharedState = null;
        challengeID = null;

        bundle = null;

        server1 = null;
        server2 = null;
        sharedSecret = null;
    }


    private String getChallengePassword(Callback[] callbacks) 
        throws AuthLoginException {
        // callback[0] is for password(also display challenge text)
        char[] tmpPassword = ((PasswordCallback)callbacks[0]).getPassword();
        if (tmpPassword == null) {
            // treat a NULL password as an empty password
            tmpPassword = new char[0];
        }
        char[] pwd = new char[tmpPassword.length];
        System.arraycopy(tmpPassword, 0, pwd, 0, tmpPassword.length);
        ((PasswordCallback)callbacks[0]).clearPassword();

        return (new String(pwd));
    }

    private String charToString (char [] tmpPassword, Callback cbk ) {
        if (tmpPassword == null) {
                // treat a NULL password as an empty password
                tmpPassword = new char[0];
            }
        char[] pwd = new char[tmpPassword.length];
        System.arraycopy(tmpPassword, 0, pwd, 0, tmpPassword.length);
        ((PasswordCallback)cbk).clearPassword();
        return new String(pwd);
    }

    /**
     * Shutdown the RADIUS connection
     */
    public void shutdown () {
        try {
            _radiusConn.disconnect();
        } catch (IOException e) {
        }
        _radiusConn = null;
    }

} 

