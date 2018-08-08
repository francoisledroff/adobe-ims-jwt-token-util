
## adobe-ims-jwt-token-util

A small Java utility showcasing Adobe IMS JWT Token exchange.

More info here : 

* https://www.adobe.io/authentication.html
* https://docs.adobe.com/dev/products/target/reference/authentication/CreatingaJSONwebtoken.html
 
It will help you get a JWT token containing the following claims:
* `exp` - the expiration time. IMS allows a time skew of 30 seconds between the time specified and the IMS server time.
* `iss` - the issuer. It must be present, and it must be in the format: `org_ident@AdobeOrg` It represents the identity of the organization which issued the token, and it must be for an organization that has provided IMS with a valid certificate. 
* `sub` - the subject. It must be present and must be in the format: `user_ident@user_auth_src`. It represents the ident and authsrc of the technical account for which a certificate has been uploaded to IMS
* `aud` - the audience of the token. Must be only one entry, in the format: `ENDPOINT_URI/c/client_id`, where the client_id is the client id for which the access token will be issued. The `ENDPOINT_URI` must be a valid IMS endpoint (e.g. `https://ims-na1.adobelogin.com` for IMS production)
* `one or more metascope claims`, in the format: `ENDPOINT_URI/s/SCOPE_CODE: true`, where the ENDPOINT_URI has the same meaning as for the audience, and the SCOPE_CODE is a valid meta-scope code that was granted to you when the certificate binding was created.

Note that Optionally, the JWT can contain the following claims (not implemented here yet)
* `jti` - a unique identifier for the token. It is dependent on the setting being configured when the certificate binding was created, and if it is set as required it must have not been previously seen by the service, or the request will be reject

It will also help you getting this signed with a `RSASSA-PKCS1-V1_5` Digital Signatures with `SHA-2` and a `RS256` The JWT algorithm/`alg` header value.
For this, it leverages a third-party open source library : [jjwt](https://github.com/jwtk/jjwt)

## Test drive

### Create your RSA private/public key and keystore 

Use openssl to Create an RSA private/public certificate

     openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout private.key -out certificate_pub.crt

To add the private key and the signed certificate to a pkcs#12 file

    openssl pkcs12 -keypbe PBE-SHA1-3DES -certpbe PBE-SHA1-3DES -export -in certificate_pub.crt -inkey private.key -out author.pfx -name "author"

To create a keystore from the generated keys, run the following command:

    cat private.key certificate_pub.crt > private-key-crt

Use the following command to set the alias (as `myalias` here)  and a non-empty keystore password.

    openssl pkcs12 -export -in private-key-crt -out keystore.p12 -name myalias -noiter -nomaciter

Make a note of your alias and password

### Configure your adobe.io integration

* Click New Integration.
* Select the Enterprise key section, and then click Next.
* From the Organization list, choose the organization for which you want to create the integration.
* Specify a name for the integration gateway and add a description.
* Upload the file `certificate_pub.crt` to the integration service.
* Enter the captcha text, and then click Next.
* Select the  service from the Add service list, click +Add Service, and then click Save (bottom of page)

### Generate your JWT token

With:
* your p12 key store file
* all the above adobe.io console info mentionned above 
* plus the the IMS metascopes that are associated with the services you selected

Once the above is done, edit [.src/main/resources/ims.properties](src/main/resources/ims.properties) according to your desired settings.

1. Install `jdk 1.8.0_72` or higher
2. Install `apache-maven-3.3.9` or higher
3. Build it: `mvn clean install`
4. Run it: `java -jar ./target/adobe-ims-jwt-token-util-0.1.0.jar ./src/main/resources/ims.properties`

Out of this you will get a valid IMS JWT Token 
Refer to https://jwt.io/ if you want to decode, inspect and validate this Jwt Token.
And `bonus`, your jwt token will be used, to fetch a access token from IMS.

## found a bug, request a feature ?

Then submit a bug and a PR.

## Similar tools/projects

* https://www.adobe.io/authentication.html

## Various pointers, further reading

* https://jwt.io/
* https://docs.adobe.com/dev/products/target/reference/authentication/CreatingaJSONwebtoken.html
* https://developer.atlassian.com/static/connect/docs/latest/concepts/understanding-jwt.html
* https://stormpath.com/blog/jwt-java-create-verify
* https://github.com/jwtk/jjwt
  

  