/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.UserInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Host key repository that accepts all hosts.
 *
 * @since 1.4
 */
@ToString
@EqualsAndHashCode
final class EasyRepo implements HostKeyRepository {

    @Override
    public int check(final String host, final byte[] bkey) {
        return HostKeyRepository.OK;
    }

    @Override
    public void add(final HostKey hostkey, final UserInfo info) {
        // do nothing
    }

    @Override
    public void remove(final String host, final String type) {
        // do nothing
    }

    @Override
    public void remove(final String host, final String type,
        final byte[] bkey) {
        // do nothing
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return "";
    }

    @Override
    public HostKey[] getHostKey() {
        return new HostKey[0];
    }

    @Override
    public HostKey[] getHostKey(final String host, final String type) {
        return new HostKey[0];
    }

}
