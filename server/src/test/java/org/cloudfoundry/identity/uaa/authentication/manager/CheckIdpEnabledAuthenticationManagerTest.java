package org.cloudfoundry.identity.uaa.authentication.manager;

import org.cloudfoundry.identity.uaa.constants.OriginKeys;
import org.cloudfoundry.identity.uaa.provider.IdentityProvider;
import org.cloudfoundry.identity.uaa.provider.JdbcIdentityProviderProvisioning;
import org.cloudfoundry.identity.uaa.security.PollutionPreventionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(PollutionPreventionExtension.class)
class CheckIdpEnabledAuthenticationManagerTest {

    private CheckIdpEnabledAuthenticationManager checkIdpEnabledAuthenticationManager;

    @Mock
    private AuthenticationManager mockAuthenticationManager;

    @Mock
    private JdbcIdentityProviderProvisioning mockJdbcIdentityProviderProvisioning;

    @Mock
    private Authentication mockInputAuthentication;

    @Mock
    private IdentityProvider mockIdentityProvider;

    @BeforeEach
    void setUp() {
        checkIdpEnabledAuthenticationManager = new CheckIdpEnabledAuthenticationManager(mockAuthenticationManager, OriginKeys.UAA, mockJdbcIdentityProviderProvisioning);
        when(mockJdbcIdentityProviderProvisioning.retrieveByOriginIgnoreActiveFlag(anyString(), anyString())).thenReturn(mockIdentityProvider);
    }

    @Test
    void withActiveIdp() {
        Authentication mockOutputAuthentication = mock(Authentication.class);
        when(mockIdentityProvider.isActive()).thenReturn(true);
        when(mockAuthenticationManager.authenticate(any(Authentication.class))).thenReturn(mockOutputAuthentication);
        Authentication actual = checkIdpEnabledAuthenticationManager.authenticate(mockInputAuthentication);
        assertThat(actual, equalTo(mockOutputAuthentication));
        verify(mockAuthenticationManager).authenticate(mockInputAuthentication);
    }

    @Test
    void withInactiveIdp() {
        when(mockIdentityProvider.isActive()).thenReturn(false);
        assertThrows(ProviderNotFoundException.class,
                () -> checkIdpEnabledAuthenticationManager.authenticate(mockInputAuthentication));
    }
}
