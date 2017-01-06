/*
 * Copyright (c) 2017, Joyent, Inc. All rights reserved.
 */
package com.joyent.manta.client.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

/**
 * This class statically loads the Legion of the Bouncy Castle encryption
 * libraries into memory, so that they are available to all consumers.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 3.0.0
 */
public final class BouncyCastleLoader {
    /**
     * Reference to the Bouncy Castle Provider.
     */
    public static final Provider BOUNCY_CASTLE_PROVIDER;

    static {
        Provider bouncyCastleProvider = Security.getProvider(
                "org.bouncycastle.jce.provider.BouncyCastleProvider");
        if (bouncyCastleProvider == null) {
            BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
            Security.addProvider(BOUNCY_CASTLE_PROVIDER);
        } else {
            BOUNCY_CASTLE_PROVIDER = bouncyCastleProvider;
        }
    }

    /**
     * Private constructor because this class only does static loading.
     */
    private BouncyCastleLoader() {
    }
}
