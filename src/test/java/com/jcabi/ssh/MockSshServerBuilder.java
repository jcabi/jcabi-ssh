/*
 * Copyright (c) 2014-2022, jcabi.com
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.mockito.Mockito;

/**
 * Builder creating mock SSH servers.
 *
 * @since 1.6
 */
class MockSshServerBuilder {

    /**
     * SSH port.
     */
    private final transient int port;

    /**
     * User auth factories.
     */
    private final transient List<UserAuthFactory> factories;

    /**
     * Optional password authenticator.
     */
    private transient Optional<PasswordAuthenticator> pwd;

    /**
     * Optional public key authenticator.
     */
    private transient Optional<PublickeyAuthenticator> pkey;

    /**
     * Constructor with a SSH port number.
     * @param prt The port number for SSH server
     */
    MockSshServerBuilder(final int prt) {
        this.port = prt;
        this.factories = new ArrayList<>(2);
        this.pwd = Optional.absent();
        this.pkey = Optional.absent();
    }

    /**
     * Builds a new instance of SSH server.
     * @return SSH server.
     * @throws IOException If fails
     */
    public SshServer build() throws IOException {
        final SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(this.port);
        final Path temp = Files.createTempDirectory("ssh");
        temp.toFile().deleteOnExit();
        sshd.setKeyPairProvider(
            new SimpleGeneratorHostKeyProvider(
                temp.resolve("hostkey.ser")
            )
        );
        sshd.setUserAuthFactories(this.factories);
        sshd.setPasswordAuthenticator(this.pwd.orNull());
        sshd.setPublickeyAuthenticator(this.pkey.orNull());
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
        this.factories.add(new UserAuthPasswordFactory());
        final PasswordAuthenticator auth =
            Mockito.mock(PasswordAuthenticator.class);
        Mockito.when(
            auth.authenticate(
                Mockito.eq(login),
                Mockito.eq(password),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        this.pwd = Optional.of(auth);
        return this;
    }

    /**
     * Setup a public key authentication.
     *
     * @return This instance of builder.
     */
    public MockSshServerBuilder usePublicKeyAuthentication() {
        this.factories.add(new UserAuthPublicKeyFactory());
        final PublickeyAuthenticator auth =
            Mockito.mock(PublickeyAuthenticator.class);
        Mockito.when(
            auth.authenticate(
                Mockito.anyString(),
                Mockito.any(PublicKey.class),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        this.pkey = Optional.of(auth);
        return this;
    }

}
