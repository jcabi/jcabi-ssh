/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import java.nio.file.Path;
import org.cactoos.text.TextOf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test for ${@link Ssh}, which connects to
 * a real SSHD server over the Internet.
 *
 * @since 1.0
 */
@Testcontainers(disabledWithoutDocker = true)
final class SshITCase extends SshITCaseTemplate {

    /**
     * Directory to store key pair.
     */
    @TempDir
    private static Path keys;

    /**
     * Docker container.
     */
    @Container
    private final GenericContainer<?> sshd = new GenericContainer<>(
        DockerImageName.parse("linuxserver/openssh-server")
    )
        .withEnv("USER_NAME", "jeff")
        .withEnv("PASSWORD_ACCESS", "false")
        .withEnv("PUBLIC_KEY", new TextOf(keys.resolve("rsa.pub")).toString())
        .withExposedPorts(2222);

    @Override
    public Shell shell() throws Exception {
        return new Shell.Verbose(
            new Ssh(
                this.sshd.getHost(),
                this.sshd.getMappedPort(2222),
                this.sshd.getEnvMap().get("USER_NAME"),
                new TextOf(keys.resolve("rsa")).toString(),
                null
            )
        );
    }

    /**
     * Generate key pair.
     * @throws Exception If fails.
     */
    @BeforeAll
    static void setUp() throws Exception {
        final KeyPair kpair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA);
        final Path rsa = keys.resolve("rsa");
        final String filename = rsa.toAbsolutePath().toString();
        kpair.writePrivateKey(filename, null);
        kpair.writePublicKey(String.format("%s.pub", filename), "");
        kpair.dispose();
    }
}
