/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcabi.log.Logger;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Jsch Logger.
 *
 * @since 1.4
 */
@ToString
@EqualsAndHashCode
final class JschLogger implements com.jcraft.jsch.Logger {

    @Override
    public boolean isEnabled(final int level) {
        return level >= com.jcraft.jsch.Logger.WARN;
    }

    @Override
    public void log(final int level, final String msg) {
        final Level jul;
        if (level >= com.jcraft.jsch.Logger.ERROR) {
            jul = Level.SEVERE;
        } else if (level >= com.jcraft.jsch.Logger.WARN) {
            jul = Level.WARNING;
        } else {
            jul = Level.INFO;
        }
        Logger.log(jul, Ssh.class, msg);
    }

}
