/*
 * Copyright (c) 2014-2025 Yegor Bugayenko
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
