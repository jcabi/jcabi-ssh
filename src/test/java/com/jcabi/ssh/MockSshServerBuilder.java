/**
 * Copyright (c) 2014-2017, jcabi.com
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

import com.google.common.base.Optional;
import com.google.common.io.Files;
import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
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
 * Builder creating mock SSH servers.
 *
 * @author Piotr Pradzynski (prondzyn@gmail.com)
 * @version $Id$
 * @since 1.6
 */
class MockSshServerBuilder {

    /**
     * SSH port.
     */
    private final transient int sshport;

    /**
     * User auth factories.
     */
    private final transient List<NamedFactory<UserAuth>> authfactories;

    /**
     * Optional password authenticator.
     */
    private transient Optional<PasswordAuthenticator> passwordauth;

    /**
     * Optional public key authenticator.
     */
    private transient Optional<PublickeyAuthenticator> publickeyauth;

    /**
     * Constructor with a SSH port number.
     * @param port The port number for SSH server
     */
    MockSshServerBuilder(final int port) {
        this.sshport = port;
        this.authfactories = new ArrayList<NamedFactory<UserAuth>>(2);
        this.passwordauth = Optional.absent();
        this.publickeyauth = Optional.absent();
    }

    /**
     * Builds a new instance of SSH server.
     * @return SSH server.
     */
    public SshServer build() {
        final SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(this.sshport);
        sshd.setKeyPairProvider(
            new SimpleGeneratorHostKeyProvider(
                new File(Files.createTempDir(), "hostkey.ser").getAbsolutePath()
            )
        );
        sshd.setUserAuthFactories(this.authfactories);
        sshd.setPasswordAuthenticator(this.passwordauth.orNull());
        sshd.setPublickeyAuthenticator(this.publickeyauth.orNull());
        return sshd;
    }

    /**
     * Setup a password authentication.
     *
     * @param login Login for an authentication.
     * @param password Password for an authentication.
     * @return This instance of builder.
     */
    public MockSshServerBuilder usePasswordAuthentication(
        final String login, final String password) {
        this.authfactories.add(new UserAuthPassword.Factory());
        final PasswordAuthenticator authenticator =
            Mockito.mock(PasswordAuthenticator.class);
        Mockito.when(
            authenticator.authenticate(
                Mockito.eq(login),
                Mockito.eq(password),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        this.passwordauth = Optional.of(authenticator);
        return this;
    }

    /**
     * Setup a public key authentication.
     *
     * @return This instance of builder.
     */
    public MockSshServerBuilder usePublicKeyAuthentication() {
        this.authfactories.add(new UserAuthPublicKey.Factory());
        final PublickeyAuthenticator authenticator =
            Mockito.mock(PublickeyAuthenticator.class);
        Mockito.when(
            authenticator.authenticate(
                Mockito.anyString(),
                Mockito.any(PublicKey.class),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        this.publickeyauth = Optional.of(authenticator);
        return this;
    }

}
