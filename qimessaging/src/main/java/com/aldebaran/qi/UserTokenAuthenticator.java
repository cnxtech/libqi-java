package com.aldebaran.qi;

import java.util.HashMap;
import java.util.Map;

/**
 * Specific {@link ClientAuthenticator} authenticating from a user and a token.
 *
 * If the server trusts on first use, we may receive a new token. In that case,
 * {@link #hasNewToken()} returns {@code true} and the new token is available
 * through {@link #getToken()}.
 */
public class UserTokenAuthenticator implements ClientAuthenticator
{
  private String user;
  private String token;
  private boolean hasNewToken;

  /**
   * Create an authenticator with an user and an initial token.
   *
   * @param user
   *          the user
   * @param initialToken
   *          the initial token (may be {@code null})
   */
  public UserTokenAuthenticator(String user, String initialToken)
  {
    if (initialToken == null)
    {
      // "" means no token (null would segfault in the API)
      initialToken = "";
    }
    this.user = user;
    token = initialToken;
  }

  /**
   * Create an authenticator with an user but no token.
   *
   * @param user
   *          the user
   */
  public UserTokenAuthenticator(String user)
  {
    this(user, null);
  }

  private static Map<String, Object> createMap(String user, String token)
  {
    Map<String, Object> authData = new HashMap<String, Object>();
    authData.put("user", user);
    authData.put("token", token);
    return authData;
  }

  @Override
  public Map<String, Object> initialAuthData()
  {
    hasNewToken = false;
    return createMap(user, token);
  }

  @Override
  public Map<String, Object> _processAuth(Map<String, Object> authData)
  {
    // If no token was provided by initialAuthData(), the gateway
    // generates and provides it the very first time (it trusts on first
    // use).
    token = (String) authData.get("newToken");
    hasNewToken = true;

    // We must return our new authentication data.
    return createMap(user, token);
  }

  /**
   * Indicates whether a new token has been retrieved during the last
   * authentication.
   *
   * @return {@©ode true} if a new token has been retrieved, {@false} otherwise
   */
  public boolean hasNewToken()
  {
    return hasNewToken;
  }

  /**
   * Return the token.
   *
   * @return the token
   */
  public String getToken()
  {
    return token;
  }
}