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

import java.nio.file.Path;
import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for ${@link Ssh}.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SshdTest {

    /**
     * Check that it's not Windows.
     */
    @BeforeEach
    public void notWindows() {
        Assumptions.assumeFalse(SystemUtils.IS_OS_WINDOWS);
    }

    @Test
    public void executeCommandOnServer() throws Exception {
        final Sshd sshd = new Sshd();
        try {
            MatcherAssert.assertThat(
                new Shell.Plain(
                    new Shell.Safe(sshd.connect())
                ).exec("echo one"),
                Matchers.startsWith("one")
            );
        } finally {
            sshd.close();
        }
    }

    @Test
    public void executeCommandOnServerWithManualConfig(
        @TempDir final Path temp) throws Exception {
        final Sshd sshd = new Sshd(temp.toFile());
        try {
            MatcherAssert.assertThat(
                new Shell.Plain(
                    new Shell.Safe(
                        new Ssh(
                            Sshd.host(), sshd.port(), Sshd.login(),
                            this.getClass().getResource("id_rsa")
                        )
                    )
                ).exec("echo 'how are you'"),
                Matchers.startsWith("how are")
            );
        } finally {
            sshd.close();
        }
    }

}
