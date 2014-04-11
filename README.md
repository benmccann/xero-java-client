## Downloading

The library is available from the Central Maven Repository and can be used easily by many tools including Maven, Gradle, SBT, etc.

    <dependency>
      <groupId>com.connectifier.xero</groupId>
      <artifactId>client</artifactId>
      <version>0.1</version>
    </dependency>

## Example Usage

    Reader pemReader = new FileReader(new File("my-x509-cert.pem"));
    XeroClient client = new XeroClient(pemReader, consumerKey, consumerSecret);
    client.getInvoices();

## Not yet supported

Pull requests will be accepted for any not yet supported features if they are accompanied by tests.

Currently, only the private app authentication method has been implemented. We use Scribe to support OAuth, so support for the public app OAuth should be straight forward to implement if needed.

Support for attachments has not yet been added.

Repeating invoices and bank transfers are not supported due to lack of support in Xero's own XML schemas. This repository will be updated when those bugs are fixed. See:
* https://github.com/XeroAPI/XeroAPI-Schemas/issues/8
* https://github.com/XeroAPI/XeroAPI-Schemas/issues/9

This client currently only has methods included or read calls, but it should only be a few lines to make write calls. Additionally, all fields in the XeroClient are protected to make it easy to extend in your own code without needing to update this client.

## Hacking on this library

The Gradle build tool must be installed. https://github.com/XeroAPI/XeroAPI-Schemas must be checked out as a sibling project. Build with:

    gradle compileJava
