/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
