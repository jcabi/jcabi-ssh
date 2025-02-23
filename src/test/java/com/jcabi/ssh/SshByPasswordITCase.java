/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

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
final class SshByPasswordITCase extends SshITCaseTemplate {

    /**
     * Docker container.
     */
    @Container
    private final GenericContainer<?> sshd = new GenericContainer<>(
        DockerImageName.parse("linuxserver/openssh-server")
    )
        .withEnv("USER_NAME", "jeff")
        .withEnv("USER_PASSWORD", "secret")
        .withEnv("PASSWORD_ACCESS", "true")
        .withExposedPorts(2222);

    @Override
    public Shell shell() throws Exception {
        return new Shell.Verbose(
            new SshByPassword(
                this.sshd.getHost(),
                this.sshd.getMappedPort(2222),
                this.sshd.getEnvMap().get("USER_NAME"),
                this.sshd.getEnvMap().get("USER_PASSWORD")
            )
        );
    }
}
