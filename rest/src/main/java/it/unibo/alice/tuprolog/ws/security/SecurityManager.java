package it.unibo.alice.tuprolog.ws.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;


/**
 * @author Andrea Muccioli
 *
 */
@DependsOn("StartupOperations")
@Singleton
@LocalBean
public class SecurityManager {
	
	@Resource(name="user/properties")
	Properties props;
	
	private EllipticCurveJsonWebKey ConfSigKey = null;
	private EllipticCurveJsonWebKey ConfEncKey = null;
	private EllipticCurveJsonWebKey EngineSigKey = null;
	private EllipticCurveJsonWebKey EngineEncKey = null;

    public SecurityManager() {
    }
    
    /**
     * Initializes all the keys and sets the key ids.
     * 
     */
    @PostConstruct
    private void initialize() {
		try {
			ConfSigKey = EcJwkGenerator.generateJwk(EllipticCurves.P256);
			ConfSigKey.setKeyId("Configuration Signature Key");
			ConfEncKey = EcJwkGenerator.generateJwk(EllipticCurves.P256);
			ConfEncKey.setKeyId("Configuration Encription Key");
			EngineSigKey = EcJwkGenerator.generateJwk(EllipticCurves.P256);
			EngineSigKey.setKeyId("Engine Signature Key");
			EngineEncKey = EcJwkGenerator.generateJwk(EllipticCurves.P256);
			EngineEncKey.setKeyId("Engine Encription Key");
		} catch (JoseException e) {
		      e.printStackTrace();
	    }
    }
    
    @Lock(LockType.READ)
	public String validate(String username, String password) {
		String usr = props.getProperty("configuration.admin.username");
		String psw = props.getProperty("configuration.admin.password");
		if (usr.equals(username)&&psw.equals(password))
			return "configurator";
		else
			return null;
	}
    
    /**
     * Creates the claims for the authentication JWT.
     * 
     * @param username : the username of the user.
     * @param role : the role of the user
     * @return the JwtClaims of the authentication JWT.
     */
    @Lock(LockType.READ)
    public JwtClaims getConfigurationClaims(String username, String role) {
    	JwtClaims claims = new JwtClaims();
		claims.setIssuer("it.unibo.alice.tuprolog");
		claims.setExpirationTimeMinutesInTheFuture(10);
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		claims.setNotBeforeMinutesInThePast(2);
		claims.setSubject(username);
		claims.setClaim("role", role);
		
		return claims;
    }
    
    /**
     * Signs the claims using the private key of ConfSigKey and returns the compact serialization 
     * 
     * @param claims : the claims to sign.
     * @return the String serialization of the signed claims.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String signClaimsAndGetSerialization(JwtClaims claims) throws JoseException {
    	JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKeyIdHeaderValue(ConfSigKey.getKeyId());
		jws.setKey(ConfSigKey.getPrivateKey());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
		String serialization = jws.getCompactSerialization();
		return serialization;
    }
    
    /**
     * Encrypts the JWS serialization of the JWT token with the public ConfEncKey
     * and returns the compact serialization of the JWE.
     * 
     * @param payload : the serialization of the JWS
     * @return the String serialization of the JWE.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String encryptAndGetSerialization(String payload) throws JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES_A128KW);
		String encAlg = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;
		jwe.setEncryptionMethodHeaderParameter(encAlg);
		jwe.setKey(ConfEncKey.getPublicKey());
		jwe.setKeyIdHeaderValue(ConfEncKey.getKeyId());
		jwe.setContentTypeHeaderValue("JWT");
		jwe.setPayload(payload);
		String jweSerialization = jwe.getCompactSerialization();
		
		return jweSerialization;
    }
    
    /**
     * Decrypts and verifies the authentication token returning the JWT claims.
     * 
     * @param token : the encrypted and signed authentication token.
     * @return the JwtClaims.
     * @throws InvalidJwtException
     */
    @Lock(LockType.READ)
    public JwtClaims decryptAndVerifyToken(String token) throws InvalidJwtException {
    	JwtConsumer consumer = new JwtConsumerBuilder().setRequireExpirationTime()
				.setAllowedClockSkewInSeconds(30)
				.setRequireSubject()
				.setExpectedIssuer("it.unibo.alice.tuprolog")
				.setDecryptionKey(ConfEncKey.getPrivateKey())
				.setVerificationKey(ConfSigKey.getPublicKey()).build();
		
		JwtClaims jwtClaims = consumer.processToClaims(token);
		return jwtClaims;
    }
    
