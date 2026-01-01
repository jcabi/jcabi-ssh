/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import org.cactoos.io.DeadInputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link Execution}.
 * @since 1.4
 */
final class ExecutionTest {

    /**
     * Exit code expected from a command.
     */
    public static final int EXIT_CODE = 127;

    @Test
    void executesCommand() throws Exception {
        final Session session = Mockito.mock(Session.class);
        final ChannelExec channel = Mockito.mock(ChannelExec.class);
        Mockito.when(session.openChannel(Mockito.anyString()))
            .thenReturn(channel);
        Mockito.when(channel.isClosed()).thenReturn(Boolean.TRUE);
        Mockito.when(channel.getExitStatus()).thenReturn(
            ExecutionTest.EXIT_CODE
        );
        MatcherAssert.assertThat(
            "should equal to exit code 127",
                new Execution(
                "hello",
                new DeadInputStream(),
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream(),
                session
            ).exec(),
            Matchers.equalTo(ExecutionTest.EXIT_CODE)
        );
    }
}
