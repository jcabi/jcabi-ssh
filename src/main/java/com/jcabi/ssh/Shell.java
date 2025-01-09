/*
 * Copyright (c) 2014-2025, jcabi.com
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

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.io.DeadInput;
import org.cactoos.io.TeeOutputStream;

/**
 * Shell.
 *
 * <p>This interface is implemented by {@link Ssh} class. In order to use
 * it, just make an instance and call
 * {@link #exec(String,InputStream,OutputStream,OutputStream)} exec()}:
 *
 * <pre> String hello = new Shell.Plain(
 *   new SSH(
 *     "ssh.example.com", 22,
 *     "yegor", "-----BEGIN RSA PRIVATE KEY-----..."
 *   )
 * ).exec("echo 'Hello, world!'");</pre>
 *
 * @since 1.0
 * @see <a href="http://www.yegor256.com/2014/09/02/java-ssh-client.html">article by Yegor Bugayenko</a>
 */
@Immutable
public interface Shell {

    /**
     * Execute and return exit code.
     * @param command Command
     * @param stdin Stdin (will be closed)
     * @param stdout Stdout (will be closed)
     * @param stderr Stderr (will be closed)
     * @return Exit code
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 line)
     */
    int exec(String command, InputStream stdin,
        OutputStream stdout, OutputStream stderr) throws IOException;

    /**
     * Fake shell for unit testing.
     *
     * <p>It doesn't do anything, but imitates the behavior of a real
     * shell, returning the code and the output provided in the ctor.</p>
     *
     * @since 1.6
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "code")
    final class Fake implements Shell {
        /**
         * Exit code.
         */
        private final int code;

        /**
         * Stdout to return.
         */
        private final byte[] stdout;

        /**
         * Stderr to return.
         */
        private final byte[] stderr;

        /**
         * Ctor.
         */
        public Fake() {
            this(0, "", "");
        }

        /**
         * Ctor.
         * @param exit Exit code to return
         * @param out Stdout to return
         * @param err Stderr to return
         */
        public Fake(final int exit, final String out, final String err) {
            this(exit, out.getBytes(), err.getBytes());
        }

        /**
         * Ctor.
         * @param exit Exit code to return
         * @param out Stdout to return
         * @param err Stderr to return
         */
        public Fake(final int exit, final byte[] out, final byte[] err) {
            this.code = exit;
            this.stdout = out;
            this.stderr = err;
        }

        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream sout, final OutputStream serr)
            throws IOException {
            while (true) {
                if (stdin.read(new byte[2048]) < 0) {
                    break;
                }
            }
            sout.write(this.stdout);
            sout.close();
            serr.write(this.stderr);
            serr.close();
            return this.code;
        }
    }

    /**
     * Safe run (throws if exit code is not zero).
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Safe implements Shell {
        /**
         * Original.
         */
        private final transient Shell origin;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Safe(final Shell shell) {
            this.origin = shell;
        }

        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            final int exit = this.origin.exec(command, stdin, stdout, stderr);
            if (exit != 0) {
                throw new IllegalArgumentException(
                    String.format("non-zero exit code #%d: %s", exit, command)
                );
            }
            return exit;
        }
    }

    /**
     * Without input and output.
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Empty {
        /**
         * Original.
         */
        private final transient Shell origin;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Empty(final Shell shell) {
            this.origin = shell;
        }

        /**
         * Just exec.
         * @param cmd Command
         * @return Exit code
         * @throws IOException If fails
         */
        public int exec(final String cmd) throws IOException {
            return this.origin.exec(
                cmd, new DeadInput().stream(),
                Logger.stream(Level.INFO, this),
                Logger.stream(Level.WARNING, this)
            );
        }
    }

    /**
     * With output only.
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Plain {
        /**
         * Original.
         */
        private final transient Shell origin;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Plain(final Shell shell) {
            this.origin = shell;
        }

        /**
         * Just exec.
         * @param cmd Command
         * @return Stdout
         * @throws IOException If fails
         */
        public String exec(final String cmd) throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.origin.exec(
                cmd, new DeadInput().stream(),
                baos, baos
            );
            return baos.toString(StandardCharsets.UTF_8.toString());
        }
    }

    /**
     * Verbose run.
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "orgn")
    final class Verbose implements Shell {
        /**
         * Original.
         */
        private final transient Shell orgn;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Verbose(final Shell shell) {
            this.orgn = shell;
        }

        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            return this.orgn.exec(
                command, stdin,
                new TeeOutputStream(stdout, Logger.stream(Level.INFO, this)),
                new TeeOutputStream(stderr, Logger.stream(Level.WARNING, this))
            );
        }
    }
}