    /**
     * Decrypts and verifies the engine token returning the JSON serialization of the engine as String
     * 
     * @param serialization : the JWE serialization of the engine.
     * @return the JSON serialization of the engine.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String decryptAndVerifyEngine(String serialization) throws JoseException {
    	String jwePayload = decrypt(EngineEncKey.getPrivateKey(), serialization);
    	return verify(EngineSigKey.getPublicKey(), jwePayload);
    }
    
    /**
     * Signs and encrypts the engine JSON serialization.
     * 
     * @param engineJsonState : the JSON serialization of the engine state as String.
     * @return the JWE serialization as String.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String signAndEncryptEngine(String engineJsonState) throws JoseException {
    	String jwsSerialization = sign(EngineSigKey.getPrivateKey(), AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256, engineJsonState);
    	String jweSerialization = encrypt(EngineEncKey.getPublicKey(), KeyManagementAlgorithmIdentifiers.ECDH_ES_A128KW, jwsSerialization);
    	return jweSerialization;
    }
    
    
    /**
     * Encrypts the payload using the provided public key and algorithm.
     * 
     * @param key : the public key to use.
     * @param algorithm : the algorithm to use.
     * @param payload : the data to encrypt.
     * @return the JWE serialization of the encrypted data.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String encrypt(PublicKey key, String algorithm, String payload) throws JoseException {
    	JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue(algorithm);
		String encAlg = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;
		jwe.setEncryptionMethodHeaderParameter(encAlg);
		jwe.setKey(key);
		jwe.setPayload(payload);
		
		return jwe.getCompactSerialization();
    }
    
    /**
     * Signs the payload using the provided private key and algorithm.
     * 
     * @param key : the private key to use.
     * @param algorithm : the algorithm to use.
     * @param payload : the data to sign.
     * @return the JWS serialization of the signed data.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String sign(PrivateKey key, String algorithm, String payload) throws JoseException {
    	JsonWebSignature jws = new JsonWebSignature();
    	jws.setPayload(payload);
		jws.setKey(key);
		jws.setAlgorithmHeaderValue(algorithm);
    	return jws.getCompactSerialization();
    }
    
    
    
    /**
     * Decrypts the JWE using the provided private key.
     * 
     * @param key : the private key to use.
     * @param compactSerialization : the serialization of the JWE to decrypt.
     * @return the decrypted payload of the JWE.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String decrypt(PrivateKey key, String compactSerialization) throws JoseException {
    	JsonWebEncryption jwe = new JsonWebEncryption();
    	jwe.setCompactSerialization(compactSerialization);
    	jwe.setKey(key);
    	String payload = jwe.getPlaintextString();
    	return payload;
    }
    
    /**
     * Verifies the JWS using the provided public key.
     * 
     * @param key : the public key to use.
     * @param compactSerialization : the serialization of the JWS to verify.
     * @return the verified payload of the JWS.
     * @throws JoseException
     */
    @Lock(LockType.READ)
    public String verify(PublicKey key, String compactSerialization) throws JoseException {
    	JsonWebSignature jws = new JsonWebSignature();
    	jws.setCompactSerialization(compactSerialization);
    	jws.setKey(key);
    	if (jws.verifySignature())
    		return jws.getPayload();
    	else
    		throw new IllegalArgumentException("Signature is not valid");
    }

}
