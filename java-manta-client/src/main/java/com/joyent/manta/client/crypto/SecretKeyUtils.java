/*
 * Copyright (c) 2016, Joyent, Inc. All rights reserved.
 */
package com.joyent.manta.client.crypto;

import com.joyent.manta.exception.MantaClientEncryptionException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Utility class providing functions for working with {@link javax.crypto.SecretKey}
 * instances.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 3.0.0
 */
public final class SecretKeyUtils {
    /**
     * Private constructor because this is a utility class.
     */
    private SecretKeyUtils() {
    }

    /**
     * Generates a new symmetric key using the specified cipher.
     *
     * @param cipherDetails cipher to generate key for
     * @return new instance of key
     * @throws MantaClientEncryptionException thrown if there is a problem getting the cipher
     */
    public static SecretKey generate(final SupportedCipherDetails cipherDetails) {
        try {
            return generate(cipherDetails.getKeyGenerationAlgorithm(), cipherDetails.getKeyLengthBits());
        } catch (NoSuchAlgorithmException e) {
            String msg = String.format("Couldn't find algorithm [%s]");
            throw new MantaClientEncryptionException(msg, e);
        }
    }

    /**
     * Generates a new symmetric key using the specified cipher.
     *
     * @param algorithm cipher to generate key for
     * @param bits number of bits of key
     * @return new instance of key
     * @throws NoSuchAlgorithmException thrown when no cipher is available by the passed name
     */
    public static SecretKey generate(final String algorithm, final int bits)
            throws NoSuchAlgorithmException {
        Validate.notNull(algorithm, "Cipher must not be null");
        Validate.isTrue(bits > 0, "Cipher bits must be greater than zero");

        KeyGenerator symKeyGenerator = KeyGenerator.getInstance(algorithm,
                BouncyCastleLoader.BOUNCY_CASTLE_PROVIDER);
        symKeyGenerator.init(bits);
        return symKeyGenerator.generateKey();
    }

    /**
     * Writes the specified key in X509 encoding to a stream. Note: This method
     * doesn't close the supplied stream.
     *
     * @param key key to write
     * @param out output stream to write to
     * @throws IOException thrown when there is a problem writing the key
     */
    public static void writeKey(final SecretKey key, final OutputStream out)
            throws IOException {
        Validate.notNull(key, "Key must not be null");
        Validate.notNull(out, "OutputStream must not be null");
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                key.getEncoded());
        out.write(x509EncodedKeySpec.getEncoded());
    }

    /**
     * Writes the specified key in X509 encoding to a NIO2 {@link Path}.
     *
     * @param key key to write
     * @param path path to write to
     * @throws IOException thrown when there is a problem writing the key
     */
    public static void writeKeyToPath(final SecretKey key, final Path path)
            throws IOException {
        Validate.notNull(key, "Key must not be null");

        try (OutputStream out = Files.newOutputStream(path)) {
            writeKey(key, out);
        }
    }

    /**
     * Writes the specified key in X509 encoding to a {@link File}.
     *
     * @param key key to write
     * @param file file to write to
     * @throws IOException thrown when there is a problem writing the key
     */
    public static void writeKeyKeyToFile(final SecretKey key, final File file)
            throws IOException {
        Validate.notNull(file, "File must not be null");
        writeKeyToPath(key, file.toPath());
    }

    /**
     * Loads symmetric secret key with the specified cipher into a
     * {@link SecretKeySpec} object from a stream. Note: This method doesn't
     * close the supplied stream and will read the entire contents of the
     * stream supplied.
     *
     * @param bytes byte array to read secret key from
     * @param cipherDetails the secret key's cipher
     * @return a new instance based on the secret key loaded
     * @throws NoSuchAlgorithmException thrown when no cipher is available by the passed name
     * @throws IOException thrown when there is a problem reading or parsing the key
     */
    public static SecretKeySpec loadKey(final byte[] bytes, final SupportedCipherDetails cipherDetails) {
        Validate.notNull(bytes, "Byte array must not be null");
        Validate.notNull(cipherDetails, "Cipher details must not be null");

        return new SecretKeySpec(bytes, cipherDetails.getKeyGenerationAlgorithm());
    }

    /**
     * Loads symmetric secret key with the specified cipher into a
     * {@link SecretKeySpec} object from a stream. Note: This method doesn't
     * close the supplied stream and will read the entire contents of the
     * stream supplied.
     *
     * @param in stream to read secret key from
     * @param algorithm the secret key's cipher name
     * @return a new instance based on the secret key loaded
     * @throws NoSuchAlgorithmException thrown when no cipher is available by the passed name
     * @throws IOException thrown when there is a problem reading or parsing the key
     */
    public static SecretKeySpec loadKey(final InputStream in, final String algorithm)
            throws NoSuchAlgorithmException, IOException {
        Validate.notNull(in, "InputStream must not be null");
        Validate.notNull(algorithm, "Cipher must not be null");
        byte[] bytesAsEncodedKey = IOUtils.toByteArray(in);

        return new SecretKeySpec(bytesAsEncodedKey, algorithm);
    }

    /**
     * Loads symmetric secret key with the specified cipher into a
     * {@link SecretKeySpec} object from a path.
     *
     * @param path path to read secret key from
     * @param algorithm the secret key's cipher name
     * @return a new instance based on the secret key loaded
     * @throws NoSuchAlgorithmException thrown when no cipher is available by the passed name
     * @throws IOException thrown when there is a problem reading or parsing the key
     */
    public static SecretKeySpec loadKeyFromPath(final Path path, final String algorithm)
            throws NoSuchAlgorithmException, IOException {
        Validate.notNull(path, "Path must not be null");
        try (InputStream in = Files.newInputStream(path)) {
            return loadKey(in, algorithm);
        }
    }

    /**
     * Loads symmetric secret key with the specified cipher into a
     * {@link SecretKeySpec} object from a path.
     *
     * @param file file to read secret key from
     * @param algorithm the secret key's cipher name
     * @return a new instance based on the secret key loaded
     * @throws NoSuchAlgorithmException thrown when no cipher is available by the passed name
     * @throws IOException thrown when there is a problem reading or parsing the key
     */
    public static SecretKeySpec loadKeyFromFile(final File file, final String algorithm)
            throws NoSuchAlgorithmException, IOException {
        Validate.notNull(file, "File must not be null");
        return loadKeyFromPath(file.toPath(), algorithm);
    }
}
