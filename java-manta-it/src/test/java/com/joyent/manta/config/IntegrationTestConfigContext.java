package com.joyent.manta.config;

import com.joyent.manta.client.crypto.SecretKeyUtils;
import com.joyent.manta.client.crypto.SupportedCipherDetails;
import org.apache.commons.lang3.BooleanUtils;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * {@link ConfigContext} implementation that loads
 * configuration parameters in an order that makes sense for unit testing
 * and allows for TestNG parameters to be loaded.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class IntegrationTestConfigContext extends SystemSettingsConfigContext {
    /**
     * Populate configuration from defaults, environment variables, system
     * properties and an addition context passed in.
     */
    public IntegrationTestConfigContext() {
        super(enableTestEncryption(new StandardConfigContext(), encryptionEnabled()));
    }

    /**
     * Populate configuration from defaults, environment variables, system
     * properties and an addition context passed in. Assigns hard-coded
     * client-side encryption configuration settings.
     */
    public IntegrationTestConfigContext(Boolean usingEncryption) {
        super(enableTestEncryption(new StandardConfigContext(),
                (encryptionEnabled() && usingEncryption == null) ||
                        BooleanUtils.isTrue(usingEncryption)));
    }

    private static <T> SettableConfigContext<T> enableTestEncryption(
            final SettableConfigContext<T> context,
            final boolean usingEncryption) {
        if (usingEncryption) {
            context.setClientEncryptionEnabled(true);
            context.setEncryptionKeyId("integration-test-key");

            SupportedCipherDetails cipherDetails = DefaultsConfigContext.DEFAULT_CIPHER;
            context.setEncryptionAlgorithm(cipherDetails.getCipherId());
            SecretKey key = SecretKeyUtils.generate(cipherDetails);
            context.setEncryptionPrivateKeyBytes(key.getEncoded());

            System.out.printf("Unique secret key used for test (base64):\n%s\n",
                    Base64.getEncoder().encodeToString(key.getEncoded()));
        }

        return context;
    }

    public static boolean encryptionEnabled() {
        String sysProp = System.getProperty(MapConfigContext.MANTA_CLIENT_ENCRYPTION_ENABLED_KEY);
        String envVar = System.getenv(EnvVarConfigContext.MANTA_CLIENT_ENCRYPTION_ENABLED_ENV_KEY);

        return BooleanUtils.toBoolean(sysProp) || BooleanUtils.toBoolean(envVar);
    }
}
