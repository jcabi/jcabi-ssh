/**
 * Copyright (c) 2014-2015, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.ssh;

import com.google.common.io.Files;
import java.io.File;
import java.security.PublicKey;
import java.util.Collections;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.auth.UserAuthPublicKey;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.mockito.Mockito;

/**
 * Factory creating mock SSH servers.
 *
 * @author Piotr Pradzynski (prondzyn@gmail.com)
 * @version $Id$
 * @since 1.6
 * @checkstyle AbstractClassNameCheck (500 lines)
 */
final class MockSshServerFactory {

    /**
     * Utility class constructor.
     */
    private MockSshServerFactory() {
        super();
    }

    /**
     * Setup SSH server based on password authentication.
     *
     * @param port Port to listen on.
     * @param login Login for an authentication.
     * @param password Password for an authentication.
     * @return SSH server.
     */
    public static SshServer passwordAuthenticatedServer(final int port,
        final String login, final String password) {
        final SshServer sshd = defaultServer(
            port,
            new UserAuthPassword.Factory()
        );
        final PasswordAuthenticator auth =
            Mockito.mock(PasswordAuthenticator.class);
        Mockito.when(
            auth.authenticate(
                Mockito.eq(login),
                Mockito.eq(password),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        sshd.setPasswordAuthenticator(auth);
        return sshd;
    }

    /**
     * Setup SSH server based on public key authentication.
     *
     * @param port Port to listen on.
     * @return SSH server.
     */
    public static SshServer publicKeyAuthenticatedServer(final int port) {
        final SshServer sshd = defaultServer(
            port,
            new UserAuthPublicKey.Factory()
        );
        final PublickeyAuthenticator auth =
            Mockito.mock(PublickeyAuthenticator.class);
        Mockito.when(
            auth.authenticate(
                Mockito.anyString(),
                Mockito.any(PublicKey.class),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        sshd.setPublickeyAuthenticator(auth);
        return sshd;
    }

    /**
     * Setup default SSH server.
     *
     * @param port Port to listen on.
     * @param authfactory Authentication factory to use.
     * @return SSH server.
     */
    private static SshServer defaultServer(final int port,
        final NamedFactory<UserAuth> authfactory) {
        final SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(
            new SimpleGeneratorHostKeyProvider(
                new File(Files.createTempDir(), "hostkey.ser").getAbsolutePath()
            )
        );
        sshd.setUserAuthFactories(
            Collections.singletonList(authfactory)
        );
        return sshd;
    }

}
